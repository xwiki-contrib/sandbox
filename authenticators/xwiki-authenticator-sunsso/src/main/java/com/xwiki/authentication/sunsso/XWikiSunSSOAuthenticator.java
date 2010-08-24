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
 *
 */
package com.xwiki.authentication.sunsso;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;

import com.iplanet.am.sdk.AMException;
import com.iplanet.am.sdk.AMStoreConnection;
import com.iplanet.am.sdk.AMUser;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;

/**
 * Authentication based on Sun SSO.
 * <p>
 * Some parameters can be used to customized its behavior in xwiki.cfg:
 * <ul>
 * <li>xwiki.authentication.cb.fields_mapping: mapping between LDAP fields and XWiki user profile fields. The default
 * mapping is <code>{@value #DEFAULT_FIELDSMAPPING_VALUE}.</code></li>
 * </ul>
 * 
 * @version $Id$
 */
public class XWikiSunSSOAuthenticator extends XWikiAuthServiceImpl
{
    /**
     * Logging tool.
     */
    private static final Log LOG = LogFactory.getLog(XWikiSunSSOAuthenticator.class);

    /**
     * The name of the configuration property containing the user attribute mapping for synchronization.
     */
    private static final String CONF_FIELDSMAPPING_NAME = "xwiki.authentication.sunsso.fields_mapping";

    /**
     * The default value of the configuration property containing the user attribute mapping for synchronization.
     */
    private static final String DEFAULT_FIELDSMAPPING_VALUE = "email=mail,first_name=givenName,last_name=sn";

    /**
     * The name of the configuration property containing the user id field name (used to create the XWiki user profile).
     */
    private static final String CONF_FIELD_UID = "xwiki.authentication.sunsso.field_uid";

    /**
     * The default value of the configuration property containing the user id field name (used to create the XWiki user
     * profile).
     */
    private static final String DEFAULT_FIELD_UID = "uid";

    /**
     * The XWiki space where users and groups are stored.
     */
    private static final String XWIKI_USERS_SPACE = "XWiki.";

    /**
     * Cache of the user attribute mapping for synchronization.
     */
    private Map<String, String> userMappings;

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl#authenticate(java.lang.String, java.lang.String,
     *      com.xpn.xwiki.XWikiContext)
     */
    @Override
    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException
    {
        // only work with SSO, does not take into account login/password
        XWikiUser user = checkAuth(context);
        return user == null ? null : new SimplePrincipal(checkAuth(context).getUser());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.user.impl.xwiki.AppServerTrustedAuthServiceImpl#checkAuth(com.xpn.xwiki.XWikiContext)
     */
    @Override
    public XWikiUser checkAuth(XWikiContext context) throws XWikiException
    {
        try {
            SSOTokenManager manager = SSOTokenManager.getInstance();
            SSOToken ssoToken = manager.createSSOToken(context.getRequest());

            // Get DN
            if (!manager.isValidToken(ssoToken)) {
                LOG.error("Failed to validate token [" + ssoToken.getPrincipal().getName() + "].");

                return null;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Authentication of [" + ssoToken.getPrincipal().getName()
                    + "] succeed. Starting synchronisation");
            }

            // Get LDAP user profile

            AMStoreConnection connection = new AMStoreConnection(ssoToken);

            AMUser user = connection.getUser(ssoToken.getPrincipal().getName());

            Map<String, Set< ? >> ldapUserAttributes = user.getAttributes();

            String ldapUserName = (String) ldapUserAttributes.get(getUidField(context)).iterator().next();

            String xwikiUserName = getXWikiFullUserName(ldapUserName);

            // Synchronize users
            syncUser(ldapUserAttributes, xwikiUserName, context);

            return new XWikiUser(xwikiUserName);
        } catch (SSOException e) {
            LOG.error("Failed to connect to SSO server", e);
        } catch (AMException e) {
            LOG.error("Failed to get user attributes.", e);
        }

        return null;
    }

    /**
     * Update or create XWiki user profile based on LDAP user profile.
     * 
     * @param ldapUserAttributes LDAP user attributes
     * @param xwikiUserName XWiki user name
     * @param context the XWiki context
     * @throws XWikiException error when synchronizing user
     */
    private void syncUser(Map<String, Set< ? >> ldapUserAttributes, String xwikiUserName, XWikiContext context)
        throws XWikiException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start synch user [" + xwikiUserName + "] with attributes [" + ldapUserAttributes + "]");
        }

        if (!context.getWiki().exists(xwikiUserName, context)) {
            createUser(ldapUserAttributes, xwikiUserName, context);
        } else {
            updateUser(ldapUserAttributes, xwikiUserName, context);
        }
    }

    /**
     * Create a new XWiki user based on LDAP user profile.
     * 
     * @param ldapUserAttributes LDAP user attributes
     * @param xwikiUserName XWiki user name
     * @param context the XWiki context
     * @throws XWikiException error when creating the new XWiki user
     */
    private void createUser(Map<String, Set< ? >> ldapUserAttributes, String xwikiUserName, XWikiContext context)
        throws XWikiException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Create user [" + xwikiUserName + "]");
        }

        Map<String, String> map = getUserMap(ldapUserAttributes, null, context);
        context.getWiki().createUser(xwikiUserName.substring(XWIKI_USERS_SPACE.length()), map, "XWiki.XWikiUserClass",
            "#includeForm(\"XWiki.XWikiUserSheet\")", "xwiki/1.0", "edit", context);
    }

    /**
     * Update the XWiki user based on LDAP user profile.
     * 
     * @param ldapUserAttributes LDAP user attributes
     * @param xwikiUserName XWiki user name
     * @param context the XWiki context
     * @throws XWikiException error when updating XWiki user profile
     */
    private void updateUser(Map<String, Set< ? >> ldapUserAttributes, String xwikiUserName, XWikiContext context)
        throws XWikiException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Update user [" + xwikiUserName + "]");
        }

        XWikiDocument userDocument = context.getWiki().getDocument(xwikiUserName, context);
        BaseObject userObject = userDocument.getObject("XWiki.XWikiUsers");

        Map<String, String> map = getUserMap(ldapUserAttributes, userObject, context);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Fields to update: " + map + "");
        }

        if (!map.isEmpty()) {
            BaseClass userClass = context.getWiki().getUserClass(context);
            userClass.fromMap(map, userObject);

            context.getWiki().saveDocument(userDocument, context);
        }
    }

    /**
     * Get the values to synchronize from LDAP user profile to XWiki user profile.
     * 
     * @param ldapUserAttributes LDAP user attributes
     * @param userObject XWiki user attributes
     * @param context the XWiki context
     * @return the values to synchronize from LDAP user profile to XWiki user profile.
     */
    private Map<String, String> getUserMap(Map<String, Set< ? >> ldapUserAttributes, BaseObject userObject,
        XWikiContext context)
    {
        Map<String, String> userFieldsMapping = getFieldMapping(context);

        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : userFieldsMapping.entrySet()) {
            String ldapUserField = entry.getKey();
            String xwikiUserField = entry.getValue();

            try {
                String ldapUserValue = (String) ldapUserAttributes.get(ldapUserField).iterator().next();

                if (userObject != null) {
                    String xwikiUserValue = userObject.getStringValue(xwikiUserField);

                    if (xwikiUserValue == null || !xwikiUserValue.equals(ldapUserValue)) {
                        map.put(xwikiUserField, ldapUserValue);
                    }
                } else {
                    map.put(xwikiUserField, ldapUserValue);
                }
            } catch (Exception e) {
                LOG.warn("Failed to synchonize LDAP field user [" + ldapUserField + "] with XWiki user field ["
                    + xwikiUserField + "]", e);
            }
        }

        return map;
    }

    /**
     * Crate XWiki user full name from LDAP uid.
     * 
     * @param ldapUserName the LDAP user uid
     * @return the XWiki user full name
     */
    private String getXWikiFullUserName(String ldapUserName)
    {
        return XWIKI_USERS_SPACE + ldapUserName;
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
        return checkAuth(context);
    }

    /**
     * @param context the XWiki context.
     * @return the user id field name
     */
    private String getUidField(XWikiContext context)
    {
        return context.getWiki().Param(CONF_FIELD_UID, DEFAULT_FIELD_UID);
    }

    /**
     * @param context the XWiki context.
     * @return the mapping between LDAP header fields names and XWiki user profile fields names.
     */
    private Map<String, String> getFieldMapping(XWikiContext context)
    {
        if (this.userMappings == null) {
            this.userMappings = new HashMap<String, String>();

            String fieldMapping = context.getWiki().Param(CONF_FIELDSMAPPING_NAME, DEFAULT_FIELDSMAPPING_VALUE);

            String[] fields = fieldMapping.split(",");

            for (int j = 0; j < fields.length; j++) {
                String[] field = fields[j].split("=");
                if (2 == field.length) {
                    String xwikiattr = field[0].trim();
                    String ldapattr = field[1].trim();

                    this.userMappings.put(ldapattr, xwikiattr);
                } else {
                    LOG.error("Error parsing fields_mapping attribute in xwiki.cfg: " + fields[j]);
                }
            }
        }

        return this.userMappings;
    }
}
