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
package org.xwiki.csrftoken.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.container.Container;
import org.xwiki.container.Request;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.csrftoken.CSRFToken;
import org.xwiki.csrftoken.CSRFTokenConfiguration;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

/**
 * Concrete implementation of the {@link CSRFToken} component.
 * 
 * This implementation uses a <code>user =&gt; token</code> map to store the tokens.
 * The tokens are random BASE64 encoded bit-strings.
 * 
 * TODO Expire tokens every couple of hours (configurable).
 * Expiration can be implemented using two maps, oldTokens and currentTokens, old tokens are replaced
 * by current tokens every 1/2 period, check is performed on both and new tokens are added to the current tokens.
 * 
 * @version $Id: $
 * @since 2.4
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class DefaultCSRFToken extends AbstractLogEnabled implements CSRFToken, Initializable
{
    /** Length of the random string in bytes. */
    private static final int TOKEN_LENGTH = 16;

    /** Space where resubmission page is located. */
    private static final String RESUBMIT_SPACE = "XWiki";

    /** Resubmission page name. */
    private static final String RESUBMIT_PAGE = "Resubmit";

    /** Token storage (one token per user). */
    private Map<String, String> tokens;

    /** Random number generator. */
    private SecureRandom random;

    /** Used to find out the current user name. */
    @Requirement
    private DocumentAccessBridge docBridge;

    /** Needed to access the current request. */
    @Requirement
    private Container container;

    /** Needed to find out the current wiki reference. */
    @Requirement
    private ModelContext model;

    /** CSRFToken component configuration. */
    @Requirement
    private CSRFTokenConfiguration configuration;

    /**
     * Initializes the storage and random number generator.
     * 
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {
        tokens = new ConcurrentHashMap<String, String>();
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            // use the default implementation then
            random = new SecureRandom();
            getLogger().warn("CSRFToken: Using default implementation of SecureRandom");
        }
        byte[] seed = random.generateSeed(TOKEN_LENGTH);
        random.setSeed(seed);
        getLogger().info("CSRFToken: Anti-CSRF secret token component has been initialized");
    }

    /**
     * {@inheritDoc}
     */
    public String getToken()
    {
        String user = docBridge.getCurrentUser();
        String token = tokens.get(user);

        // create fresh token if needed
        if (token == null) {
            byte[] bytes = new byte[TOKEN_LENGTH];
            random.nextBytes(bytes);
            token = Base64.encodeBase64URLSafeString(bytes);
            tokens.put(user, token);
        }
        return token;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTokenValid(String token)
    {
        if (!configuration.isEnabled()) {
            return true;
        }
        String storedToken = getToken();
        if (token == null || token.equals("") || !storedToken.equals(token)) {
            getLogger().warn("CSRFToken: Secret token verification failed, token: \"" + token
                + "\", stored token: \"" + storedToken + "\"");
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getResubmissionURL()
    {
        String currentUrl = getRequestURLWithoutToken();
        try {
            String query = "xredirect=" + URLEncoder.encode(currentUrl, "utf-8");
            EntityReference wiki = model.getCurrentEntityReference();
            EntityReference space = new EntityReference(RESUBMIT_SPACE, EntityType.SPACE);
            if (wiki != null) {
                space.setParent(wiki.extractReference(EntityType.WIKI));
            }
            EntityReference doc = new EntityReference(RESUBMIT_PAGE, EntityType.DOCUMENT, space);
            DocumentReference resubmitDoc = new DocumentReference(doc);
            return docBridge.getDocumentURL(resubmitDoc, "view", query, null);
        } catch (UnsupportedEncodingException exception) {
            // shouldn't happen
        }
        return "";
    }

    /**
     * Find out the URL of the current request and remove the 'form_token' parameter from the query.
     * The secret token will be replaced by the correct one on the resubmission page.
     * 
     * @return current URL without secret token
     */
    private String getRequestURLWithoutToken()
    {
        Request request = container.getRequest();
        if (request instanceof ServletRequest) {
            ServletRequest srequest = (ServletRequest) request;
            HttpServletRequest httpRequest = srequest.getHttpServletRequest();
            StringBuffer url = httpRequest.getRequestURL();
            String query = httpRequest.getQueryString();
            if (query != null) {
                query = query.replaceAll("(^|&)form_token=[^&]*", "");
                query = query.replaceFirst("^&", "");
                if (query != null && query.trim().length() != 0) {
                    url.append("?");
                    url.append(query);
                }
            }
            return url.toString();
        }
        throw new RuntimeException("Not supported request type");
    }
}

