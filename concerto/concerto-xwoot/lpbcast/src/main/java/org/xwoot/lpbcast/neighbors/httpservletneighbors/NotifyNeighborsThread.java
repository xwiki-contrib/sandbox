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

package org.xwoot.lpbcast.neighbors.httpservletneighbors;

import java.net.URL;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.lpbcast.LpbCastException;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.util.NetUtil;

/**
 * Thread used for asynchronously notifying neighbor(s).
 * 
 * @version $Id$
 */
public class NotifyNeighborsThread extends Thread
{
    /** The message to notify the neighbor(s) with. */
    private Object message;

    /** If this is not null, only this neighbor will be notified. */
    private String neighbor;

    /** If neighbor is null, this collection of neighbors will be notified instead. */
    private Neighbors neighbors;

    /** Used for logging. */
    private Log log;

    /**
     * Creates a new and empty instance.
     */
    public NotifyNeighborsThread()
    {
        this.log = LogFactory.getLog(this.getClass());
    }

    /**
     * Create a new instance used to send a message to several neighbors at once.
     * 
     * @param neighbors the neighbors to send the message to.
     * @param message the message to send.
     */
    public NotifyNeighborsThread(Neighbors neighbors, Object message)
    {
        this();
        this.setNeighbors(neighbors);
        this.setMessage(message);
    }

    /**
     * Create a new instance used to send a message to only one neighbor.
     * 
     * @param neighbor the neighbor to send the message to.
     * @param message the message to send.
     */
    public NotifyNeighborsThread(String neighbor, Object message)
    {
        this();
        this.setNeighbor(neighbor);
        this.setMessage(message);
    }

    /**
     * Send the message to a neighbor.
     * 
     * @param targetNeighbor the neighbor to whom to send the message.
     * @throws ServletNeighborsException if problems occur while sending the message.
     * @see {@link NetUtil#sendObjectViaHTTPRequest(URL, Object)}
     * @see {@link #NOTIFY_SERVLET_PATH}
     */
    protected void call(Object targetNeighbor) throws ServletNeighborsException
    {
        this.log.debug("Send message to : " + targetNeighbor);

        String neighborAddress = targetNeighbor + HttpServletNeighbors.NOTIFY_SERVLET_PATH;
        URL neighborUrl = null;
        try {
            neighborUrl = new URL(neighborAddress);
            NetUtil.sendObjectViaHTTPRequest(neighborUrl, this.message);
        } catch (Exception e) {
            throw new ServletNeighborsException(
                this.neighbor + " : Problem calling neighbor " + neighborAddress + "\n", e);
        }
    }

    /**
     * Sends the message to the destination.
     * <p>
     * If the {@link #neighbor} is not set, then the {@link #neighbors} is used as destination and vice versa.
     * <p>
     * If both are null, then the message will not be delivered anywhere.
     * 
     * @see #call(Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void run()
    {
        try {
            if (this.neighbor != null) {
                this.call(this.neighbor);
            } else {
                if (this.neighbors != null) {
                    for (Iterator i = this.neighbors.getNeighborsList().iterator(); i.hasNext();) {
                        Object aNeighbor = i.next();
                        this.call(aNeighbor);
                    }
                }
            }
        } catch (LpbCastException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param neighbors the neighbors to set. If this is null, then the {@link #neighbor} will be used as destination
     *            for the message.
     */
    public void setNeighbors(Neighbors neighbors)
    {
        this.neighbors = neighbors;
    }

    /**
     * @param neighbor the neighbor to set. If this is null, then the {@link #neighbors} will be used as destination for
     *            the message.
     */
    public void setNeighbor(String neighbor)
    {
        this.neighbor = neighbor;
    }

    /**
     * @return the message to send.
     */
    public Object getMessage()
    {
        return this.message;
    }

    /**
     * @param message the message to set.
     */
    public void setMessage(Object message)
    {
        this.message = message;
    }
}
