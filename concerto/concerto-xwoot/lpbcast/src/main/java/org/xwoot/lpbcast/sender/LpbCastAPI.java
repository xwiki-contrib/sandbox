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

package org.xwoot.lpbcast.sender;

import java.io.File;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.xwoot.lpbcast.message.Message;
import org.xwoot.lpbcast.neighbors.Neighbors;

/**
 * Defines the functionality of a Sender in the P2P Network.
 * 
 * @version $Id$
 */
public interface LpbCastAPI
{
    /**
     * Denotes the fact that the content of a message is a collection of messages, each having a patch as content. Each
     * of the messages in the collection will be logged in the antiEntropy's log. Each patch will be treated.
     */
    int LOG_OBJECT = 0;

    /**
     * Denotes the fact that the content of a message is a patch. The message will be logged and the patch will be
     * applied. After this, the message will be gossiped(broadcasted) throughout the network.
     */
    int LOG_AND_GOSSIP_OBJECT = 1;

    /**
     * Denotes the fact that the message asks for an antiEntropy. The content of the message is an array of message IDs
     * representing the log of the woot node requesting antiEntropy.
     */
    int ANTI_ENTROPY = 2;

    /**
     * @param from if this is not null, a neighbor test will be performed and this value will be provided as the node
     *            that requested the neighbor test. <b>Note:</b> This must not be equal to the neighbor that is being
     *            added because a node does not need itself as neighbor. Use null to add forcefully.
     * @param neighbor the neighbor to add to the list of known neighbors.
     * @return true if the neighbor was added successfully, false otherwise.
     */
    boolean addNeighbor(Object from, Object neighbor);

    /**
     * @param neighbor the neighbor to remove from the list of known neighbors.
     * @throws SenderException if problems occur while removing the neighbor.
     */
    void removeNeighbor(Object neighbor) throws SenderException;

    /**
     * @return the known neighbors.
     */
    Neighbors getNeighbors();

    /**
     * @return a collection of known neighbors.
     * @throws SenderException if problems with the neighbors occur.
     * @see Neighbors#getNeighborsList()
     */
    @SuppressWarnings("unchecked")
    Collection getNeighborsList() throws SenderException;

    /**
     * @see Neighbors#clearWorkingDir()
     */
    void clearWorkingDir();

    /**
     * Connects the sender to the P2P network.
     */
    void connectSender();

    /**
     * Disconnects the sender from the P2P network.
     */
    void disconnectSender();

    /**
     * @return true if the sender is connected to the P2P network, false otherwise.
     */
    boolean isSenderConnected();

    /**
     * @param to the neighbor to send to.
     * @param toSend the message to send.
     * @throws SenderException if problems occur sending the message.
     * @see {@link Neighbors#notifyNeighbor(Object, Object)}
     */
    void sendTo(Object to, Object toSend) throws SenderException;

    /**
     * Gossip(broadcast) a message through the P2P network.
     * 
     * @param message the message to gossip.
     * @throws SenderException if problems occur while gossiping the message.
     */
    void gossip(Message message) throws SenderException;

    /**
     * Write a state file to a HttpServletResponse object.
     * 
     * @param response the response object where the contents of the state file will be written.
     * @param state the location of the state file that needs to be sent.
     * @throws SenderException if problems occur while writing to the response object occur.
     * @see org.xwoot.wootEngine.WootEngine.getState()
     */
    void processSendState(HttpServletResponse response, File state) throws SenderException;

    /**
     * Write the result of an antiEntropy answer to a HttpServletResponse object.
     * 
     * @param response the response object where the answer to the antiEntropy request will be written.
     * @param diff the message diff resulting from a previous antiEntropy answer.
     * @throws SenderException if problems occur while writing to the response object.
     */
    @SuppressWarnings("unchecked")
    void processSendAE(HttpServletResponse response, Collection diff) throws SenderException;

    /**
     * Creates a new Message and sets the new message's random neighbor to one of the known neighbors managed by this
     * Sender.
     * 
     * @param originalPeerId see {@link Message#getOriginalPeerId()}
     * @param content see {@link Message#getContent()}
     * @param action see {@link Message#getAction()}
     * @param round see {@link Message#getRound()}
     * @return a new message.
     * @see #getNeighbors()
     * @see Neighbors#getNeighborRandomly()
     * @see Message#getRandNeighbor()
     */
    Message getNewMessage(Object originalPeerId, Object content, int action, int round);

    /**
     * @return the number of rounds a message will be gossiped throughout the P2P network. This will be initialized
     *         during the bootstrap process.
     */
    int getRound();

}
