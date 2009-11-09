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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.document.AdvertisementFactory;
import net.jxta.protocol.PipeAdvertisement;

import org.xwoot.contentprovider.Entry;
import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.jxta.JxtaPeer;
import org.xwoot.xwootApp.XWootAPI;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootApp.web.XWootSite;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Status extends HttpServlet
{
    private static final long serialVersionUID = 209685845279022541L;

    private static final String TYPE_QUERY_PARAMETER = "type";

    private static final String CONNECTIONS = "connections";

    private static final String CONTENT_PROVIDER = "contentProvider";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Map<String, Object> result = new HashMap<String, Object>();

        if (CONNECTIONS.equalsIgnoreCase(req.getParameter(TYPE_QUERY_PARAMETER))) {
            result = getConnectionsStatus();
        } else if (CONTENT_PROVIDER.equalsIgnoreCase(req.getParameter(TYPE_QUERY_PARAMETER))) {
            result = getContentProviderStatus();
        }

        resp.setContentType("application/xml");

        XStream xstream = new XStream(new DomDriver());
        xstream.toXML(result, resp.getOutputStream());
    }

    private Map<String, Object> getContentProviderStatus()
    {
        XWootSite xwootSite = XWootSite.getInstance();
        XWootAPI xwootAPI = xwootSite.getXWootEngine();

        Map<String, Object> result = new HashMap<String, Object>();
        
        if (xwootAPI != null) {
            XWootContentProviderInterface cp = xwootAPI.getContentProvider();
            
            List<Entry> entries = cp.getEntries(null, 0, -1);
            List<Map> entriesList = new ArrayList<Map>();
            for (Entry entry : entries) {
                Map<String, Object> entryProperties = new HashMap<String, Object>();
                entryProperties.put("pageId", entry.getPageId());
                entryProperties.put("timestamp", entry.getTimestamp());
                entryProperties.put("version", String.format("%d.%d", entry.getVersion(), entry.getMinorVersion()));
                entryProperties.put("cleared", entry.isCleared());
                entriesList.add(entryProperties);
            }

            result.put("contentProviderEntries", entriesList);
            
            entries = cp.getLastClearedEntries(null, 0, -1);
            entriesList = new ArrayList<Map>();
            for (Entry entry : entries) {
                Map<String, Object> entryProperties = new HashMap<String, Object>();
                entryProperties.put("pageId", entry.getPageId());
                entryProperties.put("timestamp", entry.getTimestamp());
                entryProperties.put("version", String.format("%d.%d", entry.getVersion(), entry.getMinorVersion()));                
                entriesList.add(entryProperties);
            }

            result.put("contentProviderLastClearedEntries", entriesList);            
        }

        return result;
    }

    private Map<String, Object> getConnectionsStatus()
    {
        XWootSite xwootSite = XWootSite.getInstance();
        XWootAPI xwootAPI = xwootSite.getXWootEngine();

        Map<String, Object> result = new HashMap<String, Object>();

        result.put("xwootStarted", xwootSite.isStarted());
        result.put("xwootConnectedToP2PNetwork", xwootAPI != null ? xwootAPI.isConnectedToP2PNetwork() : false);
        result.put("xwootConnectedToP2PGroup", xwootAPI != null ? xwootAPI.isConnectedToP2PGroup() : false);
        result.put("xwootConnectedToXWiki", xwootAPI != null ? xwootAPI.isContentManagerConnected() : false);
        result.put("xwootTargetXWiki", xwootAPI != null ? xwootAPI.getContentProvider().getEndpoint() : "None");
        result.put("xwootLastSynchronization", xwootAPI != null ? xwootAPI.getLastSynchronizationDate() : null);
        result.put("xwootLastSynchronizationFailure", xwootAPI != null ? xwootAPI.getLastSynchronizationFailure()
            : null);
        result.put("xwootIgnorePatterns", xwootAPI != null ? xwootAPI.getContentProvider().getConfiguration()
            .getIgnorePatterns() : "No content provider available.");
        result.put("xwootAcceptPatterns", xwootAPI != null ? xwootAPI.getContentProvider().getConfiguration()
            .getAcceptPatterns() : "No content provider available.");

        List<String> neighborNames = new ArrayList<String>();
        if (xwootAPI != null) {
            Collection<PipeAdvertisement> neighbors;
            //try {
                neighbors = xwootAPI.getNeighborsList();
                for (PipeAdvertisement n : neighbors) {
                    PipeAdvertisement original = n;
                    n = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
                    n.setPipeID(original.getPipeID());
                    n.setName(JxtaPeer.getPeerNameFromBackChannelPipeName(original.getName()));
                    n.setType(original.getType());
                    
                    neighborNames.add(n.getName());
                }
//            } catch (XWootException e) {
//                e.printStackTrace();
//            }
        }

        result.put("xwootNeighbours", neighborNames);

        return result;
    }

    private Properties getStatusPropertiesForConnections()
    {
        XWootSite xwootSite = XWootSite.getInstance();
        XWootAPI xwootAPI = xwootSite.getXWootEngine();

        Properties properties = new Properties();

        properties.setProperty("xwootStarted", Boolean.toString(xwootSite.isStarted()));
        properties.setProperty("xwootConnectedToP2PNetwork", xwootAPI != null ? Boolean.toString(xwootAPI
            .isConnectedToP2PNetwork()) : Boolean.toString(false));
        properties.setProperty("xwootConnectedToP2PGroup", xwootAPI != null ? Boolean.toString(xwootAPI
            .isConnectedToP2PGroup()) : Boolean.toString(false));
        properties.setProperty("xwootConnectedToXWiki", xwootAPI != null ? Boolean.toString(xwootAPI
            .isContentManagerConnected()) : Boolean.toString(false));
        properties.setProperty("xwootIgnorePatterns", xwootAPI != null ? xwootAPI.getContentProvider()
            .getConfiguration().getIgnorePatterns().toString() : "No content provider available.");
        properties.setProperty("xwootAcceptPatterns", xwootAPI != null ? xwootAPI.getContentProvider()
            .getConfiguration().getAcceptPatterns().toString() : "No content provider available.");

        List<String> neighborNames = new ArrayList<String>();
        if (xwootAPI != null) {
            Collection<PipeAdvertisement> neighbors;
//            try {
                neighbors = xwootAPI.getNeighborsList();
                for (PipeAdvertisement advertisement : neighbors) {
                    neighborNames.add(advertisement.getName());
                }
//            } catch (XWootException e) {
//                e.printStackTrace();
//            }
        }

        properties.setProperty("xwootNeighbours", neighborNames.toString());

        return properties;
    }

}
