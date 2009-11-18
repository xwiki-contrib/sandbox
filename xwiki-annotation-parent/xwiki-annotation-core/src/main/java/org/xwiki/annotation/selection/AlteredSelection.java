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

package org.xwiki.annotation.selection;

import org.xwiki.annotation.content.AlteredContent;

/**
 * This class models an altered HTML selection.
 * 
 * @version $Id$
 */
public class AlteredSelection
{
    /**
     * The altered selection.
     */
    private final AlteredContent alteredSelection;

    /**
     * The altered offset of the selection in its context.
     */
    private final int alteredOffset;

    /**
     * The altered context of this selection.
     */
    private final AlteredContent alteredSelectionContext;

    /**
     * @param alteredSelection the altered content of the selection
     * @param alteredSelectionContext altered selection context
     * @param originalOffset the original position of the selection in the context
     */
    public AlteredSelection(AlteredContent alteredSelection, AlteredContent alteredSelectionContext, int originalOffset)
    {
        this.alteredSelection = alteredSelection;
        this.alteredSelectionContext = alteredSelectionContext;
        this.alteredOffset = alteredSelectionContext.getAlteredOffset(originalOffset);
    }

    /**
     * @return the alteredSelection
     */
    public AlteredContent getAlteredSelection()
    {
        return alteredSelection;
    }

    /**
     * @return the alteredOffset
     */
    public int getAlteredOffset()
    {
        return alteredOffset;
    }

    /**
     * @return the alteredSelectionContext
     */
    public AlteredContent getAlteredSelectionContext()
    {
        return alteredSelectionContext;
    }
}
