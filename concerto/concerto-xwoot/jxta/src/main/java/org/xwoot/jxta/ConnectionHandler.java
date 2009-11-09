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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import net.jxta.socket.JxtaServerSocket;

/**
 * Daemon thread waiting for connections and spawning a new {@link ConnectionThread} for each
 * connection.
 * 
 * @version $Id:$
 */
class ConnectionHandler extends Thread
{
    JxtaServerSocket serverSocket;

    DirectMessageReceiver receiver;

    ConnectionHandler(JxtaServerSocket serverSocket, DirectMessageReceiver receiver)
    {
        super("DirectCommunication:ConnectionHandler");
        this.serverSocket = serverSocket;
        this.receiver = receiver;
    }

    /** {@inheritDoc} **/
    @Override
    public void run()
    {
        while (true) {
            try {
                Socket socket = this.serverSocket.accept();
                socket.setSoTimeout(JxtaPeer.WAIT_INTERVAL_FOR_DIRECT_COMMUNICATION_CONNECTIONS);
                
                new ConnectionThread(socket, this.receiver).start();
            } catch (SocketException closed) {
                // FIXME: can we use JxtaPeer.this.logger instead?

                if (this.receiver.getLog() != null) {
                    this.receiver.getLog().debug(
                        "Socket server got closed. Stopping this ConnectionHandler thread.",
                        closed);
                }

                return;
            } catch (IOException e) {
                this.receiver.getLog()
                    .error("Error: Failed to accept connection from client.", e);
            }
        }
    }
}
