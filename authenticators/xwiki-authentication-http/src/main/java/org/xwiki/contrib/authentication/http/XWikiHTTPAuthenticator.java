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
package org.xwiki.contrib.authentication.http;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.securityfilter.realm.SimplePrincipal;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;

/**
 * Authentication based on HTTP auth on a configured URL.
 * 
 * @version $Id$
 */
public class XWikiHTTPAuthenticator extends XWikiAuthServiceImpl
{
    /**
     * Logging tool.
     */
    private static final Log LOG = LogFactory.getLog(XWikiHTTPAuthenticator.class);

    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException
    {
        if (username != null) {
            // Get configuration
            String uri = context.getWiki().Param("xwiki.authentication.http.uri");

            // Check auth
            try {
                if (checkAuth(username, password, new URI(uri))) {
                    return syncUser(username, context);
                }
            } catch (Exception e) {
                throw new XWikiException(XWikiException.MODULE_XWIKI_ACCESS, XWikiException.ERROR_XWIKI_UNKNOWN,
                    "Failed to authenticate", e);
            }
        }

        // Fallback on standard XWiki authentication
        return super.authenticate(username, password, context);
    }

    // Auth

    private boolean checkAuth(String username, String password, URI uri) throws ClientProtocolException, IOException
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "XWik");
        httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
        httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);

        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(username, password));

        HttpGet httpget = new HttpGet(uri);
        HttpResponse response = httpClient.execute(httpget);

        return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
    }

    // Sync

    public String getValidUserName(String userName)
    {
        return userName.replace('.', '_').replace('@', '_');
    }

    private Principal syncUser(String username, XWikiContext context) throws XWikiException
    {
        // Synch user
        String validUserName = getValidUserName(username);
        String validUserFullName = "XWiki." + validUserName;

        String database = context.getDatabase();
        try {
            // Switch to main wiki to force users to be global users
            context.setDatabase(context.getMainXWiki());

            // test if user already exists
            if (!context.getWiki().exists(validUserFullName, context)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Need to create user " + validUserName);
                }

                // create user
                Map<String, String> extended = new HashMap<String, String>();
                extended.put("active", "1");

                context.getWiki().createUser(validUserName, extended, context);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("User " + validUserName + " has been successfully created");
                }
            }
        } finally {
            context.setDatabase(database);
        }

        if (context.isMainWiki()) {
            return new SimplePrincipal(validUserFullName);
        } else {
            return new SimplePrincipal(context.getMainXWiki() + ":" + validUserFullName);
        }
    }
}
