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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.wiki.InvalidComponentDefinitionException;
import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.component.wiki.WikiComponentBuilder;
import org.xwiki.component.wiki.WikiComponentException;
import org.xwiki.component.wiki.internal.DefaultWikiComponent;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default implementation of a wiki component builder, that is using the legacy XWiki core module.
 * 
 * @since 2.4-M2
 * @version $Id$
 */
@Component
public class DefaultWikiComponentBuilder extends AbstractLogEnabled implements WikiComponentBuilder
{

    /**
     * The name of the document that holds the XClass definition of a wiki component.
     */
    private static final String XWIKI_COMPONENT_CLASS = "XWiki.ComponentClass";

    /**
     * The name of the document that holds the XClass definition of an implementation of an interface by a component.
     */
    private static final String XWIKI_COMPONENT_INTERFACE_CLASS = "XWiki.ComponentInterfaceClass";

    /**
     * The name of the document that holds the XClass definition of a method of a component.
     */
    private static final String XWIKI_COMPONENT_METHOD_CLASS = "XWiki.ComponentMethodClass";

    /**
     * The property name of the name of a component method.
     */
    private static final String COMPONENT_METHOD_NAME_FIELD = "name";

    /**
     * The property name of the name of an implemented interface. (Checkstyle fix).
     */
    private static final String COMPONENT_INTERFACE_NAME_FIELD = COMPONENT_METHOD_NAME_FIELD;

    /**
     * Parser. Used to load code as XDOM from XObject string.
     */
    @Requirement("xwiki/2.0")
    private Parser parser;
    
    /**
     * Execution, needed to access the XWiki context map.
     */
    @Requirement
    private Execution execution;

    /**
     * {@inheritDoc}
     */
    public WikiComponent build(DocumentReference reference) throws InvalidComponentDefinitionException,
        WikiComponentException
    {
        try {
            XWikiDocument componentDocument = getXWikiContext().getWiki().getDocument(reference, getXWikiContext());
            BaseObject componentObject = componentDocument.getObject(XWIKI_COMPONENT_CLASS);

            if (componentObject == null) {
                throw new InvalidComponentDefinitionException("No component object could be found");
            }

            String role = componentObject.getStringValue("role");

            if (StringUtils.isBlank(role)) {
                throw new InvalidComponentDefinitionException("No role were precised in the component");
            }

            Class< ? > roleAsClass;
            try {
                roleAsClass = Class.forName(role);
            } catch (ClassNotFoundException e) {
                throw new InvalidComponentDefinitionException("The role class could not be found", e);
            }

            String roleHint = StringUtils.defaultIfEmpty(componentObject.getStringValue("roleHint"), "default");

            DefaultWikiComponent component = new DefaultWikiComponent(reference, roleAsClass, roleHint);
            component.setHandledMethods(this.getHandledMethods(componentDocument));
            component.setImplementedInterfaces(this.getDeclaredInterfaces(componentDocument));

            return component;

        } catch (XWikiException e) {
            throw new WikiComponentException("Failed to build wiki component for document " + reference.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsWikiComponent(DocumentReference reference) 
    {
        XWikiDocument componentDocument;
        try {
            componentDocument = getXWikiContext().getWiki().getDocument(reference, getXWikiContext());
            return componentDocument.getObject(XWIKI_COMPONENT_CLASS) != null;        
        } catch (XWikiException e) {
            getLogger().error("Failed to verify if document holds a wiki component", e);
            // assume false
            return false;
        }
    }
    
    /**
     * @param componentDocument the document holding the component description
     * @return the map of component handled methods/method body
     */
    private Map<String, XDOM> getHandledMethods(XWikiDocument componentDocument)
    {
        Map<String, XDOM> handledMethods = new HashMap<String, XDOM>();
        if (componentDocument.getObjectNumbers(XWIKI_COMPONENT_METHOD_CLASS) > 0) {
            for (BaseObject method : componentDocument.getObjects(XWIKI_COMPONENT_METHOD_CLASS)) {
                if (!StringUtils.isBlank(method.getStringValue(COMPONENT_METHOD_NAME_FIELD))) {
                    try {
                        XDOM xdom = parser.parse(new StringReader(method.getStringValue("code")));
                        handledMethods.put(method.getStringValue(COMPONENT_METHOD_NAME_FIELD), xdom);
                    } catch (ParseException e) {
                        // this method will just not be handled
                    }
                }
            }
        }
        return handledMethods;
    }

    /**
     * @param componentDocument the document holding the component description
     * @return the array of interfaces declared (and actually existing) by the document
     */
    private Class< ? >[] getDeclaredInterfaces(XWikiDocument componentDocument)
    {
        List<Class< ? >> interfaces = new ArrayList<Class< ? >>();
        if (componentDocument.getObjectNumbers(XWIKI_COMPONENT_INTERFACE_CLASS) > 0) {
            for (BaseObject iface : componentDocument.getObjects(XWIKI_COMPONENT_INTERFACE_CLASS)) {
                if (!StringUtils.isBlank(iface.getStringValue(COMPONENT_INTERFACE_NAME_FIELD))) {
                    try {
                        Class< ? > implemented = Class.forName(iface.getStringValue(COMPONENT_INTERFACE_NAME_FIELD));
                        interfaces.add(implemented);
                    } catch (ClassNotFoundException e) {
                        // Silent
                    }
                }
            }
        }
        return interfaces.toArray(new Class< ? >[] {});
    }

    /**
     * @return a XWikiContext, retrieved from our execution
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }

}
