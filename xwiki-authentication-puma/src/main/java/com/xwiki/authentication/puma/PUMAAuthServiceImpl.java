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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.securityfilter.realm.SimplePrincipal;

import com.ibm.portal.portlet.service.PortletServiceHome;
import com.ibm.portal.portlet.service.PortletServiceUnavailableException;
import com.ibm.portal.um.Group;
import com.ibm.portal.um.PumaLocator;
import com.ibm.portal.um.PumaProfile;
import com.ibm.portal.um.User;
import com.ibm.portal.um.exceptions.PumaException;
import com.ibm.portal.um.portletservice.PumaHome;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xpn.xwiki.web.XWikiRequest;
import com.xwiki.authentication.AbstractSSOAuthServiceImpl;

/**
 * Authenticate using IBM WebSphere Portal PUMA api.
 * 
 * @version $Id$
 */
public class PUMAAuthServiceImpl extends AbstractSSOAuthServiceImpl
{
    /**
     * LogFactory <code>LOGGER</code>.
     */
    private static final Log LOG = LogFactory.getLog(PUMAAuthServiceImpl.class);

    private PUMAConfig config = null;

    private XWikiAuthService falback = null;

    private PUMAConfig getConfig()
    {
        if (this.config == null) {
            this.config = new PUMAConfig();
        }

        return this.config;
    }

    protected XWikiAuthService getFalback(XWikiContext context)
    {
        if (this.falback == null) {
            this.falback = getConfig().getFalbackAuthenticator(context);
        }

        return falback;
    }

    protected Principal authenticateInContext(boolean local, XWikiContext context) throws XWikiException
    {
        System.out.println("Authenticate SSO");

        XWikiRequest request = context.getRequest();

        Principal principal = null;

        if (request.getRemoteUser() == null) {
            LOG
                .debug("Failed to resolve remote user. It usually mean that no SSO information has been provided to XWiki.");

            return null;
        }

        System.out.println("Request remote user: " + request.getRemoteUser());

        String ssoUser = request.getRemoteUser();

        XWikiDocument userProfile = getUserProfileByUid(ssoUser.replace(".", ""), ssoUser, context);

        System.out.println("XWiki user resolved profile name: " + userProfile.getFullName());

        // //////////////////////////////////////////////
        // Get PUMA profile
        // //////////////////////////////////////////////

        PumaHome pumaHome = getPumaHome(context);

        PumaProfile pumaProfile = pumaHome.getProfile(request);

        User user;
        try {
            user = pumaProfile.getCurrentUser();
        } catch (PumaException e) {
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                "Can't get current user uid.", e);
        }

        // ////////////////////////////////////////////////////////////////////////
        // Synch user
        // ////////////////////////////////////////////////////////////////////////

        syncUserFromPUMA(userProfile, ssoUser, user, pumaProfile, context);

        System.out.println("User [" + userProfile.getFullName() + "] synchronized");

        // from now on we can enter the application
        if (local) {
            principal = new SimplePrincipal(userProfile.getFullName());
        } else {
            principal = new SimplePrincipal(context.getDatabase() + ":" + userProfile.getFullName());
        }

        // ////////////////////////////////////////////////////////////////////////
        // Synch membership
        // ////////////////////////////////////////////////////////////////////////

        PumaLocator pl = pumaHome.getLocator(request);
        syncGroupsMembershipFromPUMA(userProfile, user, pl, pumaProfile, context);

        System.out.println("User [" + userProfile.getFullName() + "] membership synchronized");

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

    protected XWikiDocument getUserProfileByUid(String validXWikiUserName, String ssoUser, XWikiContext context)
        throws XWikiException
    {
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);

        // Try default profile name (generally in the cache)
        XWikiDocument userProfile = context.getWiki().getDocument("XWiki." + validXWikiUserName, context);

        if (!ssoUser.equalsIgnoreCase(pumaXClass.getUid(userProfile))) {
            // Search for existing profile with provided uid
            userProfile = pumaXClass.searchDocumentByUid(ssoUser);

            // Resolve default profile patch of an uid
            if (userProfile == null) {
                userProfile = getAvailableUserProfile(validXWikiUserName, ssoUser, context);
            }
        }

        return userProfile;
    }

    protected XWikiDocument getAvailableUserProfile(String validXWikiUserName, String ssoUser, XWikiContext context)
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
                if (pumaUidFromObject == null || ssoUser.equalsIgnoreCase(pumaUidFromObject)) {
                    return doc;
                }
            }
        }
    }

    protected void syncUserFromPUMA(XWikiDocument userProfile, String ssoUser, User user, PumaProfile pumaProfile,
        XWikiContext context) throws XWikiException
    {
        // Get attributes to synch
        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, String> userMapping = getConfig().getUserMapping(context);
        if (userMapping != null) {
            Map<String, Object> pumaAttributes;
            try {
                pumaAttributes = pumaProfile.getAttributes(user, new ArrayList<String>(userMapping.keySet()));
            } catch (Exception e) {
                throw new XWikiException(XWikiException.MODULE_XWIKI_USER, XWikiException.ERROR_XWIKI_USER_INIT,
                    "Impossible to retrieve user attributes for user [" + ssoUser + "] and attributes ["
                        + userMapping.keySet() + "]", e);
            }

            for (Map.Entry<String, Object> pumaAttribute : pumaAttributes.entrySet()) {
                Object value = pumaAttribute.getValue();

                if (value instanceof String) {
                    attributes.put(userMapping.get(pumaAttribute.getKey()), (String) value);
                } else {
                    LOG.warn("Type [" + (value != null ? value.getClass() : null) + "] for field ["
                        + pumaAttribute.getKey() + "] is not supported for PUMA user [" + ssoUser + "]");
                }
            }

            System.out.println("Attributes to synchronize: " + attributes);
        }

        // Sync
        if (userProfile.isNew()) {
            System.out.println("Creating new XWiki user based on LDAP attribues located at [" + ssoUser + "]");

            userProfile = createUserFromPUMA(userProfile, attributes, ssoUser, context);

            System.out.println("New XWiki user created: [" + userProfile.getFullName() + "] in wiki ["
                + userProfile.getWikiName() + "]");
        } else {
            System.out.println("Updating existing user with LDAP attribues located at " + ssoUser);

            try {
                updateUserFromPUMA(userProfile, attributes, ssoUser, context);
            } catch (XWikiException e) {
                LOG.error("Failed to synchronise user's informations", e);
            }
        }
    }

    protected void syncGroupsMembershipFromPUMA(XWikiDocument userProfile, User user, PumaLocator pl,
        PumaProfile pumaProfile, XWikiContext context) throws XWikiException
    {
        // got valid group mappings
        Map<String, Collection<String>> groupMappings = getConfig().getGroupMapping(context);

        if (groupMappings != null && groupMappings.size() > 0) {
            try {
                Collection<String> xwikiGroupsIn = new ArrayList<String>();
                Collection<String> xwikiGroupsOut = new ArrayList<String>();

                Map<String, Collection<String>> groupsToRemove = new HashMap<String, Collection<String>>(groupMappings);

                List<Group> pumaUserGroups = pl.findGroupsByPrincipal(user, false);

                System.out.println("The user belongs to following PUMA groups: ");

                // membership to add
                for (Group group : pumaUserGroups) {
                    String groupUid = pumaProfile.getIdentifier(group);

                    System.out.println("  - " + groupUid);

                    Collection<String> xwikiGroups = groupsToRemove.get(groupUid);
                    if (xwikiGroups != null) {
                        xwikiGroupsIn.addAll(xwikiGroups);

                        groupsToRemove.remove(groupUid);
                    }
                }

                // membership to remove
                for (Collection<String> xwikiGroups : groupsToRemove.values()) {
                    xwikiGroupsOut.addAll(xwikiGroups);
                }

                // apply synch
                syncGroupsMembership(userProfile, xwikiGroupsIn, xwikiGroupsOut, context);
            } catch (Exception e) {
                LOG.error("Failed to synchronize groups for user [" + userProfile + "]", e);
            }
        }
    }

    protected void updateUserFromPUMA(XWikiDocument userProfile, Map<String, String> fields, String ssoUser,
        XWikiContext context) throws XWikiException
    {
        boolean needsUpdate = updateUser(userProfile, fields, context);

        // Update PUMA profile object
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);
        needsUpdate |= pumaXClass.updatePUMAObject(userProfile, ssoUser);

        if (needsUpdate) {
            context.getWiki().saveDocument(userProfile, context);
        }
    }

    protected XWikiDocument createUserFromPUMA(XWikiDocument userProfile, Map<String, String> fields, String ssoUser,
        XWikiContext context) throws XWikiException
    {
        XWikiDocument createdUserProfile = createUser(userProfile, fields, context);

        // Update ldap profile object
        PUMAProfileXClass pumaXClass = new PUMAProfileXClass(context);

        if (pumaXClass.updatePUMAObject(createdUserProfile, ssoUser)) {
            context.getWiki().saveDocument(createdUserProfile, context);
        }

        return createdUserProfile;
    }
}
