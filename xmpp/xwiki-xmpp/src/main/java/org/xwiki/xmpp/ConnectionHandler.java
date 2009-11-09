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
 *
 */
package org.xwiki.xmpp;

/**
 * Handles the connections in a different threads. <tt>ConnectionHandler</tt> uses configuration to connect to remote
 * server with {@link org.xwiki.xmpp.ConnectionConfiguration configuration options}.
 * 
 * @author tharindu
 */
public class ConnectionHandler extends Thread
{
    private ConnectionConfiguration config;

    /**
     * Initializes the connection handler thread with given configurations
     * 
     * @param config the configuration used to connect and login to server
     */
    public ConnectionHandler(ConnectionConfiguration config)
    {
        this.config = config;
    }

    public void connect()
    {
        // TODO: implement using smack api
    }

    public void login()
    {
        // TODO: implement using smack api
    }

    public void disconnect()
    {
        // TODO: implement using smack api
    }

    public void run()
    {

    }

}
