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
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * XWoot server configuration settings and initialization.
 * 
 * @version $Id$
 */
public class Bootstrap extends HttpServlet
{
    private static final long serialVersionUID = -7533824334342866689L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        this.getServletContext().log("Bootstrap opened.");
        
        try {
            if (XWootSite.getInstance().isStarted()) {
                this.getServletContext().log("Site: " + XWootSite.getInstance().getXWootEngine().getXWootPeerId()
                    + " Bootstrap - instance already started");
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/synchronize.do"));
                return;
            }

            String errors = "";
            String xwikiPropertiesFile =
                request.getSession().getServletContext().getRealPath(XWootSite.XWIKI_PROPERTIES_FILENAME);
            String xwootPropertiesFile =
                request.getSession().getServletContext().getRealPath(XWootSite.XWOOT_PROPERTIES_FILENAME);
          //TODO better properties management 
            String contentManagerPropertiesFile = 
                request.getSession().getServletContext().getRealPath(XWootSite.CONTENT_MANAGER_PROPERTIES_FILENAME);

            // If filled the bootstrap form, process the values and move on if all ok.
            if (request.getParameter("update") != null) {
                this.getServletContext().log("Processing data.");
                
                errors =
                    XWootSite.getInstance().updatePropertiesFiles(request, xwikiPropertiesFile, xwootPropertiesFile);

                // Start the XWoot server if the properties were correctly
                // saved.
                if (StringUtils.isBlank(errors)) {
                    this.getServletContext().log("No errors found.");
                    
                    Properties p_xwiki = XWootSite.getProperties(xwikiPropertiesFile);
                    Properties p_xwoot = XWootSite.getProperties(xwootPropertiesFile);
                    
                    this.getServletContext().log("Bootstrap - starting instance -");
                    XWootSite.getInstance().init((String) p_xwoot.get(XWootSite.XWOOT_SERVER_NAME),
                        (String) p_xwoot.get(XWootSite.XWOOT_WORKING_DIR),
                        (String) p_xwiki.get(XWootSite.XWIKI_ENDPOINT),
                        (String) p_xwiki.get(XWootSite.XWIKI_USERNAME),
                        (String) p_xwiki.get(XWootSite.XWIKI_PASSWORD),
                        contentManagerPropertiesFile);

                    this.getServletContext().log("Site :" + XWootSite.getInstance().getXWootEngine().getXWootPeerId()
                        + " Bootstrap - moving on to network bootstrap -");
                    response
                        .sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapNetwork.do"));
                    return;
                } else {
                    this.getServletContext().log("Errors found.");
                }

                // There are errors, display the bootstrap page again.
                errors = errors.replaceAll("\n", "<br/>");
                request.setAttribute("errors", errors);
            } else {
                this.getServletContext().log("Bootstrap page just opened.");
            }
            
            // If just opened the bootstrap form or an error occurred, init the form fields with default data found in the properties files.
            if (!StringUtils.isBlank(xwikiPropertiesFile) && !StringUtils.isBlank(xwootPropertiesFile)) {
                Properties p_xwiki =
                    XWootSite.getProperties(xwikiPropertiesFile);
                Properties p_xwoot =
                    XWootSite.getProperties(xwootPropertiesFile);
                
                request.setAttribute("xwiki_properties", p_xwiki);
                request.setAttribute("xwoot_properties", p_xwoot);
            }

            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            this.getServletContext().log("Bootstrap failed:\n", e);
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        }
    }
}
