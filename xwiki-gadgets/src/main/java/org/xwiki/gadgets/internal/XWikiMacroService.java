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
package org.xwiki.gadgets.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwiki.gadgets.MacroService;
import org.xwiki.gadgets.descriptor.IdedMacroDescriptor;
import org.xwiki.gadgets.internal.descriptor.DefaultIdedMacroDescriptor;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroCategoryManager;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

/**
 * @version $Id$
 */
@Component
public class XWikiMacroService extends AbstractLogEnabled implements MacroService
{
    /**
     * Default XWiki logger to report errors correctly
     */
    private static final Log LOG = LogFactory.getLog(XWikiMacroService.class);

    /**
     * The syntax factory used to create {@link Syntax} instances from string syntax identifiers.
     */
    @Requirement
    private SyntaxFactory syntaxFactory;

    /**
     * The macro manager used to retrieve macros
     */
    @Requirement
    private MacroManager macroManager;

    /**
     * {@inheritDoc}
     * 
     * @see MacroService#getMacroDescriptors()
     */
    public List<IdedMacroDescriptor> getMacroDescriptors()
    {
        try {
            List<IdedMacroDescriptor> descriptors = new ArrayList<IdedMacroDescriptor>();
            for (MacroId macroId : macroManager.getMacroIds()) {
                Macro< ? > macro = macroManager.getMacro(macroId);
                IdedMacroDescriptor macroDefinition = new DefaultIdedMacroDescriptor(macro.getDescriptor(), macroId);
                descriptors.add(macroDefinition);
            }

            Collections.sort(descriptors, new Comparator<IdedMacroDescriptor>()
            {
                public int compare(IdedMacroDescriptor alice, IdedMacroDescriptor bob)
                {
                    return alice.getName().toLowerCase().compareTo(bob.getName().toLowerCase());
                }
            });

            return descriptors;
        } catch (Exception e) {
            LOG.error("Exception while retrieving the list of macro descriptors.", e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see MacroService#getMacroDescriptor(String)
     */
    public IdedMacroDescriptor getMacroDescriptor(String macroId)
    {
        try {
            MacroId macroIdObject = new MacroId(macroId);
            Macro< ? > macro = macroManager.getMacro(macroIdObject);

            return new DefaultIdedMacroDescriptor(macro.getDescriptor(), macroIdObject);
        } catch (MacroLookupException e) {
            // if macro not found, return null
            return null;
        } catch (Exception e) {
            LOG.error(String.format("Exception while retrieving macro descriptor for macro id %s.", macroId), e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
