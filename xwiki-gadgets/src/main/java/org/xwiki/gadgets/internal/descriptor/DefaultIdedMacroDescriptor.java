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
package org.xwiki.gadgets.internal.descriptor;

import java.util.Map;

import org.xwiki.gadgets.descriptor.IdedMacroDescriptor;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.descriptor.ContentDescriptor;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.macro.descriptor.ParameterDescriptor;

/**
 * Default identified macro descriptor
 * 
 * @version $Id$
 */
public class DefaultIdedMacroDescriptor implements IdedMacroDescriptor
{
    /**
     * The macro identifier
     */
    private MacroId id;

    /**
     * Macro descriptor without an id
     */
    private MacroDescriptor descriptor;

    /**
     * @param descriptor
     * @param id
     */
    public DefaultIdedMacroDescriptor(MacroDescriptor descriptor, MacroId id)
    {
        this.id = id;
        this.descriptor = descriptor;
    }

    /**
     * {@inheritDoc}
     * 
     * @see IdedMacroDescriptor#getId()
     */
    public MacroId getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     * 
     * @see IdedMacroDescriptor#getContentDescriptor()
     */
    public ContentDescriptor getContentDescriptor()
    {
        return descriptor.getContentDescriptor();
    }

    /**
     * {@inheritDoc}
     * 
     * @see IdedMacroDescriptor#getDefaultCategory()
     */
    public String getDefaultCategory()
    {
        return descriptor.getDefaultCategory();
    }

    /**
     * {@inheritDoc}
     * 
     * @see IdedMacroDescriptor#getDescription()
     */
    public String getDescription()
    {
        return descriptor.getDescription();
    }

    public String getName()
    {
        return descriptor.getName();
    }

    /**
     * {@inheritDoc}
     * 
     * @see IdedMacroDescriptor#getParameterDescriptorMap()
     */
    public Map<String, ParameterDescriptor> getParameterDescriptorMap()
    {
        return descriptor.getParameterDescriptorMap();
    }

    /**
     * {@inheritDoc}
     * 
     * @see IdedMacroDescriptor#getParametersBeanClass()
     */
    public Class< ? > getParametersBeanClass()
    {
        return descriptor.getParametersBeanClass();
    }
}
