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
import org.xwiki.annotation.internal.context.AlteredSource;
import org.xwiki.annotation.internal.exception.SelectionMappingException;

/**
 * Default implementation for {@link AlteredHTMLSelection}.
 * 
 * @version $Id$
 */
public class AlteredHTMLSelectionImpl implements AlteredHTMLSelection
{
    private final AlteredContent alteredSelection;

    private final int alteredOffset;

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
     * @see org.xwiki.annotation.internal.selection.AlteredHTMLSelection
     *      #mapToSource(org.xwiki.annotation.internal.context.AlteredSource)
     * @throws SelectionMappingException can be thrown if mapping fails
     */
    public SourceSegment mapToSource(AlteredSource source) throws SelectionMappingException
    {
        int j = source.getContent().toString().indexOf(alteredSelection.getContent().toString());
        int k = source.getContent().toString().indexOf(alteredSelection.getContent().toString(), j + 1);
        // We don't need context
        if (j != -1 && k == -1) {
            int intraOffset = source.getContent().toString().indexOf(alteredSelection.getContent().toString());
            int initialOffset = source.getInitialOffset(intraOffset);
            int finalOffset = source.getInitialOffset(intraOffset + alteredSelection.getContent().length() - 1);
            int length = finalOffset - initialOffset + 1;
            return new SourceSegment(initialOffset, length);
        } else {
            int extraOffset = source.getContent().toString().indexOf(alteredSelectionContext.getContent().toString());
            k =
                source.getContent().toString()
                    .indexOf(alteredSelectionContext.getContent().toString(), extraOffset + 1);
            if (extraOffset != -1 && k == -1) {
                int initialOffset = source.getInitialOffset(extraOffset + alteredOffset);
                int finalOffset;
                int length;
                try {
                    finalOffset =
                        source.getInitialOffset(extraOffset + alteredOffset + alteredSelection.getContent().length()
                            - 1);
                    length = finalOffset - initialOffset + 1;
                } catch (IllegalArgumentException e) {
                    finalOffset = source.getInitialLength();
                    length = finalOffset - initialOffset;
                }
                return new SourceSegment(initialOffset, length);
            } else {
                throw new SelectionMappingException();
            }
        }
    }
}
