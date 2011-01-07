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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiAttachmentArchive;
import com.xpn.xwiki.doc.FilesystemAttachmentContent;
import com.xpn.xwiki.doc.ListAttachmentArchive;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import org.suigeneris.jrcs.rcs.Version;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.store.filesystem.internal.FilesystemStoreTools;
import org.xwiki.store.serialization.Serializer;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.FileSaveTransactionRunnable;
import org.xwiki.store.FileDeleteTransactionRunnable;
import org.xwiki.store.StreamProvider;


/**
 * Filesystem based AttachmentVersioningStore implementation.
 * 
 * @version $Id$
 * @since TODO
 */
@Component("file")
public class FilesystemAttachmentVersioningStore implements AttachmentVersioningStore
{
    /** To put in an exception message if the document name or filename cannot be determined. */
    private static final String UNKNOWN_NAME = "UNKNOWN";

    /** Tools for getting files to store given content in. */
    @Requirement
    private FilesystemStoreTools fileTools;

    /** A serializer for the list of attachment metdata. */
    @Requirement
    private Serializer<List<XWikiAttachment>> metaSerializer;

    /**
     * {@inheritDoc}
     *
     * @see AttachmentVersioningStore#loadArchive(XWikiAttachment, XWikiContext, boolean)
     */
    public XWikiAttachmentArchive loadArchive(final XWikiAttachment attachment,
                                              final XWikiContext context,
                                              final boolean bTransaction)
        throws XWikiException
    {
        final File metaFile = this.fileTools.metaFileForAttachment(attachment);
        final ReadWriteLock lock = this.fileTools.getLockForFile(metaFile);
        final List<XWikiAttachment> attachList;
        lock.readLock().lock();
        try {
            final InputStream is = new FileInputStream(metaFile);
            attachList = this.metaSerializer.parse(is);
            is.close();
        } catch (Exception e) {
            if (e instanceof XWikiException) {
                throw (XWikiException) e;
            }
            final Object[] args = {attachment.getFilename(), UNKNOWN_NAME};
            if (attachment.getDoc() != null) {
                args[1] = attachment.getDoc().getFullName();
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                     XWikiException.ERROR_XWIKI_UNKNOWN,
                                     "Exception while loading attachment archive {0} for document {1}",
                                     e, args);
        } finally {
            lock.readLock().unlock();
        }

        // Get the content file and lock for each revision.
        for (XWikiAttachment attach : attachList) {
            final File contentFile =
                this.fileTools.fileForAttachmentVersion(attachment, attachment.getVersion());
            attach.setAttachment_content(
                new FilesystemAttachmentContent(contentFile,
                                                attachment,
                                                this.fileTools.getLockForFile(contentFile)));
        }

        return ListAttachmentArchive.newInstance(attachList);
    }

    /**
     * {@inheritDoc}
     * bTransaction cannot be used in this case, in order to have transaction atomicity,
     * please use getArchiveSaveRunnable() instead.
     *
     * @see AttachmentVersioningStore#saveArchive(XWikiAttachmentArchive, XWikiContext, boolean)
     */
    public void saveArchive(final XWikiAttachmentArchive archive,
                            final XWikiContext context,
                            final boolean bTransaction)
        throws XWikiException
    {
        try {
            this.getArchiveSaveRunnable(archive, context).start();
        } catch (Exception e) {
            if (e instanceof XWikiException) {
                throw (XWikiException) e;
            }
            final Object[] args = {UNKNOWN_NAME, UNKNOWN_NAME};
            if (archive.getAttachment() != null) {
                args[0] = archive.getAttachment().getFilename();
                if (archive.getAttachment().getDoc() != null) {
                    args[1] = archive.getAttachment().getDoc().getFullName();
                }
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                     XWikiException.ERROR_XWIKI_UNKNOWN,
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
     * @return a new StartableTransactionRunnable for saving this attachment archive.
     */
    public StartableTransactionRunnable getArchiveSaveRunnable(final XWikiAttachmentArchive archive,
                                                               final XWikiContext context)
    {
        return new ArchiveSaveRunnable(archive, this.fileTools, this.metaSerializer, context);
    }

    /**
     * {@inheritDoc}
     * bTransaction is ignored by this implementation.
     * If you need to delete an archive inside of a larger transaction,
     * please use getArchiveDeleteRunnable()
     *
     * @see AttachmentVersioningStore#deleteArchive(XWikiAttachment, XWikiContext, boolean)
     */
    public void deleteArchive(final XWikiAttachment attachment,
                              final XWikiContext context,
                              final boolean bTransaction)
        throws XWikiException
    {
        try {
            final XWikiAttachmentArchive archive = this.loadArchive(attachment, context, bTransaction);
            this.getArchiveDeleteRunnable(archive).start();
        } catch (Exception e) {
            if (e instanceof XWikiException) {
                throw (XWikiException) e;
            }
            final Object[] args = {attachment.getFilename(), UNKNOWN_NAME};
            if (attachment.getDoc() != null) {
                args[1] = attachment.getDoc().getFullName();
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                     XWikiException.ERROR_XWIKI_UNKNOWN,
                                     "Exception while deleting attachment archive {0} from document {1}",
                                     e, args);
        }
    }

    /**
     * Get a TransactionRunnable for deleting an attachment archive.
     * this runnable can be run with any transaction including a VoidTransaction.
     * 
     * @param archive The attachment archive to delete.
     * @return a StartableTransactionRunnable for deleting the attachment archive.
     */
    public StartableTransactionRunnable getArchiveDeleteRunnable(final XWikiAttachmentArchive archive)
    {
        return new ArchiveDeleteRunnable(archive, this.fileTools);
    }

    /*--------------------- Nested classes ---------------------*/

    /**
     * A TransactionRunnable for deleting attachment archives.
     * It uses FileDeleteTransactionRunnable so the attachment will either be deleted or fail
     * safely, it should not hang in a halfway state.
     */
    private static class ArchiveDeleteRunnable extends StartableTransactionRunnable
    {
        /**
         * Filesystem storage tools for getting files for each revision of the attachment, it's
         * metadata, and locks for each.
         */
        private final FilesystemStoreTools fileTools;

        /** The attachment which this archive is associated with. */
        private final XWikiAttachment attachment;

        /** The archive to delete. */
        private final XWikiAttachmentArchive archive;

        /**
         * @param archive the attachment archive to save.
         * @param fileTools tools for getting the metadata and versions of the attachment and locks.
         */
        public ArchiveDeleteRunnable(final XWikiAttachmentArchive archive,
                                     final FilesystemStoreTools fileTools)
        {
            if (archive == null) {
                throw new NullPointerException(
                    "Cannot construct ArchiveDeleteRunnable because archive is null.");
            }
            if (fileTools == null) {
                throw new NullPointerException(
                    "Cannot construct ArchiveDeleteRunnable because fileTools is null.");
            }
            this.fileTools = fileTools;
            this.archive = archive;
            this.attachment = archive.getAttachment();
            if (this.attachment == null) {
                throw new IllegalArgumentException(
                    "Cannot delete an archive unless it is associated with an attachment.");
            }
        }

        /**
         * {@inheritDoc}
         * Create FileDeleteTransactionRunnables for each version of the attachment content as well
         * as it's metadata store.
         *
         * @see TransactionRunnable#preRun()
         */
        protected void onPreRun() throws Exception
        {
            final List<File> toDelete = new ArrayList<File>();
            toDelete.add(this.fileTools.metaFileForAttachment(this.attachment));

            final Version[] versions = this.archive.getVersions();
            for (int i = 0; i < versions.length; i++) {
                toDelete.add(this.fileTools.fileForAttachmentVersion(attachment, versions[i].toString()));
            }

            for (File file : toDelete) {
                final TransactionRunnable contentDeleteRunnable =
                    new FileDeleteTransactionRunnable(file,
                                                      this.fileTools.getBackupFile(file),
                                                      this.fileTools.getLockForFile(file));
                contentDeleteRunnable.runIn(this);
            }
        }
    }

    /**
     * A TransactionRunnable for saving attachment archives.
     * It uses a chain of FileSaveTransactionRunnable so the attachment will either be saved or fail
     * safely, it should not hang in a halfway state.
     */
    private static class ArchiveSaveRunnable extends StartableTransactionRunnable
    {
        /** The XWikiAttachmentArchive to save. */
        private final XWikiAttachmentArchive archive;

        /** Filesystem storage tools for getting files for each revision of the attachment. */
        private final FilesystemStoreTools fileTools;

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
        protected void onPreRun() throws Exception
        {
            final Version[] versions = this.archive.getVersions();

            final List<XWikiAttachment> attachmentVersions =
                new ArrayList<XWikiAttachment>(versions.length);

            // Add the content files which need updating and add the attachments to the list.
            for (int i = 0; i < versions.length; i++) {
                final String versionName = versions[i].toString();
                final XWikiAttachment attachVer = archive.getRevision(this.archive.getAttachment(),
                                                                      versionName,
                                                                      this.context);
                attachmentVersions.add(attachVer);

                if (attachVer.isContentDirty()) {
                    // If the content is not dirty then it will not be updated.
                    final File contentFile =
                        this.fileTools.fileForAttachmentVersion(attachVer, versionName);

                    final StreamProvider contentProvider =
                        new AttachmentContentStreamProvider(attachVer, this.context);

                    final TransactionRunnable contentSaveRunnable =
                        new FileSaveTransactionRunnable(contentFile,
                                                        this.fileTools.getTempFile(contentFile),
                                                        this.fileTools.getBackupFile(contentFile),
                                                        this.fileTools.getLockForFile(contentFile),
                                                        contentProvider);

                    contentSaveRunnable.runIn(this);
                }
            }

            // Now do the metadata.
            final File attachMetaFile =
                this.fileTools.metaFileForAttachment(archive.getAttachment());

            final StreamProvider provider =
                new AttachmentListMetadataStreamProvider(this.attachmentListMetaSerilizer,
                                                         attachmentVersions);
            final TransactionRunnable metaSaveRunnable =
                new FileSaveTransactionRunnable(attachMetaFile,
                                                this.fileTools.getTempFile(attachMetaFile),
                                                this.fileTools.getBackupFile(attachMetaFile),
                                                this.fileTools.getLockForFile(attachMetaFile),
                                                provider);

            metaSaveRunnable.runIn(this);
        }
    }

    /**
     * A stream provider based on the metadata for each attachment in a list.
     * Used to save the metadata file for the list of attachments.
     */
    private static class AttachmentListMetadataStreamProvider implements StreamProvider
    {
        /** The serializer for converting the list of attachments into a stream of metadata. */
        private final Serializer<List<XWikiAttachment>> serializer;

        /** The list of attachments to get the stream of metadata from. */
        private final List<XWikiAttachment> attachList;

        /**
         * The Constructor.
         *
         * @param serializer the serializer for converting the list of attachments into a stream of data.
         * @param attachList the list of attachments to serialize.
         */
        public AttachmentListMetadataStreamProvider(final Serializer<List<XWikiAttachment>> serializer,
                                                    final List<XWikiAttachment> attachList)
        {
            this.serializer = serializer;
            this.attachList = attachList;
        }

        /**
         * {@inheritDoc}
         *
         * @see StreamProvider#getStream()
         */
        public InputStream getStream() throws IOException
        {
            return this.serializer.serialize(this.attachList);
        }
    }
}
