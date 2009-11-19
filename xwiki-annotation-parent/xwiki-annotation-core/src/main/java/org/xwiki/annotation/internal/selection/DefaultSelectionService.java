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

package org.xwiki.annotation.internal.selection;

import org.xwiki.annotation.content.AlteredContent;
import org.xwiki.annotation.selection.AlteredSelection;
import org.xwiki.annotation.selection.SelectionMappingException;
import org.xwiki.annotation.selection.SelectionService;
import org.xwiki.annotation.selection.SourceSegment;
import org.xwiki.component.annotation.Component;

/**
 * This class is responsible for providing service related to selection.
 * 
 * @version $Id$
 */
@Component
public class DefaultSelectionService implements SelectionService
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.selection.SelectionService#mapToSource(org.xwiki.annotation.selection.AlteredSelection,
     *      org.xwiki.annotation.content.AlteredContent)
     */
    public SourceSegment mapToSource(AlteredSelection selection, AlteredContent source)
        throws SelectionMappingException
    {
        int firstOcurrence =
            source.getContent().toString().indexOf(selection.getAlteredSelection().getContent().toString());
        int secondOcurrence =
            source.getContent().toString().indexOf(selection.getAlteredSelection().getContent().toString(),
                firstOcurrence + 1);
        if (firstOcurrence != -1 && secondOcurrence == -1) {
            // We don't need context, selection appears only once in the source
            int intraOffset =
                source.getContent().toString().indexOf(selection.getAlteredSelection().getContent().toString());
            int initialOffset = source.getInitialOffset(intraOffset);
            int finalOffset =
                source.getInitialOffset(intraOffset + selection.getAlteredSelection().getContent().length() - 1);
            int length = finalOffset - initialOffset + 1;
            return new SourceSegment(initialOffset, length);
        } else {
            firstOcurrence =
                source.getContent().toString().indexOf(selection.getAlteredSelectionContext().getContent().toString());
            secondOcurrence =
                source.getContent().toString().indexOf(selection.getAlteredSelectionContext().getContent().toString(),
                    firstOcurrence + 1);
            if (firstOcurrence != -1 && secondOcurrence == -1) {
                // Context appears only once in the source
                int initialOffset = source.getInitialOffset(firstOcurrence + selection.getAlteredOffset());
                int finalOffset;
                int length;
                try {
                    finalOffset =
                        source.getInitialOffset(firstOcurrence + selection.getAlteredOffset()
                            + selection.getAlteredSelection().getContent().length() - 1);
                    length = finalOffset - initialOffset + 1;
                } catch (IllegalArgumentException e) {
                    finalOffset = source.getInitialLength();
                    length = finalOffset - initialOffset;
                }
                return new SourceSegment(initialOffset, length);
            } else {
                // neither selection or context appears in the source or the context is ambiguous
                throw new SelectionMappingException("The selection \"" + selection.getAlteredSelection().getContent()
                    + "\" could not be mapped on source \"" + source.getContent() + "\"");
            }
        }
    }
}
