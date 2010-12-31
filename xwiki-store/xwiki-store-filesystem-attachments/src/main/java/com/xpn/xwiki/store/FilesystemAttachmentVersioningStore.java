/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.store;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiAttachmentArchive;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import org.suigeneris.jrcs.rcs.Version;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.store.filesystem.internal.FilesystemStoreTools;
import org.xwiki.store.serialization.Serializer;
import org.xwiki.store.TransactionRunnable;


/**
 * Interface for storing attachment versions.
 * 
 * @version $Id$
 * @since TODO
 */
@Component("file")
public class FilesystemAttachmentVersioningStore implements AttachmentVersioningStore
{
    /** Tools for getting files to store given content in. */
    @Requirement
    private FilesystemStoreTools fileTools;

    /** A serializer for the list of attachment metdata. */
    @Requirement
    private Serializer<List<XWikiAttachment>> metaSerializer;

    /**
     * Load attachment archive from store.
     * 
     * @return attachment archive. not null. return empty archive if it is not exist in store.
     * @param attachment The attachment of archive.
     * @param context The current context.
     * @param bTransaction Should use old transaction (false) or create new (true).
     * @throws XWikiException If an error occurs.
     */
    public XWikiAttachmentArchive loadArchive(final XWikiAttachment attachment,
                                              final XWikiContext context,
                                              final boolean bTransaction)
        throws XWikiException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * bTransaction cannot be used in this case, in order to have transaction atomicity,
     * please use getArchiveSaveRunnable() instead.
     *
     * @see AttachmentVersioningStore#saveArchive(XWikiAttachmentArchive, XWikiContext, boolean)
     */
    public void saveArchive(XWikiAttachmentArchive archive, XWikiContext context, boolean bTransaction)
        throws XWikiException
    {
        try {
            this.getArchiveSaveRunnable(archive, context).start()
        } catch (Exception e) {
            if (e instanceof XWikiException) {
                throw (XWikiException) e;
            }
            final Object[] args = { "UNKNOWN", "UNKNOWN" };
            if (archive.getAttachment()) {
                args[0] = archive.getAttachment().getFilename();
            }
            if (archive.getDoc()) {
                args[1] = archive.getAttachment().getDoc().getFullName();
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                     XWikiException.ERROR_UNKNOWN,
                                     "Exception while saving attachment archive {0} of document {1}",
                                     e, args);
        }
    }

    /**
     * Get a TransactionRunnable for saving or updating the current attachment.
     * this runnable can be run with any transaction including a VoidTransaction.
     * 
     * @param archive The attachment archive to save.
     * @param context An XWikiContext used for getting the attachments from the archive with getRevision()
     *                and for getting the content from the attachments with getContentInputStream().
     */
    private TransactionRunnable getArchiveSaveRunnable(final XWikiAttachmentArchive archive,
                                                       final XWikiContext context)
    {
        return new ArchiveSaveRunnable(archive, this.fileTools, this.metaSerializer, context);
    }

    /**
     * Permanently delete attachment archive.
     * 
     * @param attachment The attachment to delete.
     * @param context The current context.
     * @param bTransaction Should use old transaction (false) or create new (true).
     * @throws XWikiException If an error occurs.
     */
    public void deleteArchive(XWikiAttachment attachment, XWikiContext context, boolean bTransaction)
        throws XWikiException
    {
    }

    /*--------------------- Nested classes ---------------------*/

    private static class ArchiveSaveRunnable extends ChainingTransactionRunnable
    {
        /** The XWikiAttachmentArchive to save. */
        private final XWikiAttachmentArchive archive;

        /** Filesystem storage tools for getting files for each revision of the attachment. */
        private final FilesystemStoreTools filetools;

        /** For serializing the metadata from each revision of the attachment. */
        private final Serializer<List<XWikiAttachment>> attachmentListMetaSerilizer;

        /** The XWikiContext used to get the versions of the attachment. */
        private final XWikiContext context;

        /**
         * The Constructor.
         *
         * @param archive the attachment archive to save.
         * @param fileTools a set of tools for getting the file corrisponding to each version of the
         *                  attachment content and the file for the meta data, as well as temporary
         *                  and backup files corrisponding to each. Also for getting locks.
         * @param serializer an attachment list metadata serializer for serializing the metadata of each
         *                   version of the attachment.
         * @param context the XWikiContext used to get the revisions of the attachment.
         */
        public ArchiveSaveRunnable(final XWikiAttachmentArchive archive,
                                   final FilesystemStoreTools fileTools,
                                   final Serializer<List<XWikiAttachment>> serializer,
                                   final XWikiContext context)
        {
            this.archive = archive;
            this.fileTools = fileTools;
            this.attachmentListMetaSerilizer = serializer;
            this.context = context;
        }

        /**
         * {@inheritDoc}
         * Get attachment versions and acquire locks.
         * Content of the attachment will only be saved for revisions whose content is dirty.
         *
         * @see TransactionRunnable#preRun()
         */
        protected void preRun()
        {
            final Version[] versions = this.archive.getVersions();

            final List<XWikiAttachment> attachmentVersions =
                new ArrayList<XWikiAttachment>(versions.length);

            // Add the content files which need updating and add the attachments to the list.
            for (int i = 0; i < versions.length; i++) {
                final String versionName = versions[i].toString();
                final XWikiAttachment attachVer = archive.getRevision(versionName, this.context);
                attachmentVersions.add(attachVer);

                if (attachVer.isContentDirty()) {
                    // If the content is not dirty then it will not be updated.
                    final File contentFile =
                        this.fileTools.fileForAttachmentVersion(attachVer, versionName);

                    final StreamProvider contentProvider =
                        new AttachmentContentStreamProvider(attachVer, this.context);

                    final TransactionRunnable contentSaveRunnable =
                        new FileSaveRunnable(contentFile,
                                             this.fileTools.getTempFile(contentFile),
                                             this.fileTools.getBackupFile(contentFile),
                                             this.fileTools.getLockForFile(contentFile),
                                             provider);

                    this.add(contentSaveRunnable);
                }
            }

            // Now do the metadata.
            final File attachMetaFile =
                this.fileTools.metaFileForAttachment(archive.getAttachment());

            final StreamProvider provider =
                new AttachmentListMetadataStreamProvider(this.attachmentListMetaSerilizer,
                                                         attachmentVersions);
            final TransactionRunnable metaSaveRunnable =
                new FileSaveRunnable(attachMetaFile,
                                     this.fileTools.getTempFile(attachMetaFile),
                                     this.fileTools.getBackupFile(attachMetaFile),
                                     this.fileTools.getLockForFile(attachMetaFile),
                                     provider);

            this.add(metaSaveRunnable);
 

            // Now manually preRun() each runnable.
            super.preRun();
        }
    }

    private static class AttachmentListMetadataStreamProvider implements StreamProvider
    {
        private final Serializer<List<XWikiAttachment>> serializer;

        private final List<XWikiAttachment> attachList;

        public AttachmentListMetadataStreamProvider(final Serializer<List<XWikiAttachment>> serializer,
                                                    final List<XWikiAttachment> attachList)
        {
            this.serializer = serializer;
            this.attachList = attachList;
        }

        public InputStream getStream()
        {
            this.serializer.serialize(attachList);
        }
    }

    private static class AttachmentContentStreamProvider implements StreamProvider
    {
        /** The attachment to save content of. */
        private final XWikiAttachment attach;

        /** The XWikiContext for getting the content of the attachment. */
        private final XWikiContext context;

        public AttachmentContentStreamProvider(final XWikiAttachment attach,
                                               final XWikiContext context)
        {
            this.attach = attach;
            this.context = context;
        }

        public InputStream getStream()
        {
            return this.attachment.getContentInputStream(this.context);
        }
    }
}
