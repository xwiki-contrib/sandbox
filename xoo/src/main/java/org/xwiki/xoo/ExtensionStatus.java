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

import java.util.Hashtable;

/**
 * Stores the context of the extension
 * 
 * @version $Id$
 * @since 1.0 M
 */
public class ExtensionStatus
{

    public static final short NEW_NAVBAR = 0;

    public static final short OLD_NAVBAR = 1;

    /**
     * Connection status
     */
    private boolean loggedIn = false;

    private short navBarStatus = NEW_NAVBAR;

    /**
     * 
     */
    private Hashtable<String, Object> opened;

    private String sCurrentDocURL = null;

    public ExtensionStatus()
    {
        opened = new Hashtable<String, Object>();
    }

    /**
     * Sets the connection status
     * 
     * @param loggedIn true if the client is logged in , false otherwise
     */
    public void setLoginStatus(boolean loggedIn)
    {
        this.loggedIn = loggedIn;
    }

    /**
     * Sets the navigation bar status.
     * 
     * @param status NEW_NAVBAR or OLD_NAVBAR
     */
    public void setNavBarStatus(short status)
    {
        navBarStatus = status;
    }

    /**
     * Returns the navigation bar status.
     * 
     * @return NEW_NAVBAR if a new instance of the navigation bar was created, OLD_NAVBAR otherwise
     */
    public short getNavBarStatus()
    {
        return navBarStatus;
    }

    /**
     * Gets the connection status
     * 
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    /**
     * Mark the resource from the specified URL as opened
     * 
     * @param url The URL of the resource
     * @param ob The resource
     */
    public void addOpenedResource(String url, Object ob)
    {
        opened.put(url, ob);
    }

    /**
     * Unmark the resource
     * 
     * @param url The URL that identifies the resource
     */
    public void removeOpenedResource(String url)
    {
        opened.remove(url);
    }

    /**
     * @param url The URL of the resource
     * @return the opened resource which has the input URL
     */
    public Object getOpenedResource(String url)
    {
        return opened.get(url);
    }

    /**
     * @param url The resource URL
     * @return true if the resource is opened, false otherwise
     */
    public boolean hasOpenedResource(String url)
    {
        return opened.containsKey(url);
    }

    /**
     * @return all the opened resources
     */
    public Hashtable<String, Object> getOpenedResources()
    {
        return opened;
    }

    /**
     * @param docURL Specifies the current opened and focused Document
     */
    public void setCurrentDocURL(String docURL)
    {
        this.sCurrentDocURL = docURL;
    }

    /**
     * @return the current opened and focused Document
     */
    public String getCurrentDocURL()
    {
        return sCurrentDocURL;
    }

}
