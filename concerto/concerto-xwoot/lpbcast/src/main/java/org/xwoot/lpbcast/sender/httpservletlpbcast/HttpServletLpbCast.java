/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.lpbcast.sender.httpservletlpbcast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;
import org.xwoot.lpbcast.neighbors.NeighborsException;
import org.xwoot.lpbcast.neighbors.httpservletneighbors.HttpServletNeighbors;
import org.xwoot.lpbcast.receiver.httpservletreceiver.AbstractHttpServletReceiver;
import org.xwoot.lpbcast.sender.LpbCastAPI;
import org.xwoot.lpbcast.sender.SenderException;

import java.io.File;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

/**
 * Implements a Sender using servlets for communication trough the HTTP protocol.
 * 
 * @version $Id:$
 */
public class HttpServletLpbCast implements LpbCastAPI
{
    /** The servlet used for state transfer. */
    public static final String SEND_STATE_SERVLET = "/sendState.do";

    /** The servet used for anti-enropy. */
    public static final String SEND_AE_DIFF_SERVLET = "/sendAEDiff.do";

    /** The servlet path used for neighbor test. The url of the neighbor requesting this test must also be provided. */
    public static final String SEND_NEIGHBOR_TEST_PATH =
        "/synchronize.do?" + AbstractHttpServletReceiver.NEIGHBOR_TEST_REQUEST_PARAMETER + "=true&url=";

    /** Connected state. */
    public final LpbCastAPI connectedState = new HttpServletLpbCastStateConnected(this);

    /** Disconnected state. */
    public final LpbCastAPI disconnectedState = new HttpServletLpbCastStateDisconnected(this);

    /** Used for logging. */
    protected final Log logger = LogFactory.getLog(this.getClass());

    /**
     * @see #getRound()
     **/
    private int round;

    /**
     * @see #getNeighbors()
     */
    private Neighbors neighbors;

    /**
     * @see #getSiteId()
     */
    private Integer siteId;

    /** The state of the P2P node. This determines the behavior. */
    private LpbCastAPI state;

    /**
     * Creates a new LpbCast object.
     * 
     * @param workingDirPath the working directory where to store neighbors.
     * @param messagesRound the initial message round.
     * @param maxNeighbors the maximum number of neighbors to remember.
     * @param siteId the siteId of this node.
     * @throws HttpServletLpbCastException if problems occur initializing the neighbors manager.
     */
    public HttpServletLpbCast(String workingDirPath, int messagesRound, int maxNeighbors, Integer siteId)
        throws HttpServletLpbCastException
    {
        this.round = messagesRound;
        this.siteId = siteId;
        try {
            this.neighbors = new HttpServletNeighbors(workingDirPath, maxNeighbors, this.siteId);
        } catch (NeighborsException e) {
            throw new HttpServletLpbCastException(this.siteId + " - Problem creating neighbor\n", e);
        }

        this.logger.info(this.siteId + " - LPBCast created.");
        this.state = this.disconnectedState;
    }

    /** {@inheritDoc} */
    public boolean addNeighbor(Object from, Object neighbor)
    {
        return this.state.addNeighbor(from, neighbor);
    }

    /** {@inheritDoc} */
    public void removeNeighbor(Object neighbor) throws HttpServletLpbCastException
    {
        try {
            this.neighbors.removeNeighbor(neighbor);
        } catch (NeighborsException e) {
            throw new HttpServletLpbCastException(this.siteId + " - Problem removing neighbor.\n", e);
        }

    }

    /** {@inheritDoc} */
    public Neighbors getNeighbors()
    {
        return this.neighbors;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Collection getNeighborsList() throws HttpServletLpbCastException
    {
        try {
            return this.neighbors.getNeighborsList();
        } catch (NeighborsException e) {
            throw new HttpServletLpbCastException(this.siteId + " - Problem getting neighbors list.\n", e);
        }
    }

    /** {@inheritDoc} */
    public void clearWorkingDir()
    {
        this.neighbors.clearWorkingDir();
    }

    /** {@inheritDoc} */
    public void connectSender()
    {
        this.state.connectSender();
    }

    /** {@inheritDoc} */
    public void disconnectSender()
    {
        this.state.disconnectSender();
    }

    /** {@inheritDoc} */
    public boolean isSenderConnected()
    {
        return this.state.isSenderConnected();
    }

    /** {@inheritDoc} */
    public void sendTo(Object neighbor, Object toSend) throws SenderException
    {
        this.state.sendTo(neighbor, toSend);
    }

    /** {@inheritDoc} */
    public void gossip(Message message) throws SenderException
    {
        this.state.gossip(message);

    }

    /** {@inheritDoc} */
    public void processSendState(HttpServletResponse response, File stateFile) throws SenderException
    {
        this.state.processSendState(response, stateFile);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public void processSendAE(HttpServletResponse response, Collection diff) throws SenderException
    {
        this.state.processSendAE(response, diff);
    }

    /** {@inheritDoc} */
    public Message getNewMessage(Object originalPeerId, Object content, int action, int r)
    {
        this.logger.debug(this.getSiteId() + " - Creating a new message to send.");

        Message result = new Message();
        result.setAction(action);
        result.setContent(content);
        result.setRound(r);
        result.setOriginalPeerId(originalPeerId);
        try {
            result.setRandNeighbor(this.getNeighbors().getNeighborRandomly());
        } catch (NeighborsException e) {
            result.setRandNeighbor(null);
        }

        return result;
    }

    /** {@inheritDoc} */
    public int getRound()
    {
        return this.round;
    }

    /**
     * @return the siteId of this P2P node.
     */
    public Integer getSiteId()
    {
        return this.siteId;
    }

    /**
     * @param state the new state of this connection.
     */
    protected void setState(LpbCastAPI state)
    {
        this.state = state;
    }
}
