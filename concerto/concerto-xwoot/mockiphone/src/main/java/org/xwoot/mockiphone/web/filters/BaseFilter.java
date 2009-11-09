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

package org.xwoot.mockiphone.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.xwoot.mockiphone.iwootclient.IWootClientException;
import org.xwoot.mockiphone.web.MockIphoneSite;

/**
 * Filters all requests, and:
 * <ul>
 * <li>Forwards to the startup wizard when the engine is not initialized</li>
 * <li>Registers a skin in the session when a skin is requested</li>
 * </ul> 
 * @version $Id$
 */
public class BaseFilter implements Filter
{
    private static final long serialVersionUID = -8050793384094800122L;

    /** The filter configuration, used for accessing the servlet context. */
    private FilterConfig config = null;
    
    /**
     * {@inheritDoc}
     * 
     * @see Filter#destroy()
     */
    public void destroy()
    {
        this.config = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException,
        ServletException
    {
        HttpServletRequest request = (HttpServletRequest) srequest;
        HttpServletResponse response = (HttpServletResponse) sresponse;

        // System.out.println("#######################");
        // System.out.println("# BaseFilter ");
        // System.out.println("# ---------- ");
        // System.out.println("# Request URI  : " + request.getRequestURI());
        // System.out.println("# Context Path : " + request.getContextPath());
        // System.out.println("# Method       : " + request.getMethod());
        // System.out.println("# Remote Host  : " + request.getRemoteHost());
        // System.out.println("# Remote Addr  : " + request.getRemoteAddr());
        // System.out.println("# Remote Port  : " + request.getRemotePort());
        // System.out.println("# Remote User  : " + request.getRemoteUser());
        // System.out.println("# Session ID   : "
        // + request.getRequestedSessionId());
        // System.out.println("#######################");

        // Changing the skin.
        if (request.getParameter("skin") != null) {
            request.getSession().setAttribute("skin", request.getParameter("skin"));
        }

        // Always display the wizard when mockiphone is not initialized
        if (!MockIphoneSite.getInstance().isStarted()) {
            System.out.println("Site is not started yet, starting the wizard.");
            if (!StringUtils.equals(request.getServletPath(), "/bootstrap.do")) {
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrap.do"));
                return;
            }
        }         
        this.config.getServletContext().log("Base Filter applied");
        
        try {
            request.setAttribute("iwootUrl", MockIphoneSite.getInstance().getMockIphoneSiteEngine().getIwootRestClient().getUri());
        } catch (IWootClientException e) {
           throw new ServletException(e);
        }

        // Let the request be further processed.
        chain.doFilter(request, response); 
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.config = filterConfig;
    }

}
