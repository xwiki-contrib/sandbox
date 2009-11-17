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

import org.xwiki.annotation.internal.content.AlteredContent;
import org.xwiki.annotation.internal.exception.SelectionMappingException;

/**
 * Default implementation for {@link AlteredHTMLSelection}.
 * 
 * @version $Id$
 */
public class AlteredHTMLSelectionImpl implements AlteredHTMLSelection
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
    private AlteredContent alteredSelectionContext;

    /**
     * @param alteredSelection is the altered content of the selection
     * @param alteredSelectionContext is altered selection context
     * @param offset is the position of the selection in the context
     */
    AlteredHTMLSelectionImpl(AlteredContent alteredSelection, AlteredContent alteredSelectionContext, int offset)
    {
        this.alteredSelection = alteredSelection;
        this.alteredSelectionContext = alteredSelectionContext;
        this.alteredOffset = alteredSelectionContext.getAlteredOffset(offset);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.selection.AlteredHTMLSelection #mapToSource(AlteredContent)
     * @throws SelectionMappingException can be thrown if mapping fails
     */
    public SourceSegment mapToSource(AlteredContent source) throws SelectionMappingException
    {
        int firstOcurrence = source.getContent().toString().indexOf(alteredSelection.getContent().toString());
        int secondOcurrence =
            source.getContent().toString().indexOf(alteredSelection.getContent().toString(), firstOcurrence + 1);
        if (firstOcurrence != -1 && secondOcurrence == -1) {
            // We don't need context, selection appears only once in the source
            int intraOffset = source.getContent().toString().indexOf(alteredSelection.getContent().toString());
            int initialOffset = source.getInitialOffset(intraOffset);
            int finalOffset = source.getInitialOffset(intraOffset + alteredSelection.getContent().length() - 1);
            int length = finalOffset - initialOffset + 1;
            return new SourceSegment(initialOffset, length);
        } else {
            firstOcurrence = source.getContent().toString().indexOf(alteredSelectionContext.getContent().toString());
            secondOcurrence =
                source.getContent().toString().indexOf(alteredSelectionContext.getContent().toString(),
                    firstOcurrence + 1);
            if (firstOcurrence != -1 && secondOcurrence == -1) {
                // Context appears only once in the source
                int initialOffset = source.getInitialOffset(firstOcurrence + alteredOffset);
                int finalOffset;
                int length;
                try {
                    finalOffset =
                        source.getInitialOffset(firstOcurrence + alteredOffset + alteredSelection.getContent().length()
                            - 1);
                    length = finalOffset - initialOffset + 1;
                } catch (IllegalArgumentException e) {
                    finalOffset = source.getInitialLength();
                    length = finalOffset - initialOffset;
                }
                return new SourceSegment(initialOffset, length);
            } else {
                // neither selection or context appears in the source or the context is ambiguous
                throw new SelectionMappingException();
            }
        }
    }
}
