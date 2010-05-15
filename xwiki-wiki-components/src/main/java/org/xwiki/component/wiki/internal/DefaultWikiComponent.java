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
package org.xwiki.component.wiki.internal;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.component.wiki.WikiComponent;
import org.xwiki.model.reference.DocumentReference;

/**
 * Default implementation of a wiki component definition.
 * 
 * @since 2.4-M2
 * @version $Id$
 */
public class DefaultWikiComponent implements WikiComponent
{
    /**
     * @see {@link #getDocumentReference()}
     */
    private DocumentReference documentReference;

    /**
     * @see {@link #getHandledMethods()}
     */
    private Map<String, String> handledMethods = new HashMap<String, String>();

    /**
     * @see {@link #getRole()}
     */
    private Class< ? > role;

    /**
     * @see {@link #getRoleHint()}
     */
    private String roleHint;

    /**
     * @see {@link #getImplementedInterfaces()}
     */
    private Class< ? >[] implementedInterfaces = new Class< ? >[]{};

    /**
     * Constructor of this component.
     * 
     * @param reference the document holding the component definition
     * @param role the role implemented
     * @param roleHint the role hint for this role implementation
     */
    public DefaultWikiComponent(DocumentReference reference, Class< ? > role, String roleHint)
    {
        this.documentReference = reference;
        this.role = role;
        this.roleHint = roleHint;
    }

    /**
     * {@inheritDoc}
     */
    public DocumentReference getDocumentReference()
    {
        return this.documentReference;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getHandledMethods()
    {
        return this.handledMethods;
    }

    /**
     * {@inheritDoc}
     */
    public Class< ? > getRole()
    {
        return this.role;
    }

    /**
     * {@inheritDoc}
     */
    public String getRoleHint()
    {
        return this.roleHint;
    }

    /**
     * {@inheritDoc}
     */
    public Class< ? >[] getImplementedInterfaces()
    {
        return this.implementedInterfaces;
    }

    /**
     * Sets the handled method.
     * 
     * @see {@link #getHandledMethods()}
     * 
     * @param methods the methods this component will handle
     */
    public void setHandledMethods(Map<String, String> methods)
    {
        this.handledMethods = methods;
    }

    /**
     * Sets the implemented interfaces.
     * 
     * @see {@link #getImplementedInterfaces()}
     * 
     * @param interfaces the interfaces this component will implement.
     */
    public void setImplementedInterfaces(Class< ? >[] interfaces)
    {
        this.implementedInterfaces = interfaces;
    }

}
