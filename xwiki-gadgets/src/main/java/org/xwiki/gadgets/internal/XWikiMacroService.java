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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xwiki.gadgets.MacroService;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroLookupException;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.macro.descriptor.MacroDescriptor;
import org.xwiki.rendering.macro.wikibridge.WikiMacro;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;

/**
 * @version $Id$
 */
@Component
public class XWikiMacroService extends AbstractLogEnabled implements MacroService
{
    /**
     * Default XWiki logger to report errors correctly.
     */
    private static final Log LOG = LogFactory.getLog(XWikiMacroService.class);

    /**
     * The syntax factory used to create {@link Syntax} instances from string syntax identifiers.
     */
    @Requirement
    private SyntaxFactory syntaxFactory;

    /**
     * The macro manager used to retrieve macros.
     */
    @Requirement
    private MacroManager macroManager;

    /**
     * The {@link EntityReferenceSerializer} to serialize a {@link DocumentReference}.
     */
    @Requirement
    private EntityReferenceSerializer< ? > entityReferenceSerializer;

    /**
     * {@inheritDoc}
     * 
     * @see MacroService#getMacroDescriptors()
     */
    public List<MacroDescriptor> getMacroDescriptors()
    {
        try {
            List<MacroDescriptor> descriptors = new ArrayList<MacroDescriptor>();
            for (MacroId macroId : macroManager.getMacroIds()) {
                descriptors.add(macroManager.getMacro(macroId).getDescriptor());
            }

            Collections.sort(descriptors, new Comparator<MacroDescriptor>()
            {
                public int compare(MacroDescriptor alice, MacroDescriptor bob)
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
    public MacroDescriptor getMacroDescriptor(String macroId)
    {
        try {
            MacroId macroIdObject = new MacroId(macroId);

            return macroManager.getMacro(macroIdObject).getDescriptor();
        } catch (MacroLookupException e) {
            // if macro not found, return null
            return null;
        } catch (Exception e) {
            LOG.error(String.format("Exception while retrieving macro descriptor for macro id %s.", macroId), e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see MacroService#isWikiMacro(String)
     */
    public boolean isWikiMacro(String macroId)
    {
        try {
            Macro< ? > macro = macroManager.getMacro(new MacroId(macroId));
            if (macro instanceof WikiMacro) {
                return true;
            }

            return false;
        } catch (MacroLookupException e) {
            LOG.error(String.format("Exception checking Wiki Macro type while retrieving macro with id %s.", macroId),
                e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see MacroService#getWikiMacroDocumentReferenceFullName(String)
     */
    public String getWikiMacroDocumentReferenceFullName(String macroId)
    {
        try {
            Macro< ? > macro = macroManager.getMacro(new MacroId(macroId));
            if (macro instanceof WikiMacro) {

                DocumentReference doc = ((WikiMacro) macro).getDocumentReference();
                return (String) entityReferenceSerializer.serialize(doc);
            }

            return null;
        } catch (MacroLookupException e) {
            LOG.error(String.format(
                "Exception fetching Wiki Macro document entity reference while retrieving macro with id %s.", macroId),
                e);
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
}
