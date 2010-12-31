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
package org.xwiki.store;

/**
 * A TransactionRunnable is a closure which is meant to run inside of a transaction.
 * The runnable contains a method which will run inside of the transaction and provides hooks 
 * to execute custom code when the transaction succeeded, failed, or completed in any way.
 *
 * @version $Id$
 * @since TODO
 */
public class TransactionRunnable
{
    /**
     * {@inheritDoc}
     *
     * @see AbstractTransactionRunnable#start(Transaction)
     */
    public void start(final Transaction transaction) throws Exception
    {
        boolean transactionOpen = false;
        try {
            this.preRun();
            transactionOpen = true;
            transaction.begin();
            this.run();
            transaction.commit();
            this.onCommit();
        } catch (Exception e) {
            if (transactionOpen) {
                try {
                    transaction.rollback();
                } catch (Exception ee) {
                    // Not much we can do here, failed to rollback.
                    // Throw the original exception anyway.
                    // TODO Log this since it means the storage engine may be in an inconsistant state.
                }
            }
            try {
                this.onRollback();
            } catch (Throwable t) {
                // This exception cannot be thrown reliably so it will be swallowed.
                // TODO Log this since it means the storage engine may be in an inconsistant state.
            }
            throw e;
        } finally {
            try {
                this.onComplete();
            } catch (Throwable t) {
                // This exception cannot be thrown reliably so it will be swallowed.
                // TODO Log this since it means the storage engine may be in an inconsistant state.
            }
        }
    }

    /**
     * This will be run before the transaction is opened.
     *
     * @throws Exception which will cause the execution of
     *         onRollback then onComplete before being thrown up the calling stack.
     */
    protected void preRun() throws Exception
    {
        // By default this will do nothing.
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
