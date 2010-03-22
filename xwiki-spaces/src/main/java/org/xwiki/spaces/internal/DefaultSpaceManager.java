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
package org.xwiki.spaces.internal;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.spaces.IllegalSpaceKeyException;
import org.xwiki.spaces.SpaceAlreadyExistsException;
import org.xwiki.spaces.SpaceManager;
import org.xwiki.spaces.SpaceManagerConfiguration;
import org.xwiki.spaces.SpaceManagerException;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Implementation of the space manager that relies on the "old XWiki core" to manipulate document and objects. This is a
 * choice versus using lot of bridged code, since in both case the implementation will need a rewrite when we have the
 * new data model/store component in place ; and xwiki-core manipulation is more handy than DocumentAccessBridge.
 * 
 * @version $Id$
 */
@Component
public class DefaultSpaceManager extends AbstractLogEnabled implements SpaceManager
{

    /** The document name part of the global rights class document reference. */
    public static final String GLOBAL_RIGHTS_CLASSNAME = "XWikiGlobalRights";

    /** The <tt>level</tt> field of the {@link #GLOBAL_RIGHTS_CLASSNAME} class. */
    private static final String LEVEL_FIELD = "level";

    /** The <tt>group</tt> field of the {@link #GLOBAL_RIGHTS_CLASSNAME} class. */
    private static final String GROUP_FIELD = "group";

    /** The <tt>allow</tt> field of the {@link #GLOBAL_RIGHTS_CLASSNAME} class. */
    private static final String ALLOW_FIELD = "allow";

    /** The name of the space XWiki. */
    private static final String XWIKI_SPACENAME = "XWiki";

    /** Configuration of the space manager module. */
    @Requirement
    private SpaceManagerConfiguration configuration;

    /** Execution needed to get access to the XWiki context. */
    @Requirement
    private Execution execution;
    
    /**
     * {@inheritDoc}
     * 
     * @see SpaceManager#isLegalSpaceKey(String)
     */
    public boolean isLegalSpaceKey(String key)
    {
        if (!StringUtils.isEmpty(this.getValidationRegex()) && !key.matches(this.getValidationRegex())) {
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see SpaceManager#createSpace(String, String)
     */
    public void createSpace(String key, String name) throws SpaceAlreadyExistsException, SpaceManagerException,
        IllegalSpaceKeyException
    {
        this.createSpace(key, name, configuration.getDefaultSpaceType());
    }

    /**
     * {@inheritDoc}
     * 
     * @see SpaceManager#createSpace(String, String, String)
     */
    public void createSpace(String key, String name, String type) throws SpaceAlreadyExistsException,
        SpaceManagerException, IllegalSpaceKeyException
    {

        if (!this.isLegalSpaceKey(key)) {
            throw new IllegalSpaceKeyException(MessageFormat.format("Invalid key. Accepted pattern is [{0}]",
                new Object[] {this.getValidationRegex()}));
        }

        XWikiContext context = getXWikiContext();
        DocumentReference spaceHomeReference = new DocumentReference(getXWikiContext().getDatabase(), key, "WebHome");

        try {
            // Retrieve the document that will be the home of the space for that organization.
            XWikiDocument spaceHome = context.getWiki().getDocument(spaceHomeReference, context);

            DocumentReference spacePrefsReference =
                new DocumentReference(getXWikiContext().getDatabase(), key, "WebPreferences");
            XWikiDocument spacePrefs = context.getWiki().getDocument(spacePrefsReference, context);

            // Check the organization does not already exists.
            if (!spaceHome.isNew() || !spacePrefs.isNew()) {
                String message =
                    MessageFormat.format(
                        "Failed to create organization with key [{0}] as it already exists in the wiki.",
                        new Object[] {key});
                if (getLogger().isErrorEnabled()) {
                    getLogger().error(message);
                }
                throw new SpaceAlreadyExistsException(message);
            }

            // Setup the home of the space.
            spaceHome.setSyntax(Syntax.XWIKI_2_0);
            if (!StringUtils.isBlank(configuration.getSpaceHomeInclude())) {
                spaceHome.setContent("{{include document='" + configuration.getSpaceHomeInclude() + "' /}}");
            }

            BaseObject space =
                spaceHome.newXObject(new DocumentReference(context.getDatabase(), XWIKI_SPACENAME, "SpaceClass"),
                    context);
            space.set("name", name, context);
            space.set("type", type, context);

            context.getWiki().saveDocument(spaceHome, "Created home document for space " + key, context);

            // Setup the preferences of the space.
            this.setupPreferencesDocument(key, spacePrefs, context);

        } catch (XWikiException e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error(
                    MessageFormat.format("Failed to create organization with key [{0}]. Reason: ", new Object[] {key}));
            }
            throw new SpaceManagerException(e);
        }
    }

    /**
     * Create the initial version of the preference document for the space if default rights for either managers of the
     * space or members of the space of both are defined in the configuration.
     * 
     * @param key the technical name of the space
     * @param spacePrefs the preferences document
     * @param context the XWiki context
     * @throws XWikiException when saving the preferences document fails.
     */
    private void setupPreferencesDocument(String key, XWikiDocument spacePrefs, XWikiContext context)
        throws XWikiException
    {
        String managersLevels = configuration.getSpaceManagersAccessLevels();
        String membersLevels = configuration.getSpaceMembersAccessLevels();

        if (!StringUtils.isBlank(managersLevels)) {
            BaseObject spaceManagersRights =
                spacePrefs.newXObject(new DocumentReference(context.getDatabase(), XWIKI_SPACENAME,
                    GLOBAL_RIGHTS_CLASSNAME), context);
            spaceManagersRights.set(ALLOW_FIELD, "1", context);
            spaceManagersRights.set(GROUP_FIELD, key + ".ManagersGroup", context);
            spaceManagersRights.set(LEVEL_FIELD, "view,comment,edit", context);
        }

        if (!StringUtils.isBlank(membersLevels)) {
            BaseObject spaceMembersRights =
                spacePrefs.newXObject(new DocumentReference(context.getDatabase(), XWIKI_SPACENAME,
                    GLOBAL_RIGHTS_CLASSNAME), context);
            spaceMembersRights.set(ALLOW_FIELD, "1", context);
            spaceMembersRights.set(GROUP_FIELD, key + ".MembersGroup", context);
            spaceMembersRights.set(LEVEL_FIELD, "view,comment", context);
        }

        if (!StringUtils.isBlank(managersLevels) && StringUtils.isBlank(membersLevels)) {
            // Save the document only if we touched it.
            context.getWiki().saveDocument(spacePrefs, "Created preferences document for space " + key, context);
        }
    }

    /**
     * @return the XWiki context, retrieve from our {@link #execution}.
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    /**
     * @return the regular expression to validate space keys against.
     */
    private String getValidationRegex()
    {
        return configuration.getSpaceNameValidationRegex();
    }

}
