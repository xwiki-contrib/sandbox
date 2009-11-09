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
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.document.AdvertisementFactory;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezVousService;

import org.apache.commons.lang.StringUtils;
import org.xwoot.jxta.JxtaPeer;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.web.XWootSite;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class Synchronize extends HttpServlet
{
    private static final long serialVersionUID = -3758874922535817475L;

    /**
     * DOCUMENT ME!
     * 
     * @param request DOCUMENT ME!
     * @param response DOCUMENT ME!
     */
    @SuppressWarnings("unchecked")
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        System.out.print("Site " + XWootSite.getInstance().getXWootEngine().getXWootPeerId() + " : Synchronize page -");
        
        XWootAPI xwootEngine = XWootSite.getInstance().getXWootEngine();

        // synchronize
        if ("synchronize".equals(request.getParameter("action"))
            && XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
            this.log("Synchronization requested.");
            try {
                XWootSite.getInstance().getXWootEngine().synchronize();
            } catch (Exception e) {
                this.log("Error while synchronizing.\n", e);
                
                // FIXME: bring back the "errors" mechanism for this page as well instead of throwing servlet exceptions.
                throw new ServletException(e);
            }
        }

        // anti entropy
        else if ("antiEntropy".equals(request.getParameter("action"))
            && XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
            String neighbor = request.getParameter("neighbor");
            try {
                XWootSite.getInstance().getXWootEngine().doAntiEntropy(neighbor);
            } catch (Exception e) {
                this.log("Problems while doing anti-entropy with " + neighbor, e);
                
                //FIXME: bring back the "errors" mechanism for this page as well instead of throwing servlet exceptions.
                throw new ServletException(e);
            }
        }

        // p2p connection
        else if ("p2pnetworkconnection".equals(request.getParameter("action"))) {
            this.log("P2P connection gestion ...");
            try {
//                String mode = request.getParameter("switch");
//                if ("on".equals(mode)
//                    && !XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
//                    XWootSite.getInstance().getXWootEngine().reconnectToP2PNetwork();
//                } else if ("off".equals(mode)
//                    && XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
//                    XWootSite.getInstance().getXWootEngine().disconnectFromP2PNetwork();
//                } else {
                    if (xwootEngine.isConnectedToP2PNetwork()) {
                        xwootEngine.disconnectFromP2PNetwork();
                        
                        // Stop auto-synchronization. We don't need redundant patches.
                        XWootSite.getInstance().getAutoSynchronizationThread().stopThread();
                    } else {
                        if (xwootEngine.getPeer().isJxtaStarted()) {
                            // Network was trying to reconnect.
                            xwootEngine.disconnectFromP2PNetwork();
                        } else {
                            xwootEngine.reconnectToP2PNetwork();
                            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapGroup.do"));
                            
    //                        // Redirect to network bootstrap which will automatically rejoin the existing network configuration.
    //                        response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/bootstrapNetwork.do"));
                            return;
                        }
                    }
//                }
            } catch (Exception e) {
                // Disconnecting/Reconnecting failed, do nothing, make the user try again.
                //  We should show an error or something.
                //throw new ServletException(e);
            }
        }

        // cp connection
        else if ("cpconnection".equals(request.getParameter("action"))) {
            this.log("Content Provider connection gestion ...");
            try {
                if (XWootSite.getInstance().getXWootEngine().isConnectedToP2PGroup()){
                    XWootSite.getInstance().getXWootEngine().doAntiEntropyWithAllNeighbors();
                } 
                String mode = request.getParameter("switch");
                if (StringUtils.equals(mode, "on")
                    && !XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
                    XWootSite.getInstance().getXWootEngine().connectToContentManager();
                } else if (StringUtils.equals(mode, "off")
                    && XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
                    XWootSite.getInstance().getXWootEngine().disconnectFromContentManager();
                } else {
                    if (XWootSite.getInstance().getXWootEngine().isContentManagerConnected()) {
                        XWootSite.getInstance().getXWootEngine().disconnectFromContentManager();
                    } else {
                        XWootSite.getInstance().getXWootEngine().connectToContentManager();  
                    }
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        else {
            this.log(" no action ! -");
        }

        // view neighbors list
        Collection<PipeAdvertisement> neighbors = null;
        try {
            neighbors = xwootEngine.getNeighborsList();
        } catch (Exception e) {
            // remove this with new xwootAPI adapted to XWoot3.
        }
        
        if (neighbors != null) {
            HashMap<PipeAdvertisement, Boolean> result = new HashMap<PipeAdvertisement, Boolean>();
            for (PipeAdvertisement n : neighbors) {
                
                // send to the UI a lighter, copy version having a human-readable name.
                PipeAdvertisement original = n;
                n = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
                n.setPipeID(original.getPipeID());
                n.setName(JxtaPeer.getPeerNameFromBackChannelPipeName(original.getName()));
                n.setType(original.getType());
                
                if (!XWootSite.getInstance().getXWootEngine().isConnectedToP2PNetwork()) {
                    this.log(n + " Site " + n + " is not connected because we are disconnected.");
                    result.put(n, Boolean.FALSE);
                } else {
                    //TODO: implement a ping mechanism.
                    /*URL to = new URL(n + "/synchronize.do?test=true");
                    try {
                        HttpURLConnection init = (HttpURLConnection) to.openConnection();
                        result.put(n, Boolean.valueOf(init.getResponseMessage().contains("OK")));
                        init.disconnect();
                    } catch (Exception e) {
                        System.out.println(n + " Neighbor " + n + " is not connected");
                        result.put(n, Boolean.FALSE);
                    }*/
                    result.put(n, Boolean.TRUE);
                }
            }
            request.setAttribute("noneighbor", Boolean.valueOf(neighbors.size() == 0));
            request.setAttribute("neighbors", result);
        } else {
            request.setAttribute("noneighbor", true);
        }
        
        
        int groupConnection = -1;
        if (xwootEngine.isConnectedToP2PNetwork()) {
            RendezVousService rdvStatus = xwootEngine.getPeer().getCurrentJoinedPeerGroup().getRendezVousService();
            boolean isGroupRDV = rdvStatus.isRendezVous();
            // Check number of connected clients or connected rdvs
            boolean isConnectedToPeers = rdvStatus.getConnectedPeerIDs().size() > 0;
            // Check number of known rdvs in the network (If RDV peer).
            isConnectedToPeers |= rdvStatus.getLocalWalkView().size() > 0;
            
            if (isConnectedToPeers) {
                groupConnection = 1;
            } else if (isGroupRDV) {
                groupConnection = 0;
            }
        } else {
            // -1 by default, when network is down.
        }
        
        int networkConnection = -1;
        if (xwootEngine.isConnectedToP2PNetwork()) {
            RendezVousService rdvStatus = xwootEngine.getPeer().getDefaultGroup().getRendezVousService();
            boolean isGroupRDV = rdvStatus.isRendezVous();
            // Check number of connected clients or connected rdvs
            boolean isConnectedToPeers = rdvStatus.getConnectedPeerIDs().size() > 0;
            // Check number of known rdvs in the network (If RDV peer).
            isConnectedToPeers |= rdvStatus.getLocalWalkView().size() > 0;
            
            if (isConnectedToPeers) {
                networkConnection = 1;
            } else if (isGroupRDV) {
                networkConnection = 0;
            }
        } else {
            // -1 by default, when network is down.
        }
        
        
        //Boolean reconnectingToNetwork = xwootEngine.getPeer().isJxtaStarted() && !xwootEngine.isConnectedToP2PNetwork();
        //Boolean connectedToNetwork = xwootEngine.isConnectedToP2PNetwork() || reconnectingToNetwork;
        
        request.setAttribute("content_provider", XWootSite.getInstance().getXWootEngine().getContentProvider());
        request.setAttribute("xwiki_url", XWootSite.getInstance().getXWootEngine().getContentManagerURL());
        request.setAttribute("p2pconnection", xwootEngine.getPeer().isJxtaStarted());
        request.setAttribute("groupConnection", groupConnection);
        request.setAttribute("networkConnection", networkConnection);        
        request.setAttribute("cpconnection", Boolean.valueOf(XWootSite.getInstance().getXWootEngine()
            .isContentManagerConnected()));
        request.getRequestDispatcher("/pages/Synchronize.jsp").forward(request, response);


        return;
    }
}
