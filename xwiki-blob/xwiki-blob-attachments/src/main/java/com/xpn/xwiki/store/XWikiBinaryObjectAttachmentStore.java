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

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Binary Object based implementation of XWikiAttachmentStoreInterface.
 * 
 * @version $Id$
 */
@Component("blob")
public class XWikiBinaryObjectAttachmentStore implements XWikiAttachmentStoreInterface
{
    /** Number which will be the first 8 bytes of the UUIDs for all attachment contents. */
    private static final long UUID_MOST_SIGNIFICANT = 0xA77AC40000000000L;

    /** The provider of the BinaryObjects to store the attachments in. */
    @Requirement
    private BinaryObjectProvider binProvider;

    /** This store is deferred to if attachment content is unavailable in filesystem or when deleting. */
    @Requirement
    private XWikiAttachmentStoreInterface hibernateAttachStore;

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#saveAttachmentContent(XWikiAttachment, XWikiContext, boolean)
     */
    public void saveAttachmentContent(final XWikiAttachment attachment,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        saveAttachmentContent(attachment, true, context, bTransaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#saveAttachmentContent(XWikiAttachment, boolean, XWikiContext, boolean)
     */
    public void saveAttachmentContent(final XWikiAttachment attachment,
                                      final boolean parentUpdate,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        if (parentUpdate) {
            throw new UnsupportedOperationException("Update of the parent document is not supported by this "
                                                    + "implementation.");
        }

        final XWikiBinaryObjectAttachmentContent content;

        if (!(attachment.getAttachment_content() instanceof XWikiBinaryObjectAttachmentContent)) {
            // If it's a different type of attachment content then we'll copy it over.
            final BinaryObject store = this.binProvider.get();
            store.load(XWikiBinaryObjectAttachmentStore.getAttachmentUUID(attachment));

            content = new XWikiBinaryObjectAttachmentContent(this.binProvider, store);
            content.setContent(attachment.getAttachment_content().getContentInputStream());
            content.setContentDirty(attachment.getAttachment_content().isContentDirty());
        } else {
            content = (XWikiBinaryObjectAttachmentContent) attachment.getAttachment_content();
        }

        boolean inTransaction = bTransaction;
        final XWikiHibernateStore hibernate = context.getContext().getWiki().getHibernateStore();

        boolean contentDirty = content.isContentDirty();

        final String db = context.getDatabase();
        final String attachdb = (attachment.getDoc() == null) ? null : attachment.getDoc().getDatabase();

        // Checkstyle bans anonymous inner classes over 20 lines long precluding the use of TransactionRunnable.
        try {
            if (inTransaction) {
                hibernate.checkHibernate(context);
                inTransaction = hibernate.beginTransaction(context);
            }

            try {
                if (attachdb != null) {
                    context.setDatabase(attachdb);
                }

                // Save the actual attachment content.
                content.save();

                // Update the content archive.
                if (contentDirty) {
                    attachment.updateContentArchive(context);
                }

                // Load the attachment revision history.
                if (attachment.getAttachment_archive() == null) {
                    attachment.loadArchive(context);
                }

                // Save the attachment archive.
                context.getWiki().getAttachmentVersioningStore().saveArchive(attachment.getAttachment_archive(),
                                                                             context, false);

            if (inTransaction) {
                hibernate.endTransaction(context, true);
            }
        } catch (Exception e) {
            try {
                content.setContent(backup.getContent());
            } catch (Exception e) {
                // Failed to copy the content from the backup.
            }
            try {
                if (inTransaction) {
                    hibernate.endTransaction(context, false);
                }
            } catch (Exception ee) {
                // Failed to rollback, throw the original exception anyway.
            }
            Object[] args = {attachment.getFilename(), attachment.getDoc().getFullName()};
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                XWikiException.ERROR_XWIKI_STORE_HIBERNATE_SAVING_ATTACHMENT,
                "Exception while saving attachment {0} of document {1}", e, args);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#saveAttachmentsContent(List, XWikiDocument, boolean, XWikiContext, boolean)
     */
    public void saveAttachmentsContent(final List<XWikiAttachment> attachments,
                                       final XWikiDocument doc,
                                       final boolean bParentUpdate,
                                       final XWikiContext context,
                                       final boolean bTransaction) throws XWikiException
    {
        if (attachments == null) {
            return;
        }
        new TransactionRunnable() {
            public void run() throws XWikiException
                {
                try {
                    for (XWikiAttachment attach : attachments) {
                        this.saveAttachmentContent(attach, false, context, false);
                    }
                    if (bParentUpdate) {
                        context.getWiki().getStore().saveXWikiDoc(doc, context, false);
                    }
                } catch {
                    throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                             XWikiException.ERROR_XWIKI_STORE_HIBERNATE_SAVING_ATTACHMENT,
                                             "Exception while saving attachments", e);
                }
            }
        }.start(bTransaction, context);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#loadAttachmentContent(XWikiAttachment, XWikiContext, boolean)
     */
    public void loadAttachmentContent(final XWikiAttachment attachment,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        try {
            final BinaryObject binObj = this.binProvider.get();
            binObj.load(XWikiBinaryObjectAttachmentStore.getAttachmentUUID(attachment));
            return new XWikiBinaryObjectAttachmentContent(this.binProvider, binObj);
        } catch (Exception e) {
            Object[] args = {attachment.getFilename(), attachment.getDoc().getFullName()};
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                XWikiException.ERROR_XWIKI_STORE_HIBERNATE_LOADING_ATTACHMENT,
                "Exception while loading attachment {0} of document {1}", e, args);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#deleteXWikiAttachment(XWikiAttachment, XWikiContext, boolean)
     */
    public void deleteXWikiAttachment(final XWikiAttachment attachment,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        deleteXWikiAttachment(attachment, true, context, bTransaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#deleteXWikiAttachment(XWikiAttachment, boolean, XWikiContext, boolean)
     */
    public void deleteXWikiAttachment(final XWikiAttachment attachment,
                                      final boolean parentUpdate,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        attachmentStore.
    }

    /**
     * Get a UUID for an attachment.
     * Since attachments have an integer id in the database, this implemetation just prepends some statically
     * set bytes and crafts a UUID.
     * The correct way is to accept whatever UUID the BinaryObject chooses and store that but XWikiAttachment
     * has no way to store a UUID and this is legacy compatability.
     * Constructed from a statically defined 8 bytes, the attachment id # (4 bytes) and 
     * the hashcode of the version (4 bytes).
     * A7:7A:C4:00:00:00:00:00:<Attachment ID#>:<Version Hashcode>
     *
     * @param the attachment to get the UUID for.
     * @return a uuid corrisponding to the Attachment.
     */
    private static UUID getAttachmentUUID(final XWikiAttachment attachment)
    {
        return new UUID(XWikiBinaryObjectAttachmentStore.UUID_MOST_SIGNIFICANT,
                        (attachment.getId() << 32) & ((long) attachment.getVersion().hashCode()));
    }

    private static abstract class TransactionRunnable
    {
        public void start(final boolean doInTransaction, final XWikiContext xcontext) throws XWikiException
        {
            final XWikiHibernateStore hib = xcontext.getWiki().getHibernateStore();
            boolean bTransaction = doInTransaction;
            if (bTransaction) {
                hib.checkHibernate(xcontext);
                bTransaction = hib.beginTransaction(xcontext);
            }
            try {
                this.run();
                if (bTransaction) {
                    hib.endTransaction(xcontext, true);
                }
                this.commit();
            } catch (XWikiException e) {
                try {
                    if (bTransaction) {
                        hib.endTransaction(xcontext, false);
                    }
                } catch (XWikiException ee) {
                    // Not much we can do here, failed to rollback. Throw the original exception instead.
                }
                throw e;
            }
        }

        public abstract void run() throws XWikiException;

        public void commit()
        {
            // This is optional so by default it will do nothing.
        }

        public void endTransaction()
        {
            // This is optional so by default it will do nothing.
        }
    }
}
