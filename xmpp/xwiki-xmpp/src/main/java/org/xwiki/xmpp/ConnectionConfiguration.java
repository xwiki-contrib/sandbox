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
 * Configuration to use while establishing the connection to the server. This class encapsulates all the required
 * information to use when connecting and logging into xmpp server.
 * 
 * @author tharindu
 */
public class ConnectionConfiguration
{
    private String serviceName;

    private String host;

    private int port;

    private String username;

    private String password;

    private String resource;

    /**
     * Returns the server name of the target server.
     * 
     * @return the server name of the target server.
     */
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     * Sets the service name of the target server.
     * 
     * @param serviceName the server name of the target server.
     */
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    /**
     * Returns the host to use when establishing the connection. The host and port to use might have been resolved by a
     * DNS lookup as specified by the XMPP spec (and therefore may not match the {@link #getServiceName service name}.
     * 
     * @return the host to use when establishing the connection.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the host to use when establishing the connection.
     * 
     * @param host the host
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * Returns the port to use when establishing the connection.
     * 
     * @return the port to use when establishing the connection.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the port to use when establishing the connection
     * 
     * @param port
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Returns the username used to login to the server.
     * 
     * @return the username used to login to the server.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Sets the username used to login to the server.
     * 
     * @return the username used to login to the server.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Returns the password used to login to the server.
     * 
     * @return the password used to login to the server.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password used to login to the server.
     * 
     * @return the password used to login to the server.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns the resource used to login to the server.
     * 
     * @return the resource used to login to the server.
     */
    public String getResource()
    {
        return resource;
    }

    /**
     * Sets the resource used to login to the server.
     * 
     * @return the resource used to login to the server.
     */
    public void setResource(String resource)
    {
        this.resource = resource;
    }

}
