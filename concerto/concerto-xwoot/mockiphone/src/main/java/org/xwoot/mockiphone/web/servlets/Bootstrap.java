/**
 * 
 *        -- class header / Copyright (C) 2008  100 % INRIA / LGPL v2.1 --
 * 
 *  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  Copyright (C) 2008  100 % INRIA
 *  Authors :
 *                       
 *                       Gerome Canals
 *                     Nabil Hachicha
 *                     Gerald Hoster
 *                     Florent Jouille
 *                     Julien Maire
 *                     Pascal Molli
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  INRIA disclaims all copyright interest in the application XWoot written
 *  by :    
 *          
 *          Gerome Canals
 *         Nabil Hachicha
 *         Gerald Hoster
 *         Florent Jouille
 *         Julien Maire
 *         Pascal Molli
 * 
 *  contact : maire@loria.fr
 *  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *  
 */

package org.xwoot.mockiphone.web.servlets;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.xwoot.mockiphone.web.MockIphoneSite;

/**
 * DOCUMENT ME!
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
        try {
            if (MockIphoneSite.getInstance().isStarted()) {
                System.out.println("Site: " + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId()
                    + " Bootstrap - instance already started");
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/defaultApp.do"));
                return;
            }

            String errors = "";
            String mockIphonePropertiesFile =
                request.getSession().getServletContext().getRealPath(MockIphoneSite.MOCKIPHONE_PROPERTIES_FILENAME);

            if (request.getParameter("update") != null) {
                errors =
                    MockIphoneSite.getInstance().updatePropertiesFiles(request,mockIphonePropertiesFile);

                // Start the MockIphone if the properties were correctly
                // saved.
                if (StringUtils.isBlank(errors)) {
                    Properties p = MockIphoneSite.getInstance().getProperties(mockIphonePropertiesFile);
                    MockIphoneSite.getInstance().init(
                        p.getProperty(MockIphoneSite.IWOOT_END_POINT),
                        p.getProperty(MockIphoneSite.MOCKIPHONE_DIR_NAME),
                        new Integer(RandomUtils.nextInt(1000000) + 1000000).intValue());

                    System.out.println("Site :" + MockIphoneSite.getInstance().getMockIphoneSiteEngine().getId()+ " Bootstrap - starting instance -");
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/default.do"));
                    return;
                }

                // There are errors, display the bootstrap page again.
                errors = errors.replaceAll("\n", "<br/>");
                request.setAttribute("errors", errors);
            }

            if (!StringUtils.isBlank(mockIphonePropertiesFile)) {
                Properties p =
                    MockIphoneSite.getInstance().updatePropertiesFromRequest(request, mockIphonePropertiesFile);
                request.setAttribute("properties", p);
            }
           
            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            System.out.println("EXCEPTION catched !!");
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            //request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        }
    }
}
