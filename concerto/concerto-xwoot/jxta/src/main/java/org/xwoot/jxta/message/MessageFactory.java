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

/**
 * Simple factory class for creating messages.
 * 
 * @version $Id$
 */
public final class MessageFactory
{
    /** Private constructor to disable utility class instantiation. */
    private MessageFactory()
    {
        // void
    }

    /**
     * @param originalPeerId the sending peer's identification. Must not be null.
     * @param content the content of the message.
     * @param action the action that created this message. Must not be null.
     * @return a new Message instance.
     */
    public static Message createMessage(Object originalPeerId, Object content, Message.Action action)
    {
        if (originalPeerId == null) {
            throw new NullPointerException(
                "originalPeerId is null. Each message must contain information about how to reach the sender.");
        }

        if (action == null) {
            throw new NullPointerException("action is null. Each message must contain the action that created it.");
        }

        return new JxtaMessage(originalPeerId, content, action);
    }
}
