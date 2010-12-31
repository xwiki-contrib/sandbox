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

import java.util.ArrayList;
import java.util.List;

/**
 * A TransactionRunnable which is made up of a bunch of other runnables
 * which need to run in the same transaction.
 *
 * @version $Id$
 * @since TODO
 */
public class ChainingTransactionRunnable extends TransactionRunnable
{
    /** All of the runnables to be run then committed. */
    private final List<TransactionRunnable> allRunnables = new ArrayList<TransactionRunnable>();

    /**
     * Add a TransactionRunnable to this chain.
     *
     * @param runnable the TransactionRunnables to run in this transaction.
     */
    public void add(final TransactionRunnable runnable)
    {
        this.allRunnables.add(runnable);
    }

    /**
     * {@inheritDoc}
     *
     * @see TransactionRunnable#preRun()
     */
    protected void preRun() throws Exception
    {
        for (TransactionRunnable run : this.allRunnables) {
            run.run();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see TransactionRunnable#run()
     */
    protected void run() throws Exception
    {
        for (TransactionRunnable run : this.allRunnables) {
            run.run();
        }
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
                // TODO Log this since it means the storage engine may be in an inconsistant state.
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
                // TODO Log this since it means the storage engine may be in an inconsistant state.
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
                // TODO Log this since it means the storage engine may be in an inconsistant state.
            }
        }
    }
}
