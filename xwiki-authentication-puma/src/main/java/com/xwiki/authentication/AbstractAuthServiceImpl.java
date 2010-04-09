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

package com.xwiki.authentication;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;

/**
 * Base authenticator. Provide some common tools.
 * 
 * @version $Id$
 */
public abstract class AbstractAuthServiceImpl extends XWikiAuthServiceImpl
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Log LOG = LogFactory.getLog(AbstractAuthServiceImpl.class);

    protected XWikiAuthService getFallback(XWikiContext context)
    {
        return null;
    }

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
        BaseClass userClass = context.getWiki().getUserClass(context);

        Map<String, String> map = new HashMap<String, String>(fields);

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

        return context.getWiki().getDocument(userProfile, context);
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

    /**
     * Remove user name from provided XWiki group.
     * 
     * @param xwikiUserName the full name of the user.
     * @param groupName the name of the group.
     * @param context the XWiki context.
     */
    protected void removeUserFromXWikiGroup(String xwikiUserName, String groupName, XWikiContext context)
    {
        try {
            String groupClassName = context.getWiki().getGroupClass(context).getName();

            // Get the XWiki document holding the objects comprising the group membership list
            XWikiDocument groupDoc = context.getWiki().getDocument(groupName, context);

            // Get and remove the specific group membership object for the user
            BaseObject groupObj = groupDoc.getObject(groupClassName, "member", xwikiUserName);
            groupDoc.removeObject(groupObj);

            // Save modifications
            context.getWiki().saveDocument(groupDoc, context);
        } catch (Exception e) {
            LOG.error("Failed to remove a user from a group " + xwikiUserName + " group: " + groupName, e);
        }
    }

    /**
     * Add user name to provided XWiki group.
     * 
     * @param xwikiUserName the full name of the user.
     * @param groupName the name of the group.
     * @param context the XWiki context.
     */
    protected void addUserToXWikiGroup(String xwikiUserName, String groupName, XWikiContext context)
    {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format("Adding user {0} to xwiki group {1}", xwikiUserName, groupName));
            }

            BaseClass groupClass = context.getWiki().getGroupClass(context);

            // Get document representing group
            XWikiDocument groupDoc = context.getWiki().getDocument(groupName, context);

            // Add a member object to document
            BaseObject memberObj = groupDoc.newObject(groupClass.getName(), context);
            Map<String, String> map = new HashMap<String, String>();
            map.put("member", xwikiUserName);
            groupClass.fromMap(map, memberObj);

            // Save modifications
            context.getWiki().saveDocument(groupDoc, context);

            if (LOG.isDebugEnabled()) {
                LOG
                    .debug(MessageFormat
                        .format("Finished adding user {0} to xwiki group {1}", xwikiUserName, groupName));
            }

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Failed to add a user [{0}] to a group [{1}]", xwikiUserName, groupName), e);
        }
    }

    public Principal authenticate(String login, String password, XWikiContext context) throws XWikiException
    {
        Principal principal = null;

        String wikiName = context.getDatabase();

        // SSO authentication
        try {
            context.setDatabase(context.getMainXWiki());

            principal = authenticateInContext(wikiName.equals(context.getMainXWiki()), context);
        } catch (XWikiException e) {
            LOG.debug("Failed to authenticate with SSO", e);
        } finally {
            context.setDatabase(wikiName);
        }

        // Falback on configured authenticator
        if (principal == null) {
            XWikiAuthService fallback = getFallback(context);

            if (fallback != null) {
                LOG.debug("Fallback on authenticator " + fallback);

                principal = fallback.authenticate(login, password, context);
            }
        }

        LOG.debug("Principal: " + principal);

        return principal;
    }

    protected abstract Principal authenticateInContext(boolean local, XWikiContext context) throws XWikiException;
}
