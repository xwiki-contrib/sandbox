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

import java.util.UUID;

/**
 * Implements a message sent through the P2P network.
 * 
 * @version $Id$
 */
public class JxtaMessage implements Message
{

    /** Unique ID used for serialization. */
    private static final long serialVersionUID = 2247154106139644160L;

    /** The Globally Unique ID of this message. This is also the key by which it is stored in the antiEntropy's log. */
    private Object id;

    /** The content of this message. This can vary, depending on the value of the action field. */
    private Object content;

    /** The XWootId of the peer that created this message. */
    private Object originalPeerId;

    /** The action that created this message. */
    private Action action;

    /** Creates a new Message object. */
    public JxtaMessage()
    {
        this.id = UUID.randomUUID();
    }

    /**
     * Creates a new Message object.
     * 
     * @param originalPeerId the creator of this message.
     * @param content the message's content.
     * @param action the action defined by this message.
     * @see #getOriginalPeerId()
     * @see #getContent()
     * @see #getAction()
     */
    public JxtaMessage(Object originalPeerId, Object content, Action action)
    {
        this();
        this.setOriginalPeerId(originalPeerId);
        this.setContent(content);
        this.setAction(action);
    }

    /** {@inheritDoc} **/
    public Action getAction()
    {
        return this.action;
    }

    /** {@inheritDoc} **/
    public void setAction(Action action)
    {
        this.action = action;
    }

    /** {@inheritDoc} **/
    public Object getContent()
    {
        return this.content;
    }

    /** {@inheritDoc} **/
    public void setContent(Object content)
    {
        this.content = content;
    }

    /** {@inheritDoc} **/
    public Object getId()
    {
        return this.id;
    }

    /** {@inheritDoc} **/
    public Object getOriginalPeerId()
    {
        return this.originalPeerId;
    }

    /** {@inheritDoc} **/
    public void setOriginalPeerId(Object originalPeerId)
    {
        this.originalPeerId = originalPeerId;
    }

}
