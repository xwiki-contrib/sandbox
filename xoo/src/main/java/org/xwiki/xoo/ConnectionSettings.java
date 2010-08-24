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

package org.xwiki.xoo;

/**
 * Stores the settings used for connecting to a remote server.
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class ConnectionSettings
{

    /**
     * The Server URL
     */
    private String serverURL = "http://localhost:8080";

    /**
     * User Name
     */
    private String username = "Admin";

    /**
     * The password for the UserName
     */
    private String password = "admin";

    private String wikiURL = "/xwiki";

    private String xmlRpcURL = "/xmlrpc";

    public ConnectionSettings()
    {
    }

    /**
     * Constructs new settings for connecting to the Server
     * 
     * @param serverURL Server's URL
     * @param username The UserName used for login
     * @param password The Password used for login
     */
    public ConnectionSettings(String serverURL, String username, String password)
    {
        this.serverURL = serverURL;
        this.username = username;
        this.password = password;
    }

    /**
     * @return the Server's URL
     */
    public String getServerURL()
    {
        return serverURL;
    }

    /**
     * @param serverURL Sets a URL for the remote server
     */
    public void setServerURL(String serverURL)
    {
        this.serverURL = serverURL;
    }

    /**
     * @return Username for connecting to the remote server
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username Sets a username for the remote server
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @return The Password used for connecting to the remote server
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password Sets a password for the remote server
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @param wikiURL Sets the Wiki installation path relative to the server
     */
    public void setWikiURL(String wikiURL)
    {
        this.wikiURL = wikiURL;
    }

    /**
     * @return the Wiki installation path relative to the server
     */
    public String getWikiURL()
    {
        return wikiURL;
    }

    /**
     * @param xmlRpcURL Sets the XMLRPC installation relative path
     */
    public void setXmlRpcURL(String xmlRpcURL)
    {
        this.xmlRpcURL = xmlRpcURL;
    }

    /**
     * @return the XMLRPC installation relative path
     */
    public String getXmlRpcURL()
    {
        return xmlRpcURL;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return serverURL + " " + username + " " + password;
    }

}
