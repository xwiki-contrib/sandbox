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

package com.xwiki.authentication.trustedldap;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;

import com.novell.ldap.LDAPException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConfig;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConnection;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPException;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPSearchAttribute;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.LDAP.LDAPProfileXClass;
import com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl;
import com.xpn.xwiki.web.XWikiRequest;

public class TrustedLDAPAuthServiceImpl extends XWikiLDAPAuthServiceImpl
{
    /** LogFactory <code>LOGGER</code>. */
    private static final Log LOG = LogFactory.getLog(TrustedLDAPAuthServiceImpl.class);

    private TrustedLDAPConfig config = null;

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

    public void setConfig(TrustedLDAPConfig config)
    {
        this.config = config;
    }

    public TrustedLDAPConfig getConfig()
    {
        if (this.config == null) {
            this.config = new TrustedLDAPConfig();
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

        LOG.debug("XWikiUser: " + user);

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
            LOG.debug("Fallback on standard LDAP authenticator");

            user = super.checkAuth(username, password, rememberme, context);
        }

        LOG.debug("XWikiUser: " + user);

        return user;
    }

    public XWikiUser checkAuthSSO(String username, String password, XWikiContext context) throws XWikiException
    {
        Cookie cookie;

        LOG.debug("checkAuth");

        LOG.debug("Action: " + context.getAction());
        if (context.getAction().startsWith("logout")) {
            cookie = getCookie("XWIKISSOAUTHINFO", context);
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

        cookie = getCookie("XWIKISSOAUTHINFO", context);
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
            String encuname =
                encryptText(principal.getName().contains(":") ? principal.getName() : context.getDatabase() + ":"
                    + principal.getName(), context);
            Cookie usernameCookie = new Cookie("XWIKISSOAUTHINFO", encuname);
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

            principal = authenticateSSOInContext(login, password, wikiName.equals(context.getMainXWiki()), context);
        } catch (Exception e) {
            LOG.debug("Failed to authenticate with SSO", e);
        } finally {
            context.setDatabase(wikiName);
        }

        // Falback on LDAP authenticator
        if (principal == null) {
            principal = super.authenticate(login, password, context);
        }

        return principal;
    }

    protected Map<String, String> parseRemoteUser(String ssoRemoteUser, XWikiContext context)
    {
        Map<String, String> ldapConfiguration = new HashMap<String, String>();

        ldapConfiguration.put("login", ssoRemoteUser);

        Pattern remoteUserParser = getConfig().getRemoteUserParser(context);

        LOG.debug("remoteUserParser: " + remoteUserParser);

        if (remoteUserParser != null) {
            Matcher marcher = remoteUserParser.matcher(ssoRemoteUser);

            if (marcher.find()) {
                int groupCount = marcher.groupCount();
                if (groupCount == 0) {
                    ldapConfiguration.put("login", marcher.group());
                } else {
                    for (int g = 1; g <= groupCount; ++g) {
                        String groupValue = marcher.group(g);

                        List<String> remoteUserMapping = getConfig().getRemoteUserMapping(g, context);

                        for (String configName : remoteUserMapping) {
                            ldapConfiguration
                                .put(configName, convertRemoteUserMapping(configName, groupValue, context));
                        }
                    }
                }
            }
        }

        return ldapConfiguration;
    }

    private String convertRemoteUserMapping(String propertyName, String propertyValue, XWikiContext context)
    {
        Map<String, String> hostConvertor = getConfig().getRemoteUserMapping(propertyName, true, context);
        String converted = hostConvertor.get(propertyValue.toLowerCase());

        return converted != null ? converted : propertyValue;
    }

    public boolean open(XWikiLDAPConnection connector, Map<String, String> remoteUserLdapConfiguration,
        XWikiContext context) throws XWikiLDAPException
    {
        XWikiLDAPConfig config = XWikiLDAPConfig.getInstance();

        // open LDAP
        int ldapPort = config.getLDAPPort(context);
        String ldapHost = remoteUserLdapConfiguration.get("ldap_server");
        if (ldapHost == null) {
            ldapHost = config.getLDAPParam("ldap_server", "localhost", context);
        }

        String ldapUserName = remoteUserLdapConfiguration.get("login");
        String password = remoteUserLdapConfiguration.get("password");

        // allow to use the given user and password also as the LDAP bind user and password
        String bindDN = config.getLDAPBindDN(ldapUserName, password, context);
        String bindPassword = config.getLDAPBindPassword(ldapUserName, password, context);

        boolean bind;
        if ("1".equals(config.getLDAPParam("ldap_ssl", "0", context))) {
            String keyStore = config.getLDAPParam("ldap_ssl.keystore", "", context);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Connecting to LDAP using SSL");
            }

            bind = connector.open(ldapHost, ldapPort, bindDN, bindPassword, keyStore, true, context);
        } else {
            bind = connector.open(ldapHost, ldapPort, bindDN, bindPassword, null, false, context);
        }

        return bind;
    }

    public Principal authenticateSSOInContext(String login, String password, boolean local, XWikiContext context)
        throws XWikiException, UnsupportedEncodingException, LDAPException
    {
        LOG.debug("Authenticate SSO");

        XWikiRequest request = context.getRequest();

        boolean checkAuth = false;
        Principal principal = null;

        String ssoRemoteUser = request.getRemoteUser();

        if (ssoRemoteUser == null) {
            // try using provided user name/password if no SSO information is provided
            ssoRemoteUser = login;
            checkAuth = true;

            if (ssoRemoteUser == null) {
                LOG.warn("Failed to resolve remote user. It usually mean that no SSO information has been provided to XWiki.");

                return null;
            }
        }

        LOG.debug("request remote user: " + ssoRemoteUser);

        // ////////////////////////////////////////////////////////////////////
        // Extract LDAP informations from remote user
        // ////////////////////////////////////////////////////////////////////

        Map<String, String> remoteUserLdapConfiguration = parseRemoteUser(ssoRemoteUser, context);

        // provide form password
        if (!remoteUserLdapConfiguration.containsKey("password")) {
            remoteUserLdapConfiguration.put("password", password);
        }

        String ldapUid = remoteUserLdapConfiguration.get("login");
        String validXWikiUserName = ldapUid.replace(".", "");

        LOG.debug("ldapUid: " + ldapUid);
        LOG.debug("validXWikiUserName: " + validXWikiUserName);

        // ////////////////////////////////////////////////////////////////////
        // LDAP
        // ////////////////////////////////////////////////////////////////////

        XWikiLDAPConnection connector = new XWikiLDAPConnection();
        XWikiLDAPUtils ldapUtils = new XWikiLDAPUtils(connector);

        XWikiLDAPConfig config = XWikiLDAPConfig.getInstance();
        ldapUtils.setUidAttributeName(config.getLDAPParam(XWikiLDAPConfig.PREF_LDAP_UID, "cn", context));
        ldapUtils.setGroupClasses(config.getGroupClasses(context));
        ldapUtils.setGroupMemberFields(config.getGroupMemberFields(context));
        ldapUtils.setUserSearchFormatString(config.getLDAPParam("ldap_user_search_fmt", "({0}={1})", context));

        ldapUtils.setBaseDN(remoteUserLdapConfiguration.containsKey("ldap_base_DN") ? remoteUserLdapConfiguration
            .get("ldap_base_DN") : config.getLDAPParam("ldap_base_DN", "", context));

        // ////////////////////////////////////////////////////////////////////
        // bind to LDAP
        // ////////////////////////////////////////////////////////////////////

        if (!open(connector, remoteUserLdapConfiguration, context)) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Bind to LDAP server failed.");
        }

        // ////////////////////////////////////////////////////////////////////
        // find XWiki user profile page
        // ////////////////////////////////////////////////////////////////////

        LDAPProfileXClass ldapProfileClass = new LDAPProfileXClass(context);

        XWikiDocument userProfile = getUserProfileByUid(validXWikiUserName, ldapUid, context);

        // get DN from existing XWiki user
        String ldapDn = ldapProfileClass.getDn(userProfile);

        if (LOG.isDebugEnabled() && ldapDn != null) {
            LOG.debug("Found user dn with the user object: " + ldapDn);
        }

        List<XWikiLDAPSearchAttribute> searchAttributes = null;

        // if we still don't have a dn, search for it. Also get the attributes, we might need
        // them
        if (ldapDn == null) {
            searchAttributes = ldapUtils.searchUserAttributesByUid(ldapUid, getAttributeNameTable(context));

            if (searchAttributes != null) {
                for (XWikiLDAPSearchAttribute searchAttribute : searchAttributes) {
                    if ("dn".equals(searchAttribute.name)) {
                        ldapDn = searchAttribute.value;

                        break;
                    }
                }
            }
        }

        if (ldapDn == null) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Can't find LDAP user DN for [" + ldapUid + "]");
        }

        // if using form user/password, validate it
        if (checkAuth) {
            if ("1".equals(config.getLDAPParam("ldap_validate_password", "0", context))) {
                String passwordField = config.getLDAPParam("ldap_password_field", "userPassword", context);
                if (!connector.checkPassword(ldapDn, password, passwordField)) {
                    LOG.debug("Password comparison failed, are you really sure you need validate_password ?"
                        + " If you don't enable it, it does not mean user credentials are not validated."
                        + " The goal of this property is to bypass standard LDAP bind"
                        + " which is usually bad unless you really know what you do.");

                    throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                        "LDAP authentication failed:" + " could not validate the password: wrong password for "
                            + ldapDn);
                }
            } else {
                String bindDNFormat = config.getLDAPBindDN(context);
                String bindDN = config.getLDAPBindDN(ldapUid, password, context);

                if (bindDNFormat.equals(bindDN)) {
                    // Validate user credentials
                    connector.bind(ldapDn, password);

                    // Rebind admin user
                    connector.bind(bindDN, config.getLDAPBindPassword(ldapUid, password, context));
                }
            }
        }

        // ////////////////////////////////////////////////////////////////////
        // sync user
        // ////////////////////////////////////////////////////////////////////

        boolean isNewUser = userProfile.isNew();

        syncUser(userProfile, searchAttributes, ldapDn, ldapUid, ldapUtils, context);

        // from now on we can enter the application
        if (local) {
            principal = new SimplePrincipal(userProfile.getFullName());
        } else {
            principal = new SimplePrincipal(context.getDatabase() + ":" + userProfile.getFullName());
        }

        // ////////////////////////////////////////////////////////////////////
        // sync groups membership
        // ////////////////////////////////////////////////////////////////////

        try {
            syncGroupsMembership(userProfile.getFullName(), ldapDn, isNewUser, ldapUtils, context);
        } catch (XWikiException e) {
            LOG.error("Failed to synchronise user's groups membership", e);
        }

        LOG.debug("Principal=" + principal);

        return principal;
    }
}
