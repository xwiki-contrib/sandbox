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
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.store.filesystem.internal.FilesystemStoreTools;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.FilesystemAttachmentContent;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiAttachmentContent;
import com.xpn.xwiki.doc.XWikiDocument;
import org.apache.commons.io.IOUtils;

/**
 * Filesystem based implementation of XWikiAttachmentStoreInterface.
 * 
 * @version $Id$
 */
@Component("file")
public class FilesystemAttachmentStore implements XWikiAttachmentStoreInterface
{
    /**
     * This store is deferred to if attachment content is unavailable in filesystem or when deleting.
     * Also when new attachment files are detected, corresponding attachments will be created.
     */
    @Requirement
    private XWikiAttachmentStoreInterface hibernateAttachStore;

    /**
     * Tools for getting files to store given content in.
     */
    @Requirement
    private FilesystemStoreTools fileTools;

    /**
     * {@inheritDoc}
     * This implementation cannot operate in a larger transaction so it starts a new transaction no matter
     * whether bTransaction is true or false.
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#saveAttachmentContent(
     *          XWikiAttachment, XWikiContext, boolean)
     */
    public void saveAttachmentContent(final XWikiAttachment attachment,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        this.saveAttachmentContent(attachment, true, context, bTransaction);
    }

    /**
     * {@inheritDoc}
     * This implementation cannot operate in a larger transaction so it starts a new transaction no matter
     * whether bTransaction is true or false.
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#saveAttachmentContent(
     *          XWikiAttachment, boolean, XWikiContext, boolean)
     */
    public void saveAttachmentContent(final XWikiAttachment attachment,
                                      final boolean updateDocument,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        final Transaction trans = new XWikiHibernateTransaction(context);
        try {
            this.runnableToSaveAttachmentContent(attachment, updateDocument, context).start(trans);
        } catch (Exception e) {
            if (e instanceof XWikiException) {
                throw (XWikiException) e;
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                     XWikiException.ERROR_XWIKI_STORE_HIBERNATE_SAVING_ATTACHMENT,
                                     "Exception while saving attachment.", e);
        }
    }

    /**
     * @param attachment the XWikiAttachment whose content should be saved.
     * @param updateDocument whether or not to update the document at the same time.
     * @param context the XWikiContext for the request.
     * @return a TransactionRunnable for saving the attachment content.
     */
    private TransactionRunnable runnableToSaveAttachmentContent(final XWikiAttachment attachment,
                                                                final boolean updateDocument,
                                                                final XWikiContext context)
    {
        final XWikiAttachmentContent content = attachment.getAttachment_content();

        if (content == null || (!content.isContentDirty() && !attachment.isMetaDataDirty())) {
            // If content has not been modified then there's nothing to save.
            return new VoidTransactionRunnable();
        }

        // This is the permanent location where the attachment will go.
        final File attachFile = this.fileTools.fileForAttachment(attachment);

        // The current file will be renamed to this file while the transaction runs,
        // if it fails then this file will be renamed back.
        final File backupFile = this.fileTools.getBackupFile(attachFile);

        final ReadWriteLock lock = this.fileTools.getLockForFile(attachFile);

        return new AttachmentSaveTransactionRunnable(attachment,
                                                     updateDocument,
                                                     context,
                                                     attachFile,
                                                     backupFile,
                                                     lock);
    }

    /**
     * A TransactionRunnable for saving an attachment.
     */
    private class AttachmentSaveTransactionRunnable extends TransactionRunnable
    {
        /** The XWikiAttachment whose content should be saved. */
        private final XWikiAttachment attachment;

        /** Whether or not to update the document at the same time. */
        private final boolean updateDocument;

        /** The XWikiContext for the request. */
        private final XWikiContext context;

        /** The File to store the attachment in. */
        private final File attachFile;

        /** The File to backup the content of the existing attachment in. */
        private final File backupFile;

        /** This Lock will be locked while the attachment file is being written to. */
        private final ReadWriteLock lock;

        /**
         * Construct a TransactionRunnable for saving the attachment content.
         *
         * @param attachment the XWikiAttachment whose content should be saved.
         * @param updateDocument whether or not to update the document at the same time.
         * @param context the XWikiContext for the request.
         * @param attachFile the File to store the attachment in.
         * @param backupFile the File to backup the content of the existing attachment in.
         * @param lock this Lock will be locked while the attachment file is being written to.
         */
        public AttachmentSaveTransactionRunnable(final XWikiAttachment attachment,
                                                 final boolean updateDocument,
                                                 final XWikiContext context,
                                                 final File attachFile,
                                                 final File backupFile,
                                                 final ReadWriteLock lock)
        {
            this.attachment = attachment;
            this.updateDocument = updateDocument;
            this.context = context;
            this.attachFile = attachFile;
            this.backupFile = backupFile;
            this.lock = lock;
        }

        /**
         * This will run in the transaction.
         *
         * @throws Exception if an exception is thrown copying the file,
         *         saving the attachment version, or updating the document which contains the attachment.
         */
        protected void run() throws Exception
        {
            // Update the archive of the attachment.
            // This must happen before the content is saved since it depends on the
            // old version of the attachment content.
            this.attachment.updateContentArchive(context);

            // Lock the attachment file so it won't be read while it's being written to.
            this.lock.writeLock().lock();

            // Move the current attachment file to a backup location.
            if (this.attachFile.exists()) {
                // If this fails, there isn't a lot which can be done, simply ignore it.
                this.attachFile.renameTo(backupFile);
            }

            if (!this.attachFile.getParentFile().exists() && !this.attachFile.getParentFile().mkdirs()) {
                throw new IOException("Could not make directory tree to place file in. "
                                      + "Do you have permission to write to ["
                                      + this.attachFile.getAbsolutePath() + "] ?");
            }

            // Copy the file.
            OutputStream out = null;
            try {
                out = new FileOutputStream(this.attachFile);
                IOUtils.copy(this.attachment.getContentInputStream(this.context), out);
            } finally {
                IOUtils.closeQuietly(out);
            }

            // Persist the attachment archive.
            context.getWiki().getAttachmentVersioningStore().saveArchive(
                this.attachment.getAttachment_archive(), this.context, false);

            // If the parent document is to be saved as well, save it.
            if (this.updateDocument) {
                this.context.getWiki().getStore().saveXWikiDoc(this.attachment.getDoc(),
                                                               this.context, false);
            }
        }

        /** This will happen if the transaction fails. */
        protected void onRollback()
        {
            try {
                if (this.attachFile.exists()) {
                    this.attachFile.delete();
                }
            } finally {
                this.backupFile.renameTo(this.attachFile);
            }
        }

        /** This will run if the transaction succeeds. */
        protected void onCommit()
        {
            if (this.backupFile.exists()) {
                this.backupFile.delete();
            }
        }
    }

    /**
     * {@inheritDoc}
     * This implementation cannot operate in a larger transaction so it starts a new transaction no matter
     * whether bTransaction is true or false.
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#saveAttachmentsContent(
     *          List, XWikiDocument, boolean, XWikiContext, boolean)
     */
    public void saveAttachmentsContent(final List<XWikiAttachment> attachments,
                                       final XWikiDocument doc,
                                       final boolean updateDocument,
                                       final XWikiContext context,
                                       final boolean bTransaction) throws XWikiException
    {
        if (attachments == null || attachments.size() == 0) {
            return;
        }

        final ChainingTransactionRunnable chain = new ChainingTransactionRunnable();

        try {
            chain.start(new XWikiHibernateTransaction(context));

            for (XWikiAttachment attach : attachments) {
                chain.run(this.runnableToSaveAttachmentContent(attach, false, context));
            }

            // Save the parent document only once.
            if (updateDocument) {
                context.getWiki().getStore().saveXWikiDoc(doc, context, false);
            }

            chain.commit();
        } catch (Exception e) {
            try {
                chain.rollback();
            } catch (Throwable t) {
                // This should not happen but must not squash the original exception.
            }

            if (e instanceof XWikiException) {
                throw (XWikiException) e;
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_STORE,
                                     XWikiException.ERROR_XWIKI_STORE_HIBERNATE_SAVING_ATTACHMENT,
                                     "Exception while saving attachments", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#loadAttachmentContent(
     *          XWikiAttachment, XWikiContext, boolean)
     */
    public void loadAttachmentContent(final XWikiAttachment attachment,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        final File attachFile = this.fileTools.fileForAttachment(attachment);
        if (attachFile.exists()) {
            attachment.setAttachment_content(
                new FilesystemAttachmentContent(attachFile,
                                                attachment,
                                                this.fileTools.getLockForFile(attachFile)));
            return;
        }

        // If there is no attachment file then try loading from the Hibernate attachment store.
        this.hibernateAttachStore.loadAttachmentContent(attachment, context, bTransaction);

        // Then save it as a a file so it will be ported over.
        this.saveAttachmentContent(attachment, false, context, true);

        // Not deleting content from the old attachment store in order to be as safe as possible.
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#deleteXWikiAttachment(
     *          XWikiAttachment, XWikiContext, boolean)
     */
    public void deleteXWikiAttachment(final XWikiAttachment attachment,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        this.deleteXWikiAttachment(attachment, true, context, bTransaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#deleteXWikiAttachment(
     *          XWikiAttachment, boolean, XWikiContext, boolean)
     */
    public void deleteXWikiAttachment(final XWikiAttachment attachment,
                                      final boolean parentUpdate,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        final File attachFile = this.fileTools.fileForAttachment(attachment);
        final ReadWriteLock lock = this.fileTools.getLockForFile(attachFile);
        try {
            lock.writeLock().lock();
            if (attachFile.exists()) {
                attachFile.delete();
            }

            // TODO Only delete if it is contained in the database.
            // If the attachment is also stored in the database, delete that one as well.
            this.hibernateAttachStore.deleteXWikiAttachment(attachment,
                                                            parentUpdate,
                                                            context,
                                                            bTransaction);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     * This implementation does nothing.
     *
     * @see com.xpn.xwiki.store.XWikiAttachmentStoreInterface#cleanUp(XWikiContext)
     */
    public void cleanUp(XWikiContext context)
    {
        // Do nothing.
    }

    /* ---------------------------- Nested Classes. ---------------------------- */

    /**
     * A TransactionRunnable which is made up of a bunch of other runnables
     * which need to run in the same transaction.
     */
    private static class ChainingTransactionRunnable extends TransactionRunnable
    {
        /** Hold the transaction while the runnable runs. */
        private Transaction transaction;

        /** All of the runnables to be run then committed. */
        private List<TransactionRunnable> allRunnables = new ArrayList<TransactionRunnable>();

        /**
         * {@inheritDoc}
         *
         * @see TransactionRunnable#start(Transaction)
         */
        public void start(final Transaction transaction) throws Exception
        {
            this.transaction = transaction;
            transaction.begin();
        }

        /**
         * Run a TransactionRunnable in this transaction.
         *
         * @param runnable the TransactioRunnable to run in this transaction.
         * @throws Exception whatever exception is thrown by the passed runnable  
         */
        public void run(final TransactionRunnable runnable) throws Exception
        {
            if (this.transaction == null) {
                throw new IllegalStateException("Trying to run TransactionRunnables before the transaction"
                                                + " has been started");
            }
            try {
                this.allRunnables.add(runnable);
                runnable.run();
            } catch (Exception e) {
                this.rollback();
                throw e;
            }
        }

        /**
         * Commit the transaction.
         */
        public void commit()
        {
            try {
                this.transaction.commit();
            } catch (Exception e) {
                this.rollback();
                return;
            }
            this.onCommit();
            this.onComplete();
        }

        /**
         * Rollback the transaction.
         */
        public void rollback()
        {
            try {
                this.transaction.rollback();
            } catch (Exception e) {
                // The database is in an inconsistent state, this should be ERROR logged.
            }
            this.onRollback();
            this.onComplete();
        }

        /**
         * {@inheritDoc}
         *
         * @see TransactionRunnable#onCommit()
         */
        protected void onCommit()
        {
            for (TransactionRunnable run : this.allRunnables) {
                try {
                    run.onCommit();
                } catch (Throwable t) {
                    // onCommit should not throw anything.
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see TransactionRunnable#onRollback()
         */
        protected void onRollback()
        {
            for (TransactionRunnable run : this.allRunnables) {
                try {
                    run.onRollback();
                } catch (Throwable t) {
                    // onRollback should not throw anything.
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see TransactionRunnable#onComplete()
         */
        protected void onComplete()
        {
            for (TransactionRunnable run : this.allRunnables) {
                try {
                    run.onComplete();
                } catch (Throwable t) {
                    // onComplete should not throw anything.
                }
            }
        }
    }

    /**
     * A TransactionRunnable which does nothing.
     */
    private static class VoidTransactionRunnable extends TransactionRunnable
    {
        /**
         * {@inheritDoc}
         *
         * @see TransactionRunnable#start(Transaction)
         */
        public void start(final Transaction transaction)
        {
            // No op. Hence why it's void.
        }
    }

    /**
     * A Transaction based on XWikiHibernateStore.
     */
    private static class XWikiHibernateTransaction implements Transaction
    {
        /** The storage engine. */
        private final XWikiHibernateBaseStore store;

        /** The XWikiContext associated with the request which started this Transaction. */
        private final XWikiContext context;

        /**
         * True if the transaction should be ended when finished.
         * This will only be false if the transaction could not be started because another transaction
         * was already open and associated with the same XWikiContext.
         */
        private boolean shouldCloseTransaction;

        /**
         * The Constructor.
         *
         * @param context the XWikiContext associated with the request which started this Transaction.
         */
        public XWikiHibernateTransaction(final XWikiContext context)
        {
            this.store = context.getWiki().getHibernateStore();
            this.context = context;
        }

        /**
         * {@inheritDoc}
         *
         * @see Transaction#begin()
         */
        public void begin() throws XWikiException
        {
            this.store.checkHibernate(this.context);
            this.shouldCloseTransaction = this.store.beginTransaction(this.context);
        }

        /**
         * {@inheritDoc}
         *
         * @see Transaction#commit()
         */
        public void commit()
        {
            if (this.shouldCloseTransaction) {
                this.store.endTransaction(this.context, true);
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see Transaction#rollback()
         */
        public void rollback()
        {
            if (this.shouldCloseTransaction) {
                this.store.endTransaction(this.context, false);
            }
        }
    }

    /**
     * A Transaction represents an atomic unit of work on the storage engine.
     * Implementations may use whatever type of storage they like as long as it supports
     * beginning a transaction, and committing or rolling back that transaction.
     */
    private static interface Transaction
    {
        /**
         * Start the transaction.
         * Prepare the storage engine to handle a transaction.
         *
         * @throws Exception if something goes wrong with the storage engine.
         */
        void begin() throws Exception;

        /**
         * Commit the transaction.
         * Save the work which was done during this transaction.
         *
         * @throws Exception if something goes wrong with the storage.
         */
        void commit() throws Exception;

        /**
         * Rollback the transaction.
         * Return the storage engine to the state it was before the transaction began.
         *
         * @throws Exception if something goes wrong with the storage.
         */
        void rollback() throws Exception;
    }


    /**
     * A TransactionRunnable is a closure which is meant to run inside of a transaction.
     * The runnable contains a method which will run inside of the transaction and provides hooks 
     * to execute custom code when the transaction succeeded, failed, or completed in any way.
     */
    private static class TransactionRunnable
    {
        /**
         * This is called to start a transaction which will run this runnable.
         *
         * @param transaction a transaction which this runnable should run in.
         * @throws Exception if the runnable throws an Exception or if something else goes wrong.
         */
        public void start(final Transaction transaction) throws Exception
        {
            try {
                transaction.begin();
                this.run();
                transaction.commit();
                this.onCommit();
            } catch (Exception e) {
                try {
                    transaction.rollback();
                } catch (Exception ee) {
                    // Not much we can do here, failed to rollback. Throw the original exception anyway.
                }
                try {
                    this.onRollback();
                } catch (Throwable t) {
                    // This exception cannot be thrown reliably so it will be swallowed.
                }
                throw e;
            } finally {
                try {
                    this.onComplete();
                } catch (Throwable t) {
                    // This exception cannot be thrown reliably so it will be swallowed.
                }
            }
        }

        /**
         * This will be run inside of a database transaction.
         *
         * @throws Exception which will cause a rollback of the transaction and then execution of
         *         onRollback then onComplete before being thrown up the calling stack.
         */
        protected void run() throws Exception
        {
            // By default this will do nothing.
        }

        /**
         * This will be run if the transaction succeeds.
         * This function should not throw any exceptions, if it does they will disappear.
         */
        protected void onCommit()
        {
            // By default this will do nothing.
        }

        /**
         * This will run if the transaction fails.
         * This function should not throw any exceptions, if it does they will disappear.
         */
        protected void onRollback()
        {
            // By default this will do nothing.
        }

        /**
         * This will be run when after onCommit or onRollback no matter the outcome.
         * This function should not throw any exceptions, if it does they will disappear.
         */
        protected void onComplete()
        {
            // By default this will do nothing.
        }
    }
}
