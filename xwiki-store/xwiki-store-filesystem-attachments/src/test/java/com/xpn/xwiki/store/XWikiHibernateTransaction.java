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
package org.xwiki.store.hibernate.internal;

import com.xpn.xwiki.store.XWikiHibernateBaseStore;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import org.xwiki.store.Transaction;

/**
 * A Transaction based on XWikiHibernateStore.
 *
 * @version $Id$
 * @since TODO
 */
public class XWikiHibernateTransaction implements Transaction
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
