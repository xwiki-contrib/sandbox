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
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.protocol.PipeAdvertisement;

import org.apache.commons.lang.StringUtils;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * <p>
 * Servlet that can provide status information about the XWoot engine, such as the connected/disconnected state, number
 * of peers, number of monitored pages.
 * </p>
 * <p>
 * The servlet is used by making a HTTP request to
 * <code>http://xwoot.server.net/xwootApp/information?request=&lt;action&gt;</code>, where <code>action</code> is one
 * of:
 * <ul>
 * <li><code>isXWootInitialized</code> to test if the XWoot engine is configured and running</li>
 * <li><code>isWikiConnected</code> to test if XWoot is connected to the managed wiki and can synchronize with it</li>
 * <li><code>isP2PNetworkConnected</code> to test if the P2P network is enabled</li>
 * <li><code>isDocumentManaged</code> to test if a document is managed by XWoot; the document name is specified in the
 * <code>document</code> request parameter</li>
 * <li><code>listPeers</code> to list the configured peers and their connected status</li>
 * </ul>
 * </p>
 * 
 * @version $Id$
 */
public class Information extends HttpServlet
{
    private static final long serialVersionUID = 20080922L;

    /** Timeout for detecting if a peer is connected or not. */
    private static final int CONNECTION_TIMEOUT = 5000;

    /**
     * {@inheritDoc}
     * 
     * @see HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        try {
            response.setContentType("text/xml; charset=UTF-8");
            StringBuffer result = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            XWootSite woot = XWootSite.getInstance();
            String info = request.getParameter("request");
            if (info == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.append("<error>No specific information requested</error>");
            } else if (info.equals("isXWootInitialized")) {
                result.append("<initialized>" + woot.isStarted() + "</initialized>");
            } else {
                if (!woot.isStarted()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    result.append("<error>Server not started yet</error>");
                } else {
                    XWootAPI xwoot = woot.getXWootEngine();
                    if (info.equals("isWikiConnected")) {
                        result.append("<wikiConnected>" + xwoot.isContentManagerConnected() + "</wikiConnected>");
                    } else if (info.equals("isP2PNetworkConnected")) {
                        result.append("<p2pConnected>" + xwoot.isConnectedToP2PNetwork() + "</p2pConnected>");
                    } else if (info.equals("isDocumentManaged")) {
                        String pagename = request.getParameter("document");
                        if (StringUtils.isEmpty(pagename)) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            result.append("<error>You must specify a page name</error>");
                        } else {
                         /*   XWootPage page = new XWootPage(pagename, null);
                            result.append("<managed name=\"" + pagename + "\">" + ((XWoot)xwoot).isPageManaged(page)
                                + "</managed>");*/
                        }
                    } else if (info.equals("listPeers")) {
                        result.append("<peers>\n");
                        for (Object peer : xwoot.getNeighborsList()) {
                            result.append("  <peer connected=\"" + getConnectionStatus(peer) + "\">" + peer
                                + "</peer>\n");
                        }
                        result.append("</peers>");
                    } else if (info.equals("listLastPages")){
                        String id = request.getParameter("id");
                        List<String>pages=xwoot.getLastPages(id);
                        result.append("<pages>\n");
                        for (String pageName : pages){
                            result.append("  <page>" + pageName +"</page>\n");
                        }
                        result.append("</pages>\n");
                    }
                    else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        result.append("<error>Unknown information</error>");
                        System.out.println(info);
                    }
                }
            }
            String resultString = result.toString();
            Writer out = response.getWriter();
            response.setContentLength(resultString.getBytes("UTF-8").length);
            out.append(resultString);
            out.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Tests if a peer is connected to the P2P network or not. To be considered offline a peer can either be offline, or
     * be online but have the P2P network disabled, meaning that it will not accept or create patches.
     * 
     * @param peer The peer HTTP address.
     * @return <code>true</code> if the peer is reachable and has its P2P network enabled, <code>false</code> otherwise.
     */
    private boolean getConnectionStatus(Object peer)
    {
        if (peer instanceof String) {
            try {
                URL testAddress = new URL(peer + "/synchronize.do?test=true");
                HttpURLConnection connection = (HttpURLConnection) testAddress.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(CONNECTION_TIMEOUT);
                connection.setUseCaches(false);
                connection.setRequestMethod("GET");
                return connection.getResponseMessage().contains("OK");
            } catch (Exception ex) {
                return false;
            }
        } else if (peer instanceof PipeAdvertisement) {
            // TODO: Ping peer through pipe.
            return true;
        }
        
        return false;
    }
}
