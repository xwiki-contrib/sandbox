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
package org.xwiki.xwoot.manager.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Composable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.phase.LogEnabled;
import org.xwiki.xwoot.manager.XWootManager;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DefaultXWootManager extends AbstractLogEnabled implements XWootManager, LogEnabled, Initializable,
    Composable
{
    private String DEFAULT_XWOOTAPP_ENDPOINT = "http://localhost:8080/xwootApp";

    /** A HTTP client used to communicate with XWoot. */
    private HttpClient client;

    private ComponentManager componentManager;

    private DocumentAccessBridge documentBridge;

    /** This is the page that is queried in order to retrieve the XWootApp endpoint */
    private String XWOOTAPP_ENDPOINT_PAGE = "XWiki.XWootAppEndpoint";

    /**
     * Initializes the HTTP client utility. Called by the component manager when this component is instantiated.
     * 
     * @see Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        client = new HttpClient(connectionManager);
        client.getParams().setSoTimeout(2000);
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(1, true));

        try {
            documentBridge = (DocumentAccessBridge) componentManager.lookup(DocumentAccessBridge.class.getName());
        } catch (ComponentLookupException e) {
            throw new InitializationException(e.getMessage());
        }
    }

    private String call(String service)
    {
        String xwootAppAddress = getXWootAppAddress();

        HttpMethod method = new GetMethod(xwootAppAddress + service);

        /* This is needed because the xwootApp servlet might send redirects to perform initializations */
        method.setFollowRedirects(true);

        try {
            getLogger().debug("Requesting: " + method.getURI());
            if (client.executeMethod(method) < 400) {
                String result = method.getResponseBodyAsString();
                getLogger().debug("Result: " + result);
                return result;
            }
            getLogger().info("Failed call: " + method.getStatusLine());
        } catch (CircularRedirectException e) {
            /*
             * Ignore. This could be normal. For example in the case of connecting/disconnecting the P2P network we call
             * the synchronize servlet that redirects to the boostrap that redirects to synchronize again, causing this
             * exception.
             */
        } catch (Exception ex) {
            getLogger().warn("Exception occured while calling [" + service + "] on [" + xwootAppAddress + "]", ex);
        } finally {
            // Release the connection, since HTTPClient reuses connections for improved performance
            method.releaseConnection();
        }
        return "failed";
    }

    private Map<String, Object> getStatusMap(String type)
    {
        String uri = String.format("%s/status?type=%s", getXWootAppAddress(), type);
        HttpMethod method = new GetMethod(uri);
        try {
            getLogger().debug("Requesting: " + method.getURI());
            if (client.executeMethod(method) < 400) {
                XStream xstream = new XStream(new DomDriver());
                Map<String, Object> result = (Map<String, Object>) xstream.fromXML(method.getResponseBodyAsStream());
                getLogger().debug("Result: " + result);
                return result;
            }
            getLogger().info("Failed call: " + method.getStatusLine());
        } catch (Exception ex) {
            getLogger().warn("Exception occured while calling [" + uri + "]", ex);
        } finally {
            // Release the connection, since HTTPClient reuses connections for improved performance
            method.releaseConnection();
        }

        return new HashMap<String, Object>();
    }

    public Map<String, Object> getConnectionsInfo()
    {
        return getStatusMap("connections");
    }

    public String getXWootAppAddress()
    {
        String xwootAppAddress = DEFAULT_XWOOTAPP_ENDPOINT;
        if (documentBridge.exists("XWiki.XWootAppEndpoint")) {
            try {
                xwootAppAddress = documentBridge.getDocumentContent(XWOOTAPP_ENDPOINT_PAGE).trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return xwootAppAddress;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#connectWiki()
     */
    public void connectWiki()
    {        
        call("/synchronize.do?action=cpconnection&switch=on");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#disconnectWiki()
     */
    public void disconnectWiki()
    {
        call("/synchronize.do?action=cpconnection&switch=off");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#connectP2P()
     */
    public void connectP2P()
    {
        /* This is not working anymore... */
        call("/synchronize.do?action=p2pnetworkconnection");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#disconnectP2P()
     */
    public void disconnectP2P()
    {
        /* This is not working anymore... */
        call("/synchronize.do?action=p2pnetworkconnection");
    }

    /**
     * {@inheritDoc}
     * 
     * @see XWootManager#synchronize()
     */
    public void synchronize()
    {
        call("/synchronize.do?action=synchronize");
    }

    public void compose(ComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    public Map<String, Object> getContentProviderInfo()
    {
        return getStatusMap("contentProvider");
    }
}
