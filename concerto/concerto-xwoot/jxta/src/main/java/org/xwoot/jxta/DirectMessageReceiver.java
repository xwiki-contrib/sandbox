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
package org.xwoot.jxta;

import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;

/**
 * Describes the structure of a receiver for direct messages from other peer group members.
 * 
 * @version $Id$
 */
public interface DirectMessageReceiver
{
    /**
     * Receive a direct message from a peer.
     * 
     * @param message the received message.
     * @param oos the ObjectOutputStream that can be used to immediately reply.
     */
    void receiveDirectMessage(Object message, ObjectOutputStream oos);

    /** @return the log object the connection threads can use to log events. */
    Log getLog();
}
