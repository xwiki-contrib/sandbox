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

package org.xwoot.lpbcast.message;

import java.io.Serializable;
import java.util.UUID;

/**
 * Describes a message sent trough the P2P network.
 * 
 * @version $Id$
 */
public class Message implements Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -8107239172395652489L;

    /** The Globally Unique ID of this message. This is also the key by which it is stored in the antiEntropy's log. */
    private Object id;

    /** Value used for message gossip. It will be decremented each time the message is passed on. */
    private int round;

    /** The content of this message. This can vary, depending on the value of the action field. */
    private Object content;

    /** The XWootId of the peer that created this message. */
    private Object originalPeerId;

    /** The action to be applied on this message. */
    private int action;

    /** A random neighbor used to propagate all the members of the P2P Network to each node probabilistically. */
    private Object randNeighbor;

    /** Creates a new Message object. */
    public Message()
    {
        this.id = UUID.randomUUID();
    }

    /**
     * @return the action to be applied on this message. The available actions are defined in {@link LpbCastAPI}.
     */
    public int getAction()
    {
        return this.action;
    }

    /**
     * @param action the action to set.
     * @see #getAction()
     */
    public void setAction(int action)
    {
        this.action = action;
    }

    /**
     * @return the content of this message. This can vary, depending on the value of the action field.
     * @see #getAction()
     */
    public Object getContent()
    {
        return this.content;
    }

    /**
     * @param content the content to set.
     * @see #getContent()
     */
    public void setContent(Object content)
    {
        this.content = content;
    }

    /**
     * @return the Globally Unique ID of this message. This is also the key by which it is stored in antiEntropy's log.
     * @see Guid
     */
    public Object getId()
    {
        return this.id;
    }

    /**
     * @return the XWootId of the peer that created this message.
     */
    public Object getOriginalPeerId()
    {
        return this.originalPeerId;
    }

    /**
     * @param originalPeerId the originalPeerId to set
     * @see #getOriginalPeerId()
     */
    public void setOriginalPeerId(Object originalPeerId)
    {
        this.originalPeerId = originalPeerId;
    }

    /**
     * @return a random neighbor used to propagate all the members of the P2P Network to each node probabilistically.
     */
    public Object getRandNeighbor()
    {
        return this.randNeighbor;
    }

    /**
     * @param randNeighbor the randNeighbor to set.
     * @see #getRandNeighbor()
     */
    public void setRandNeighbor(Object randNeighbor)
    {
        this.randNeighbor = randNeighbor;
    }

    /**
     * @return a value used for message gossip. It will be decremented each time the message is passed on.
     */
    public int getRound()
    {
        return this.round;
    }

    /**
     * @param round the round to set.
     * @see #getRound()
     */
    public void setRound(int round)
    {
        this.round = round;
    }
}
