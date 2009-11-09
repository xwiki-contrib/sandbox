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
package org.xwoot.xwootApp.web.filters;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * Filters all requests, and:
 * <ul>
 * <li>Forwards to the startup wizard when the xwoot engine is not initialized</li>
 * <li>Includes other peers in the network whenever a request from such a peer is received</li>
 * <li>Registers a skin in the session when a skin is requested</li>
 * </ul>
 * 
 * @todo Split this into several filters.
 * @todo Add a security filter.
 * @version $Id$
 */
public class BaseFilter implements Filter
{
    /** Logging helper object. */
    private static final Log LOG = LogFactory.getLog(BaseFilter.class);

    /** Serialization helper object. */
    private static final long serialVersionUID = -8050793384094800122L;

    /** {@inheritDoc} */
    public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException,
        ServletException
    {
        HttpServletRequest request = (HttpServletRequest) srequest;
        HttpServletResponse response = (HttpServletResponse) sresponse;

        LOG.debug(MessageFormat.format("Received request from {0}@{1} for {2}", request.getRemoteAddr(), request
            .getRemotePort(), request.getRequestURL()));

        XWootSite site = XWootSite.getInstance();
        XWootAPI xwoot = site.getXWootEngine();

        String requestedServletPath = request.getServletPath();
        String requestedContextPath = request.getContextPath();
        
        // While the XWoot site is not fully configured, ensure the proper flow.
        if (!site.isStarted()) {
            LOG.debug("Site is not started yet, starting the wizard.");
            if (!"/bootstrap.do".equals(requestedServletPath)) {
                response.sendRedirect(response.encodeRedirectURL(requestedContextPath + "/bootstrap.do"));
                return;
            }
        } else if (!xwoot.isConnectedToP2PNetwork()) {
            LOG.debug("Site is not connected to a network yet, opening network bootstrap.");
            if (!"/bootstrapNetwork.do".equals(requestedServletPath) && !"/synchronize.do".equals(requestedServletPath) && "p2pnetworkconnection".equals(request.getParameter("action"))) {
                response.sendRedirect(response.encodeRedirectURL(requestedContextPath + "/bootstrapNetwork.do"));
                return;
            }
        } else if (!xwoot.getPeer().hasJoinedAGroup()/*isConnectedToP2PGroup()*/) {
            LOG.debug("Site has not joined a group yet, opening group bootstrap.");
            // We don't force redirect if we are already there or if we changed our mind and went back one step. 
            if (!"/bootstrapGroup.do".equals(requestedServletPath) && !"/bootstrapNetwork.do".equals(requestedServletPath)) {
                response.sendRedirect(response.encodeRedirectURL(requestedContextPath + "/bootstrapGroup.do"));
                return;
            }
        } else if (!xwoot.isStateComputed()) {
            LOG.debug("Site does not have a state yet, opening stateManagement.");
            if (!"/stateManagement.do".equals(requestedServletPath) && !"/bootstrapGroup.do".equals(requestedServletPath)) {
                response.sendRedirect(response.encodeRedirectURL(requestedContextPath + "/stateManagement.do"));
                return;
            }
        }
        
        // Add a header to inform about the presence of the xwoot service.
        response.addHeader("XWOOT_SERVICE", "xwoot service");

        // Let the request be further processed.
        chain.doFilter(request, response);
        
        LOG.debug("Base Filter applied");
        
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see Filter#destroy()
     */
    public void destroy()
    {
    }
}
