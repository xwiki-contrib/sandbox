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
package org.xwiki.portlet;

import javax.portlet.PortletPreferences;

/**
 * Extends {@link DispatchPortlet} with custom implementation for XWiki.
 * 
 * @version $Id$
 */
public class XWikiDispatchPortlet extends DispatchPortlet
{
    /**
     * The name of the preference holding the servlet path used to compute the default dispatch URL.
     */
    public static final String PREFERENCE_SERVLET_PATH = "servletPath";

    /**
     * The name of the preference holding the path info used to compute the default dispatch URL.
     */
    public static final String PREFERENCE_PATH_INFO = "pathInfo";

    /**
     * {@inheritDoc}
     * 
     * @see DispatchPortlet#getDefaultDispatchURL(PortletPreferences)
     */
    @Override
    protected String getDefaultDispatchURL(PortletPreferences preferences)
    {
        return preferences.getValue(PREFERENCE_SERVLET_PATH, "/bin")
            + preferences.getValue(PREFERENCE_PATH_INFO, "/view/Main/");
    }
}
