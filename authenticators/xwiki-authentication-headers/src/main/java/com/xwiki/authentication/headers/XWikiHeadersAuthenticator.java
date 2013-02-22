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
package com.xwiki.authentication.headers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;

/**
 * Authentication based on HTTP headers.
 * <p>
 * Some parameters can be used to customized its behavior in xwiki.cfg:
 * <ul>
 * <li>xwiki.authentication.headers.secret_field: if the header field has any value, it's validated against
 * xwiki.authentication.headers.secret_value value</li>
 * <li>xwiki.authentication.headers.auth_field: if this header field has any value the authentication is apply,
 * otherwise it's trying standard XWiki authentication. The default field is <code>{@value #DEFAULT_AUTH_FIELD}</code>.</li>
 * <li>xwiki.authentication.headers.id_field: the value in header containing the string to use when creating the XWiki
 * user profile page. The default field is the same as auth field.</li>
 * <li>xwiki.authentication.headers.fields_mapping: mapping between HTTP header values and XWiki user profile values.
 * The default mapping is <code>{@value #DEFAULT_FILEDS_MAPPING}.</code></li>
 * </ul>
 *
 * @version $Id$
 */
public class XWikiHeadersAuthenticator extends XWikiAuthServiceImpl
{
    /** LogFactory <code>LOGGER</code>. */
    private static final Logger LOG = LoggerFactory.getLogger(XWikiHeadersAuthenticator.class);

    private static final String DEFAULT_AUTH_FIELD = "remote_user";

    private static final String DEFAULT_ID_FIELD = DEFAULT_AUTH_FIELD;

    private static final String DEFAULT_FILEDS_MAPPING = "email=mail,first_name=givenname,last_name=sn";

    private Map<String, String> userMappings;

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.user.impl.xwiki.AppServerTrustedAuthServiceImpl#checkAuth(com.xpn.xwiki.XWikiContext)
     */
    @Override
    public XWikiUser checkAuth(XWikiContext context) throws XWikiException
    {
        LOG.debug("Headers auth started");
        String secretField = getSecretFieldName(context);

        if (!StringUtils.isEmpty(secretField)) {
            String headerSecretValue = getSecretFieldHeaderValue(context);

            if (headerSecretValue == null || !headerSecretValue.equals(getSecretFieldValue(context))) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Secret header value in header [" + secretField + "] is not valid");
                }

                return null;
            }
        }

        LOG.debug("Headers auth after secret");
        String auth = getAuthFieldHeaderValue(context);

        LOG.debug("Headers auth field value: " + auth);

        // if no user is provided try standard XWiki authentication
        if (StringUtils.isEmpty(auth)) {
            return super.checkAuth(context);
        }

        String id = getIdFieldHeaderValue(context);

        // convert user id as XWiki does not support '.' in characters
        String validUserName = getValidUserName(id);
        String validUserFullName = "XWiki." + validUserName;

        LOG.debug("Headers auth username: " + validUserFullName);

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
                Map<String, String> extended = getExtendedInformations(context);
                extended.put("active", "1");

                context.getWiki().createUser(validUserName, extended, "edit", context);

                // mark that we have created the user in the session
                context.getRequest().getSession().setAttribute("xwikiheadersauthenticator", validUserFullName);
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("User " + validUserName + " has been successfully created");
                }
            } else if (!validUserFullName.equals(context.getRequest().getSession().getAttribute("xwikiheadersauthenticator"))) {
                Map<String, String> extended = getExtendedInformations(context);
                XWikiDocument userDoc = context.getWiki().getDocument(validUserFullName, context);                
                BaseObject userObj = userDoc.getObject("XWiki.XWikiUsers");
                boolean updated = false;

                for (Map.Entry<String, String> entry : extended.entrySet()) {
                   String field = entry.getKey();
                   String value = entry.getValue();
                   BaseProperty prop = (BaseProperty)userObj.get(field);
                   String currentValue = (prop==null || prop.getValue()==null) ? null : prop.getValue().toString();
                   if (value!=null && !value.equals(currentValue)) {
                     userObj.set(field, value, context);
                     updated = true;
                   }                
                }

                if (updated==true) {
                   context.getWiki().saveDocument(userDoc, context);
                }

                // mark that we have checked the user in the session
                context.getRequest().getSession().setAttribute("xwikiheadersauthenticator", validUserFullName);
            }
        } finally {
            context.setDatabase(database);
        }

        if (context.isMainWiki()) {
            return new XWikiUser(validUserFullName);
        } else {
            return new XWikiUser(context.getMainXWiki() + ":" + validUserFullName);
        }
    }

    public String getValidUserName(String userName)
    {
        return userName.replace('.', '=').replace('@', '_');
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.user.impl.xwiki.AppServerTrustedAuthServiceImpl#checkAuth(java.lang.String, java.lang.String,
     *      java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public XWikiUser checkAuth(String username, String password, String rememberme, XWikiContext context)
        throws XWikiException
    {
        String auth = getAuthFieldHeaderValue(context);

        if (StringUtils.isEmpty(auth)) {
            return super.checkAuth(context);
        } else {
            return checkAuth(context);
        }
    }

    private String getIdFieldHeaderValue(XWikiContext context)
    {
        return context.getRequest().getHeader(getIdFieldName(context));
    }

    private String getIdFieldName(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.headers.id_field", DEFAULT_ID_FIELD);
    }

    private String getAuthFieldHeaderValue(XWikiContext context)
    {
        return context.getRequest().getHeader(getAuthFieldName(context));
    }

    private String getAuthFieldName(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.headers.auth_field", DEFAULT_AUTH_FIELD);
    }

    private String getSecretFieldName(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.headers.secret_field", null);
    }

    private String getSecretFieldValue(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.headers.secret_value", null);
    }

    private String getSecretFieldHeaderValue(XWikiContext context)
    {
        return context.getRequest().getHeader(getSecretFieldName(context));
    }

    private Map<String, String> getExtendedInformations(XWikiContext context)
    {
        Map<String, String> extInfos = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : getFieldMapping(context).entrySet()) {
            String headerValue = context.getRequest().getHeader(entry.getKey());

            if (headerValue != null) {
                extInfos.put(entry.getValue(), headerValue);
            }
        }

        return extInfos;
    }

    /**
     * @param context the XWiki context.
     * @return the mapping between HTTP header fields names and XWiki user profile fields names.
     */
    private Map<String, String> getFieldMapping(XWikiContext context)
    {
        if (this.userMappings == null) {
            this.userMappings = new HashMap<String, String>();

            String fieldMapping =
                context.getWiki().Param("xwiki.authentication.headers.fields_mapping", DEFAULT_FILEDS_MAPPING);

            String[] fields = fieldMapping.split(",");

            for (int j = 0; j < fields.length; j++) {
                String[] field = fields[j].split("=");
                if (2 == field.length) {
                    String xwikiattr = field[0].trim();
                    String headerattr = field[1].trim();

                    this.userMappings.put(headerattr, xwikiattr);
                } else {
                    LOG.error("Error parsing HTTP header fields_mapping attribute in xwiki.cfg: " + fields[j]);
                }
            }
        }

        return this.userMappings;
    }
}
