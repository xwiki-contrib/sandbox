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
package org.xwoot.xwootApp.web.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

public class ContentProviderDiagnostics extends HttpServlet
{
    private static final long serialVersionUID = -3266228974643536434L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        XWootSite site = XWootSite.getInstance();
        XWootAPI xwootAPI = site.getXWootEngine();
        if (xwootAPI == null) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrap.do"));
            return;
        }

        XWootContentProviderInterface xwcp = xwootAPI.getContentProvider();
        request.setAttribute("content_provider", xwcp);
        request.setAttribute("config", xwcp.getConfiguration());
        request.setAttribute("entries", xwcp.getEntries(null, 0, -1));

        request.getRequestDispatcher("/pages/ContentProviderDiagnostics.jsp").forward(request, response);

    }

}
