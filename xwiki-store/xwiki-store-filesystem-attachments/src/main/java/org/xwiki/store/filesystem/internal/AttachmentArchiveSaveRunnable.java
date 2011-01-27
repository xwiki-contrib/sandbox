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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiAttachmentArchive;
import com.xpn.xwiki.XWikiContext;
import org.suigeneris.jrcs.rcs.Version;
import org.xwiki.store.filesystem.internal.FilesystemStoreTools;
import org.xwiki.store.filesystem.internal.AttachmentFileProvider;
import org.xwiki.store.serialization.Serializer;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.FileSaveTransactionRunnable;
import org.xwiki.store.StreamProvider;


/**
 * A TransactionRunnable for saving attachment archives.
 * It uses a chain of FileSaveTransactionRunnable so the attachment will either be saved or fail
 * safely, it should not hang in a halfway state.
 * 
 * @version $Id$
 * @since TODO
 */
public class AttachmentArchiveSaveRunnable extends StartableTransactionRunnable
{
    /** The XWikiAttachmentArchive to save. */
    private final XWikiAttachmentArchive archive;

    /** Filesystem storage tools for getting files for each revision of the attachment. */
    private final FilesystemStoreTools fileTools;

    /** Used to get the files for storing each version of the attachment. */
    private final AttachmentFileProvider provider;

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
     * @param provider the means to get the files to store each version of the attachment.
     * @param serializer an attachment list metadata serializer for serializing the metadata of each
     *                   version of the attachment.
     * @param context the XWikiContext used to get the revisions of the attachment.
     */
    public AttachmentArchiveSaveRunnable(final XWikiAttachmentArchive archive,
                                         final FilesystemStoreTools fileTools,
                                         final AttachmentFileProvider provider,
                                         final Serializer<List<XWikiAttachment>> serializer,
                                         final XWikiContext context)
    {
        this.archive = archive;
        this.fileTools = fileTools;
        this.provider = provider;
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
                    this.provider.getAttachmentVersionContentFile(versionName);

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
            this.provider.getAttachmentVersioningMetaFile();

        final StreamProvider metaProvider =
            new AttachmentListMetadataStreamProvider(this.attachmentListMetaSerilizer,
                                                     attachmentVersions);
        final TransactionRunnable metaSaveRunnable =
            new FileSaveTransactionRunnable(attachMetaFile,
                                            this.fileTools.getTempFile(attachMetaFile),
                                            this.fileTools.getBackupFile(attachMetaFile),
                                            this.fileTools.getLockForFile(attachMetaFile),
                                            metaProvider);

        metaSaveRunnable.runIn(this);
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
