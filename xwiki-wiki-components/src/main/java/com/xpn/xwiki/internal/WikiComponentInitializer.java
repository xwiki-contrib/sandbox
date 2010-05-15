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
package com.xpn.xwiki.internal;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xwiki.bridge.XWikiInitializedBridgeEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.wiki.InvalidComponentDefinitionException;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.component.wiki.WikiComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.user.api.XWikiRightService;

/**
 * Initializes the Wiki Component feature. First ensure all needed XClasses are up-to-date, then registers existing
 * components.
 * 
 * @since 2.4-M2
 * @version $Id$
 */
@Component("wikiComponentInitializer")
public class WikiComponentInitializer extends AbstractLogEnabled implements EventListener
{
    /**
     * The XClass defining a component implementation.
     */
    private static final String WIKI_COMPONENT_CLASS = "XWiki.ComponentClass";

    /**
     * The XClass defining a component requirement.
     */
    private static final String WIKI_COMPONENT_REQUIREMENT_CLASS = "XWiki.ComponentRequirementClass";

    /**
     * The XClass defining a component method.
     */
    private static final String WIKI_COMPONENT_METHOD_CLASS = "XWiki.ComponentMethodClass";

    /**
     * The XClass defining a component interface implementation.
     */
    private static final String WIKI_COMPONENT_INTERFACE_CLASS = "XWiki.ComponentInterfaceClass";

    /**
     * The name property of the {@link WIKI_COMPONENT_INTERFACE_CLASS} XClass.
     */
    private static final String INTERFACE_NAME_FIELD = "name";

    /**
     * The name property of the {@link WIKI_COMPONENT_METHOD_CLASS} XClass. (Fix checkstyle).
     */
    private static final String METHOD_NAME_FIELD = INTERFACE_NAME_FIELD;

    /**
     * The role property of both {@link WIKI_COMPONENT_CLASS} and {@link WIKI_COMPONENT_REQUIREMENT_CLASS}.
     */
    private static final String COMPONENT_ROLE_HINT_FIELD = "roleHint";

    /**
     * The role hint property of both {@link WIKI_COMPONENT_CLASS} and {@link WIKI_COMPONENT_REQUIREMENT_CLASS}.
     */
    private static final String COMPONENT_ROLE_FIELD = "role";

    /**
     * Our execution. Needed to access the XWiki context.
     */
    @Requirement
    private Execution execution;

    /**
     * The wiki component manager that knows how to register component definition against the underlying CM.
     */
    @Requirement
    private WikiComponentManager wikiComponentManager;

    /**
     * Builder that creates component description from document references.
     */
    @Requirement
    private WikiComponentBuilder wikiComponentBuilder;

    /**
     * {@inheritDoc}
     */
    public List<Event> getEvents()
    {
        return Arrays.<Event> asList(new XWikiInitializedBridgeEvent());
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return "wikiComponentInitializer";
    }

    /**
     * {@inheritDoc}
     */
    public void onEvent(Event arg0, Object arg1, Object arg2)
    {
        // First step, verify that all XClasses exists and are up-to-date (act if not).
        this.installOrUpdateComponentXClasses();
        // Second step, lookup and register existing components.
        this.registerExistingWikiComponents();
    }

    /**
     * Registers existing components. Query them against the store, and if they are built properly (valid definition)
     * register them against the CM.
     */
    private void registerExistingWikiComponents()
    {
        String query =
            ", BaseObject as obj, StringProperty as role where obj.className='XWiki.ComponentClass'"
                + " and obj.name=doc.fullName and role.id.id=obj.id and role.id.name='role' and role.value <>''";
        try {
            for (DocumentReference ref : getXWikiContext().getWiki().getStore().searchDocumentReferences(query,
                getXWikiContext())) {
                try {
                    WikiComponent component = this.wikiComponentBuilder.build(ref);

                    this.wikiComponentManager.registerWikiComponent(component);
                } catch (InvalidComponentDefinitionException e) {
                    // Fail quietly and only log at the debug level.
                    getLogger().debug("Invalid wiki component definition for reference " + ref.toString(), e);
                } catch (WikiComponentException e) {
                    // Fail quietly and only log at the debug level.
                    getLogger().debug("Failed to register wiki component for reference " + ref.toString(), e);
                }
            }
        } catch (XWikiException e) {
            getLogger().error("Failed to register existing wiki components", e);
        }

    }

    /**
     * Verify that all XClasses exists and are up-to-date (act if not).
     */
    private void installOrUpdateComponentXClasses()
    {
        try {
            this.installOrUpdateComponentXClass();
            this.installOrUpdateComponentRequirementXClass();
            this.installOrUpdateComponentMethodXClass();
            this.installOrUpdateComponentInterfaceXClass();
        } catch (XWikiException e) {
            getLogger().error("Failed to install or update wiki component XClasses", e);
        }
    }

    /**
     * Verify that the {@link #WIKI_COMPONENT_INTERFACE_CLASS} exists and is up-to-date (act if not).
     * 
     * @throws XWikiException on failure
     */
    private void installOrUpdateComponentInterfaceXClass() throws XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWikiDocument doc = xcontext.getWiki().getDocument(WIKI_COMPONENT_INTERFACE_CLASS, xcontext);

        BaseClass bclass = doc.getXClass();
        bclass.setName(WIKI_COMPONENT_INTERFACE_CLASS);

        boolean needsUpdate = false;

        needsUpdate |= this.initializeXClassDocumentMetadata(doc, "Wiki Component Implements Interface XWiki Class");
        needsUpdate |= bclass.addTextField(INTERFACE_NAME_FIELD, "Interface Qualified Name", 30);

        if (needsUpdate) {
            this.update(doc);
        }
    }

    /**
     * Verify that the {@link #WIKI_COMPONENT_CLASS} exists and is up-to-date (act if not).
     * 
     * @throws XWikiException on failure
     */
    private void installOrUpdateComponentXClass() throws XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWikiDocument doc = xcontext.getWiki().getDocument(WIKI_COMPONENT_CLASS, xcontext);

        BaseClass bclass = doc.getXClass();
        bclass.setName(WIKI_COMPONENT_CLASS);

        boolean needsUpdate = false;

        needsUpdate |= this.initializeXClassDocumentMetadata(doc, "Wiki Component XWiki Class");
        needsUpdate |= bclass.addTextField(COMPONENT_ROLE_FIELD, "Component role", 30);
        needsUpdate |= bclass.addTextField(COMPONENT_ROLE_HINT_FIELD, "Component role hint", 30);

        if (needsUpdate) {
            this.update(doc);
        }
    }

    /**
     * Verify that the {@link #WIKI_COMPONENT_REQUIREMENT_CLASS} exists and is up-to-date (act if not).
     * 
     * @throws XWikiException on failure
     */
    private void installOrUpdateComponentRequirementXClass() throws XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWikiDocument doc = xcontext.getWiki().getDocument(WIKI_COMPONENT_REQUIREMENT_CLASS, xcontext);

        BaseClass bclass = doc.getXClass();
        bclass.setName(WIKI_COMPONENT_REQUIREMENT_CLASS);

        boolean needsUpdate = false;

        needsUpdate |= this.initializeXClassDocumentMetadata(doc, "Wiki Component Requirement XWiki Class");
        needsUpdate |= bclass.addTextField(COMPONENT_ROLE_FIELD, "Requirement role", 30);
        needsUpdate |= bclass.addTextField(COMPONENT_ROLE_HINT_FIELD, "Requirement role hint", 30);
        needsUpdate |= bclass.addTextField("bindingName", "Binding name", 30);
        needsUpdate |= bclass.addStaticListField("type", "Requirement type", "single=Single|list=List|map=Map");

        if (needsUpdate) {
            this.update(doc);
        }
    }

    /**
     * Verify that the {@link #WIKI_COMPONENT_METHOD_CLASS} exists and is up-to-date (act if not).
     * 
     * @throws XWikiException on failure
     */
    private void installOrUpdateComponentMethodXClass() throws XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWikiDocument doc = xcontext.getWiki().getDocument(WIKI_COMPONENT_METHOD_CLASS, xcontext);

        BaseClass bclass = doc.getXClass();
        bclass.setName(WIKI_COMPONENT_METHOD_CLASS);

        boolean needsUpdate = false;

        needsUpdate |= this.initializeXClassDocumentMetadata(doc, "Wiki Component Method XWiki Class");
        needsUpdate |= bclass.addTextField(METHOD_NAME_FIELD, "Method name", 30);
        needsUpdate |= bclass.addTextAreaField("code", "Method body code", 40, 20);

        if (needsUpdate) {
            this.update(doc);
        }
    }

    /**
     * Utility method for updating a wiki macro class definition document.
     * 
     * @param doc xwiki document containing the wiki macro class.
     * @throws XWikiException if an error occurs while saving the document.
     */
    private void update(XWikiDocument doc) throws XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        xcontext.getWiki().saveDocument(doc, xcontext);
    }

    /**
     * Helper method to prepare a document that will hold an XClass definition, setting its initial metadata, if needed
     * (author, title, parent, content, etc.).
     * 
     * @param doc the document to prepare
     * @param title the title to set
     * @return true if the doc has been modified and needs saving, false otherwise
     */
    private boolean initializeXClassDocumentMetadata(XWikiDocument doc, String title)
    {
        boolean needsUpdate = false;

        if (StringUtils.isBlank(doc.getCreator())) {
            needsUpdate = true;
            doc.setCreator(XWikiRightService.SUPERADMIN_USER);
        }
        if (StringUtils.isBlank(doc.getAuthor())) {
            needsUpdate = true;
            doc.setAuthor(doc.getCreator());
        }
        if (StringUtils.isBlank(doc.getParent())) {
            needsUpdate = true;
            doc.setParent("XWiki.XWikiClasses");
        }
        if (StringUtils.isBlank(doc.getTitle())) {
            needsUpdate = true;
            doc.setTitle(title);
        }
        if (StringUtils.isBlank(doc.getContent()) || !XWikiDocument.XWIKI20_SYNTAXID.equals(doc.getSyntaxId())) {
            needsUpdate = true;
            doc.setContent("{{include document=\"XWiki.ClassSheet\" /}}");
            doc.setSyntaxId(XWikiDocument.XWIKI20_SYNTAXID);
        }
        return needsUpdate;
    }

    /**
     * @return the XWikiContext extracted from the execution.
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

}
