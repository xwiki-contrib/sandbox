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
package org.xwoot.jxta.message;

import java.io.Serializable;

/**
 * Describes a message sent through the P2P network.
 *
 * @version $Id$
 */
public interface Message extends Serializable
{
    /** Available actions. */
    public enum Action {
        /** Contains a single patch object. */
        BROADCAST_PATCH,
        
        /** Contains an array of the requesting peer's message IDs. */
        ANTI_ENTROPY_REQUEST,
        
        /** Contains a collection of Patches. (TODO: should we go back to messages? Any advantages?) */
        ANTI_ENTROPY_REPLY,
        
        /** Has empty content. */
        STATE_REQUEST,
        
        /** TODO: think this out. Should probably contain the zipped state file. byte[]? */
        STATE_REPLY 
    }
    
    /**
     * @return the action that created this message. The available actions are defined by the {@link Action} enum.
     */
    Action getAction();

    /**
     * @param action the action to set.
     * @see #getAction()
     */
    void setAction(Action action);

    /**
     * @return the content of this message. This can vary, depending on the value of the action field.
     * @see #getAction()
     */
    Object getContent();

    /**
     * @param content the content to set.
     * @see #getContent()
     */
    void setContent(Object content);

    /**
     * @return the Globally Unique ID of this message. This is also the key by which it is stored in antiEntropy's log.
     * @see Guid
     */
    Object getId();

    /**
     * @return the XWootId of the peer that created this message.
     */
    Object getOriginalPeerId();

    /**
     * @param originalPeerId the originalPeerId to set
     * @see #getOriginalPeerId()
     */
    void setOriginalPeerId(Object originalPeerId);

}
