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

package com.xpn.xwiki.user.impl.NTLM;

import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConnection;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPSearchAttribute;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPUtils;
import com.xpn.xwiki.plugin.ldap.XWikiLDAPConfig;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.LDAP.LDAPProfileXClass;
import com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl;
import jcifs.UniAddress;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbSession;
import jcifs.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

public class NTLMAuthServiceImpl extends XWikiLDAPAuthServiceImpl
{
    /** LogFactory <code>LOGGER</code>. */
    private static final Log LOG = LogFactory.getLog(NTLMAuthServiceImpl.class);

    private NTLMConfig config = null;

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
                String encoded = Base64.encode(encrypted);
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
                byte[] encrypted = Base64.decode(text.replaceAll("_", "="));
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

    public XWikiUser checkAuth(String a, String b, String c, XWikiContext context) throws XWikiException
    {
        LOG.debug("checkAuth2");

        return checkAuth(context);
    }

    public void setConfig(NTLMConfig config)
    {
        this.config = config;
    }

    public NTLMConfig getConfig()
    {
        if (this.config == null) {
            this.config = new NTLMConfig();
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

    public XWikiUser checkAuth(XWikiContext context) throws XWikiException
    {
        Cookie cookie;

        LOG.debug("checkAuth");

        LOG.debug("Action: " + context.getAction());
        if (context.getAction().startsWith("logout")) {
            cookie = getCookie("XWIKINTLMAUTHINFO", context);
            if (cookie != null) {
                cookie.setMaxAge(0);
                context.getResponse().addCookie(cookie);
            }

            return null;
        }

        Principal principal = null;

        Cookie[] cookies = context.getRequest().getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                LOG.debug("CookieList: " + c.getName() + " => " + c.getValue());
            }
        }

        cookie = getCookie("XWIKINTLMAUTHINFO", context);
        if (cookie != null) {
            LOG.debug("Found Cookie");
            String uname = decryptText(cookie.getValue(), context);
            if (uname != null) {
                principal = new SimplePrincipal(uname);
            }
        }

        String msg = context.getRequest().getHeader("Authorization");
        if (msg != null) {
            LOG.debug("Found NTLM Auth Cookie, this could be an IE6 bug (#831167)");
            if (msg.startsWith("NTLM ")) {
                LOG.debug("Removing principal because of NTLM header");
                principal = null;
            }
        }

        // Authenticate
        if (principal == null) {
            principal = authenticate(null, null, context);
            if (principal == null) {
                LOG.debug("Can't get principal");
                return null;
            }

            LOG.debug("Saving auth cookie");
            String encuname = encryptText(principal.getName(), context);
            Cookie usernameCookie = new Cookie("XWIKINTLMAUTHINFO", encuname);
            usernameCookie.setMaxAge(-1);
            context.getResponse().addCookie(usernameCookie);
        }

        return new XWikiUser(principal.getName());
    }

    public Principal authenticate(String username, String password, XWikiContext context) throws XWikiException
    {
        NtlmPasswordAuthentication ntlm;
        UniAddress dc;
        String domainController = getConfig().getParam("domainController", context);
        String defaultDomain = getConfig().getParam("defaultDomain", context);

        LOG.debug("authenticate");
        ntlm = null;
        if (ntlm == null) {
            LOG.debug("no NTLM attr");

            String msg = context.getRequest().getHeader("Authorization");
            try {
                if (msg != null && (msg.startsWith("NTLM ") || msg.startsWith("Basic "))) {
                    LOG.debug("Auth type: " + msg);
                    LOG.debug("domainController: " + domainController);
                    dc = UniAddress.getByName(domainController, true);
                    if (msg.startsWith("NTLM ")) {
                        byte[] challenge = SmbSession.getChallenge(dc);
                        byte[] src = Base64.decode(msg.substring(5));
                        if (src[8] == 1) {
                            LOG.debug("phase 1");
                            Type1Message type1 = new Type1Message(src);
                            Type2Message type2 = new Type2Message(type1, challenge, null);
                            msg = Base64.encode(type2.toByteArray());
                            context.getResponse().setHeader("WWW-Authenticate", "NTLM " + msg);
                            context.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            context.getResponse().flushBuffer();

                            return null;
                        } else if (src[8] == 3) {
                            LOG.debug("phase 2");
                            Type3Message type3 = new Type3Message(src);
                            byte[] lmResponse = type3.getLMResponse();
                            if (lmResponse == null)
                                lmResponse = new byte[0];
                            byte[] ntResponse = type3.getNTResponse();
                            if (ntResponse == null)
                                ntResponse = new byte[0];
                            ntlm =
                                    new NtlmPasswordAuthentication(type3.getDomain(), type3.getUser(), challenge,
                                        lmResponse, ntResponse);
                        }
                    } else {
                        LOG.debug("Basic session");
                        String auth = new String(Base64.decode(msg.substring(6)), "US-ASCII");
                        int index = auth.indexOf(':');
                        String user = (index != -1) ? auth.substring(0, index) : auth;
                        String pass = (index != -1) ? auth.substring(index + 1) : "";
                        index = user.indexOf('\\');
                        if (index == -1)
                            index = user.indexOf('/');
                        String domain = (index != -1) ? user.substring(0, index) : defaultDomain;
                        user = (index != -1) ? user.substring(index + 1) : user;
                        ntlm = new NtlmPasswordAuthentication(domain, user, pass);
                    }

                    try {
                        SmbSession.logon(dc, ntlm);
                    } catch (SmbAuthException sae) {
                        LOG.debug("Can't logon");
                        
                        return null;
                    }
                } else {
                    LOG.debug("No auth type");
                    showLogin(context);
                    
                    return null;
                }
            } catch (Exception e) {
                LOG.debug("Got exception: ", e);

                return null;
            }
        }

        String ldapUid = ntlm.getUsername();
        String validXWikiUserName = ntlm.getUsername();

        XWikiLDAPConnection connector = new XWikiLDAPConnection();
        XWikiLDAPUtils ldapUtils = new XWikiLDAPUtils(connector);

        XWikiLDAPConfig config = XWikiLDAPConfig.getInstance();
        ldapUtils.setUidAttributeName(config.getLDAPParam(XWikiLDAPConfig.PREF_LDAP_UID, "cn", context));
        ldapUtils.setGroupClasses(config.getGroupClasses(context));
        ldapUtils.setGroupMemberFields(config.getGroupMemberFields(context));
        ldapUtils.setBaseDN(config.getLDAPParam("ldap_base_DN", "", context));
        ldapUtils.setUserSearchFormatString(config.getLDAPParam("ldap_user_search_fmt", "({0}={1})", context));

        if (!connector.open(ldapUid, password, context)) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Bind to LDAP server failed.");
        }

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

        // 8. sync user
        boolean isNewUser = userProfile.isNew();
        syncUser(userProfile, searchAttributes, ldapDn, ldapUid, ldapUtils, context);

        // from now on we can enter the application
        Principal principal = new SimplePrincipal(context.getDatabase() + ":" + userProfile.getFullName());

        LOG.debug("Principal: " + principal.toString());

        // 9. sync groups membership

        try {
            syncGroupsMembership(userProfile.getFullName(), ldapDn, isNewUser, ldapUtils, context);
        } catch (XWikiException e) {
            LOG.error("Failed to synchronise user's groups membership", e);
        }

        return principal;
    }

    public void showLogin(XWikiContext context) throws XWikiException
    {
        String realm = getConfig().getParam("realm", "XWiki NTLM", context);

        LOG.debug("showLogin");
        context.getResponse().setHeader("WWW-Authenticate", "NTLM");
        context.getResponse().addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");

        context.getResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
