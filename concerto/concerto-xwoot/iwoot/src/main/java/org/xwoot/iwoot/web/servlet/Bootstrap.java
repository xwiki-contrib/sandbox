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

package org.xwoot.iwoot.web.servlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.xwoot.iwoot.restApplication.RestApplication;
import org.xwoot.iwoot.web.IWootWebApp;

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
            if (IWootWebApp.getInstance().isStarted()) {
                System.out.println("Site: " + IWootWebApp.getInstance().getIWootEngine().getId()
                    + " Bootstrap - instance already started");
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/mainboard.do"));
                return;
            }

            String errors = "";
            String iwootPropertiesFile =
                request.getSession().getServletContext().getRealPath(IWootWebApp.IWOOT_PROPERTIES_FILENAME);

            if (request.getParameter("update") != null) {
                errors =
                    IWootWebApp.getInstance().updatePropertiesFiles(request,iwootPropertiesFile);

                // Start IWoot if the properties were correctly
                // saved.
                if (StringUtils.isBlank(errors)) {
                    Properties p = IWootWebApp.getInstance().getProperties(iwootPropertiesFile);
                    IWootWebApp.getInstance().init(
                        p.getProperty(IWootWebApp.IWOOT_XWOOT_URL_PROPERTY_KEY),
                        p.getProperty(IWootWebApp.IWOOT_WCM_TYPE_PROPERTY_KEY),
                        p.getProperty(IWootWebApp.IWOOT_XWOOT_TYPE_PROPERTY_KEY),
                        p.getProperty(IWootWebApp.IWOOT_REAL_WCM_URL_PROPERTY_KEY),
                        p.getProperty(IWootWebApp.IWOOT_REAL_WCM_LOGIN_PROPERTY_KEY),
                        p.getProperty( IWootWebApp.IWOOT_REAL_WCM_PWD_PROPERTY_KEY),
                        new Integer(RandomUtils.nextInt(1000000) + 1000000));

                    System.out.println("Site :" + IWootWebApp.getInstance().getIWootEngine().getId()+ " Bootstrap - starting instance -");
                    RestApplication appli=(RestApplication) getServletContext().getAttribute("com.noelios.restlet.ext.servlet.ServerServlet.application");
                    
                    if (appli!=null){
                        appli.setIwoot(IWootWebApp.getInstance().getIWootEngine());
                    }
                    
                    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/rest"));
                    return;
                }

                // There are errors, display the bootstrap page again.
                errors = errors.replaceAll("\n", "<br/>");
                request.setAttribute("errors", errors);
            }

            if (!StringUtils.isBlank(iwootPropertiesFile)) {
                Properties p =
                    IWootWebApp.getInstance().updatePropertiesFromRequest(request, iwootPropertiesFile);
                request.setAttribute("properties", p);
            }
           
            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            System.out.println("EXCEPTION catched !!");
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/pages/Bootstrap.jsp").forward(request, response);
            return;
        }
    }
}
