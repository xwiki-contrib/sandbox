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

package com.xwiki.authentication.puma;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;

import com.ibm.portal.portlet.service.PortletServiceHome;
import com.ibm.portal.portlet.service.PortletServiceUnavailableException;
import com.ibm.portal.um.Group;
import com.ibm.portal.um.PumaLocator;
import com.ibm.portal.um.PumaProfile;
import com.ibm.portal.um.User;
import com.ibm.portal.um.exceptions.PumaAttributeException;
import com.ibm.portal.um.exceptions.PumaException;
import com.ibm.portal.um.exceptions.PumaMissingAccessRightsException;
import com.ibm.portal.um.exceptions.PumaModelException;
import com.ibm.portal.um.exceptions.PumaSystemException;
import com.ibm.portal.um.portletservice.PumaHome;
import com.novell.ldap.LDAPConnection;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConfig;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPSearchAttribute;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.LDAP.LDAPProfileXClass;
import com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.XWikiRequest;

public class PUMAAuthServiceImpl extends XWikiLDAPAuthServiceImpl
{
    /** LogFactory <code>LOGGER</code>. */
    private static final Log LOG = LogFactory.getLog(PUMAAuthServiceImpl.class);

    private static final String COOKIE_NAME = "XWIKISSOAUTHINFO";

    private PUMAConfig config = null;

    protected String encryptText(String text, XWikiContext context)
    {
        try {
            String secretKey = null;
            secretKey = context.getWiki().Param("xwiki.authentication.encryptionKey");
            secretKey = secretKey.substring(0, 24);

            if (secretKey != null) {
                SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "TripleDES");
                Cipher cipher = Cipher.getInstance("TripleDES");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] encrypted = cipher.doFinal(text.getBytes());
                String encoded = new String(Base64.encodeBase64(encrypted));
                return encoded.replaceAll("=", "_");
            } else {
                LOG.error("Encryption key not defined");
            }
        } catch (Exception e) {
            LOG.error("Failed to encrypt text", e);
        }

        return null;
    }

    protected String decryptText(String text, XWikiContext context)
    {
        try {
            String secretKey = null;
            secretKey = context.getWiki().Param("xwiki.authentication.encryptionKey");
            secretKey = secretKey.substring(0, 24);

            if (secretKey != null) {
                SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "TripleDES");
                Cipher cipher = Cipher.getInstance("TripleDES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] encrypted = Base64.decodeBase64(text.replaceAll("_", "=").getBytes("ISO-8859-1"));
                String decoded = new String(cipher.doFinal(encrypted));
                return decoded;
            } else {
                LOG.error("Encryption key not defined");
            }
        } catch (Exception e) {
            LOG.error("Failed to decrypt text", e);
        }

        return null;
    }

    public void setConfig(PUMAConfig config)
    {
        this.config = config;
    }

    public PUMAConfig getConfig()
    {
        if (this.config == null) {
            this.config = new PUMAConfig();
        }

        return this.config;
    }

    protected Cookie getCookie(String cookieName, XWikiContext context)
    {
        Cookie[] cookies = context.getRequest().getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl#checkAuth(com.xpn.xwiki.XWikiContext)
     */
    public XWikiUser checkAuth(XWikiContext context) throws XWikiException
    {
        XWikiUser user = null;

        if (context.getRequest().getRemoteUser() != null) {
            user = checkAuthSSO(null, null, context);
        }

        if (user == null) {
            user = super.checkAuth(context);
        }

        return user;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl#checkAuth(java.lang.String, java.lang.String,
     *      java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    public XWikiUser checkAuth(String username, String password, String rememberme, XWikiContext context)
        throws XWikiException
    {
        XWikiUser user = null;

        if (context.getRequest().getRemoteUser() != null) {
            user = checkAuthSSO(username, password, context);
        }

        if (user == null) {
            user = super.checkAuth(username, password, rememberme, context);
        }

        return user;
    }

    public XWikiUser checkAuthSSO(String username, String password, XWikiContext context) throws XWikiException
    {
        Cookie cookie;

        LOG.debug("checkAuth");

        LOG.debug("Action: " + context.getAction());
        if (context.getAction().startsWith("logout")) {
            cookie = getCookie(COOKIE_NAME, context);
            if (cookie != null) {
                cookie.setMaxAge(0);
                context.getResponse().addCookie(cookie);
            }

            return null;
        }

        Principal principal = null;

        if (LOG.isDebugEnabled()) {
            Cookie[] cookies = context.getRequest().getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    LOG.debug("CookieList: " + c.getName() + " => " + c.getValue());
                }
            }
        }

        cookie = getCookie(COOKIE_NAME, context);
        if (cookie != null) {
            LOG.debug("Found Cookie");
            String uname = decryptText(cookie.getValue(), context);
            if (uname != null) {
                principal = new SimplePrincipal(uname);
            }
        }

        XWikiUser user;

        // Authenticate
        if (principal == null) {
            principal = authenticate(username, password, context);
            if (principal == null) {
                return null;
            }

            LOG.debug("Saving auth cookie");
            String encuname = encryptText(principal.getName(), context);
            Cookie usernameCookie = new Cookie(COOKIE_NAME, encuname);
            usernameCookie.setMaxAge(-1);
            usernameCookie.setPath("/");
            context.getResponse().addCookie(usernameCookie);

            user = new XWikiUser(principal.getName());
        } else {
            user =
                new XWikiUser(principal.getName().startsWith(context.getDatabase()) ? principal.getName().substring(
                    context.getDatabase().length() + 1) : principal.getName());
        }

        LOG.debug("XWikiUser=" + user);

        return user;
    }

    public Principal authenticate(String login, String password, XWikiContext context) throws XWikiException
    {
        Principal principal = null;

        String wikiName = context.getDatabase();

        // SSO authentication
        try {
            context.setDatabase(context.getMainXWiki());

            principal = authenticateSSOInContext(wikiName.equals(context.getMainXWiki()), context);
        } catch (XWikiException e) {
            LOG.debug("Failed to authenticate with SSO", e);
        } finally {
            context.setDatabase(wikiName);
        }

        // Falback on PUUMA authenticator
        if (principal == null) {
            principal = super.authenticate(login, password, context);
        }

        return principal;
    }

    public Principal authenticateSSOInContext(boolean local, XWikiContext context) throws XWikiException
    {
        LOG.debug("Authenticate SSO");

        XWikiRequest request = context.getRequest();

        Principal principal = null;

        if (request.getRemoteUser() == null) {
            LOG
                .warn("Failed to resolve remote user. It usually mean that no SSO information has been provided to XWiki.");

            return null;
        }

        LOG.debug("request remote user: " + request.getRemoteUser());

        // //////////////////////////////////////////////

        PumaHome pumaHome = getPumaHome(context);

        PumaProfile pumaProfile = pumaHome.getProfile(request);

        User user;
        String pumaUid;
        try {
            user = pumaProfile.getCurrentUser();
            pumaUid = pumaProfile.getIdentifier(user);
        } catch (PumaException e) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Can't get current user uid.", e);
        }

        LOG.debug("Current user uid: " + pumaUid);

        String validXWikiUserName = pumaUid.replace(".", "");

        // ////////////////////////////////////////////////////////////////////////
        // Synch user
        // ////////////////////////////////////////////////////////////////////////

        XWikiDocument userProfile = getUserProfileByUid(validXWikiUserName, pumaUid, context);

        syncUserFromPUMA(userProfile, pumaUid, user, pumaProfile, context);

        // ////////////////////////////////////////////////////////////////////////
        // Synch membership
        // ////////////////////////////////////////////////////////////////////////

        PumaLocator pl = pumaHome.getLocator(request);
        syncGroupsMembershipFromPUMA(userProfile, user, pl, pumaProfile, context);

        return principal;
    }

    protected PumaHome getPumaHome(XWikiContext context) throws XWikiException
    {
        try {
            PortletServiceHome portletServiceHome;

            javax.naming.Context ctx;
            ctx = new javax.naming.InitialContext();
            portletServiceHome =
                (PortletServiceHome) ctx.lookup("portletservice/com.ibm.portal.um.portletservice.PumaHome");

            if (portletServiceHome == null) {
                throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                    "Can't access PUMA service.");
            }

            return (PumaHome) portletServiceHome.getPortletService(PumaHome.class);
        } catch (NamingException e) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Can't access PUMA service.", e);
        } catch (PortletServiceUnavailableException e) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Can't access PUMA service.", e);
        }
    }

    protected XWikiDocument getUserProfileByUid(String validXWikiUserName, String pumaUid, XWikiContext context)
        throws XWikiException
    {
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);

        // Try default profile name (generally in the cache)
        XWikiDocument userProfile = context.getWiki().getDocument("XWiki." + validXWikiUserName, context);

        if (!pumaUid.equalsIgnoreCase(pumaXClass.getUid(userProfile))) {
            // Search for existing profile with provided uid
            userProfile = pumaXClass.searchDocumentByUid(pumaUid);

            // Resolve default profile patch of an uid
            if (userProfile == null) {
                userProfile = getAvailableUserProfile(validXWikiUserName, pumaUid, context);
            }
        }

        return userProfile;
    }

    protected XWikiDocument getAvailableUserProfile(String validXWikiUserName, String pumaUid, XWikiContext context)
        throws XWikiException
    {
        BaseClass userClass = context.getWiki().getUserClass(context);
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);

        String fullUserName = "XWiki." + validXWikiUserName;

        // Check if the default profile document is available
        for (int i = 0; true; ++i) {
            String profileName = fullUserName;

            if (i > 0) {
                profileName += "_" + i;
            }

            XWikiDocument doc = context.getWiki().getDocument(profileName, context);

            // Don't use non user existing document
            if (doc.isNew() || doc.getObject(userClass.getName()) != null) {
                String pumaUidFromObject = pumaXClass.getUid(doc);

                // If the user is a PUUMA user compare uids
                if (pumaUidFromObject == null || pumaUid.equalsIgnoreCase(pumaUidFromObject)) {
                    return doc;
                }
            }
        }
    }

    protected void syncUserFromPUMA(XWikiDocument userProfile, String pumaUid, User user, PumaProfile pumaProfile,
        XWikiContext context) throws XWikiException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LDAP attributes will be used to update XWiki attributes.");
        }

        Map<String, String> userMapping = getConfig().getUserMappings(context);
        Map<String, Object> pumaAttributes;
        try {
            pumaAttributes = pumaProfile.getAttributes(user, new ArrayList<String>(userMapping.keySet()));
        } catch (Exception e) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Impossible to retrieve user attributes for user [" + pumaUid + "] and attributes ["
                    + userMapping.keySet() + "]", e);
        }

        // Get attributes to synch
        Map<String, String> attributes = new HashMap<String, String>();
        for (Map.Entry<String, Object> pumaAttribute : pumaAttributes.entrySet()) {
            Object value = pumaAttribute.getValue();

            if (value instanceof String) {
                attributes.put(userMapping.get(pumaAttribute.getKey()), (String) value);
            } else {
                LOG.warn("Type [" + value.getClass() + "] for field [" + pumaAttribute.getKey()
                    + "] is not supported for PUMA user [" + pumaUid + "]");
            }
        }

        LOG.debug("Attributes to synchronize: " + attributes);

        // Sync
        if (userProfile.isNew()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating new XWiki user based on LDAP attribues located at [" + pumaUid + "]");
            }

            userProfile = createUserFromPUMA(userProfile, attributes, pumaUid, context);

            if (LOG.isDebugEnabled()) {
                LOG.debug("New XWiki user created: [" + userProfile.getFullName() + "] in wiki ["
                    + userProfile.getWikiName() + "]");
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Updating existing user with LDAP attribues located at " + pumaUid);
            }

            try {
                updateUserFromPUMA(userProfile, attributes, pumaUid, context);
            } catch (XWikiException e) {
                LOG.error("Failed to synchronise user's informations", e);
            }
        }
    }

    protected void syncGroupsMembershipFromPUMA(XWikiDocument userProfile, User user, PumaLocator pl,
        PumaProfile pumaProfile, XWikiContext context) throws XWikiException
    {
        // got valid group mappings
        Map<String, Collection<String>> groupMappings = getConfig().getGroupMappings(context);

        if (groupMappings != null && groupMappings.size() > 0) {
            try {
                Collection<String> xwikiGroupsIn = new ArrayList<String>();
                Collection<String> xwikiGroupsOut = new ArrayList<String>();

                List<Group> pumaUserGroups = pl.findGroupsByPrincipal(user, false);

                LOG.debug("The user belongs to following PUMA groups: ");

                // membership to add
                for (Group group : pumaUserGroups) {
                    String groupUid = pumaProfile.getIdentifier(user);

                    LOG.debug("  - " + groupUid);

                    Collection<String> xwikiGroups = groupMappings.get(groupUid);
                    if (xwikiGroups != null) {
                        xwikiGroupsIn.addAll(xwikiGroups);

                        groupMappings.remove(groupUid);
                    }
                }

                // membership to remove
                for (Collection<String> xwikiGroups : groupMappings.values()) {
                    xwikiGroupsOut.addAll(xwikiGroups);
                }

                // apply synch
                syncGroupsMembership(userProfile, xwikiGroupsIn, xwikiGroupsOut, context);
            } catch (Exception e) {
                LOG.error("Failed to synchronize groups for user [" + userProfile + "]", e);
            }
        }
    }

    protected void updateUserFromPUMA(XWikiDocument userProfile, Map<String, String> fields, String pumaUid,
        XWikiContext context) throws XWikiException
    {
        boolean needsUpdate = updateUser(userProfile, fields, context);

        // Update PUMA profile object
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);
        needsUpdate |= pumaXClass.updatePUMAObject(userProfile, pumaUid);

        if (needsUpdate) {
            context.getWiki().saveDocument(userProfile, context);
        }
    }

    protected XWikiDocument createUserFromPUMA(XWikiDocument userProfile, Map<String, String> fields, String pumaUid,
        XWikiContext context) throws XWikiException
    {
        XWikiDocument createdUserProfile = createUser(userProfile, fields, context);

        // Update ldap profile object
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);

        if (pumaXClass.updatePUMAObject(createdUserProfile, pumaUid)) {
            context.getWiki().saveDocument(createdUserProfile, context);
        }

        return createdUserProfile;
    }

    // GENERIC

    protected boolean updateUser(XWikiDocument userProfile, Map<String, String> fields, XWikiContext context)
        throws XWikiException
    {
        BaseClass userClass = context.getWiki().getUserClass(context);

        BaseObject userObj = userProfile.getObject(userClass.getName());

        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            if (userClass.get(key) == null) {
                continue;
            }
            String value = entry.getValue();

            String objValue = userObj.getStringValue(key);
            if (objValue == null || !objValue.equals(value)) {
                map.put(key, value);
            }
        }

        boolean needsUpdate = false;
        if (!map.isEmpty()) {
            userClass.fromMap(map, userObj);
            needsUpdate = true;
        }

        return needsUpdate;
    }

    protected XWikiDocument createUser(XWikiDocument userProfile, Map<String, String> fields, XWikiContext context)
        throws XWikiException
    {
        XWikiLDAPConfig config = XWikiLDAPConfig.getInstance();

        Map<String, String> userMappings = config.getUserMappings(null, context);

        BaseClass userClass = context.getWiki().getUserClass(context);

        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();

            String xattr = userMappings.get(key);

            if (xattr == null) {
                continue;
            }

            map.put(xattr, entry.getValue());
        }

        // Mark user active
        map.put("active", "1");

        String content;
        String syntaxId;
        if (!context.getWiki().getDefaultDocumentSyntax().equals(XWikiDocument.XWIKI10_SYNTAXID)) {
            content = "{{include document=\"XWiki.XWikiUserSheet\"/}}";
            syntaxId = XWikiDocument.XWIKI20_SYNTAXID;
        } else {
            content = "#includeForm(\"XWiki.XWikiUserSheet\")";
            syntaxId = XWikiDocument.XWIKI10_SYNTAXID;
        }

        context.getWiki().createUser(userProfile.getName(), map, userClass.getName(), content, syntaxId, "edit",
            context);

        return context.getWiki().getDocument(userProfile.getFullName(), context);
    }

    protected void syncGroupsMembership(XWikiDocument userProfile, Collection<String> xwikiGroupsIn,
        Collection<String> xwikiGroupsOut, XWikiContext context) throws XWikiException
    {
        String fullName = userProfile.getFullName();

        Collection<String> xwikiUserGroupList =
            context.getWiki().getGroupService(context).getAllGroupsNamesForMember(fullName, 0, 0, context);

        if (LOG.isDebugEnabled()) {
            LOG.debug("The user belongs to following XWiki groups: ");
            for (String userGroupName : xwikiUserGroupList) {
                LOG.debug(userGroupName);
            }
        }

        for (String xwikiGroupName : xwikiGroupsIn) {
            if (!xwikiUserGroupList.contains(xwikiGroupName)) {
                addUserToXWikiGroup(fullName, xwikiGroupName, context);
            }
        }

        for (String xwikiGroupName : xwikiGroupsOut) {
            if (xwikiUserGroupList.contains(xwikiGroupName)) {
                removeUserFromXWikiGroup(fullName, xwikiGroupName, context);
            }
        }
    }
}
