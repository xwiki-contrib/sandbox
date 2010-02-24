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
package org.xwiki.gadgets;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;

/**
 * @version $Id$
 */
@ComponentRole
public interface MacroService
{
    /**
     * Gets the sorted by name definition list of all macros.
     * 
     * @return sorted list of all macros
     */
    List<MacroDescriptor> getMacroDescriptors();

    /**
     * Looks up a macro.
     * 
     * @param macroId the id of the macro to lookup
     * @return the macro descriptor if macro was found, else null
     */
    MacroDescriptor getMacroDescriptor(String macroId);
}
