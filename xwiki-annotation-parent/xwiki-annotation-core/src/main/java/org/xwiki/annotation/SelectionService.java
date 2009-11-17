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

package org.xwiki.annotation;

import org.xwiki.annotation.internal.content.AlteredContent;
import org.xwiki.annotation.internal.exception.SelectionMappingException;
import org.xwiki.annotation.internal.selection.AlteredSelection;
import org.xwiki.annotation.internal.selection.SourceSegment;
import org.xwiki.component.annotation.ComponentRole;

/**
 * This class provide services related to content selection.
 * 
 * @version $Id$
 */
@ComponentRole
public interface SelectionService
{
    /**
     * This component alters and wrap selection informations in a single object. <br />
     * 
     * @param alterer the content alterer to clean up the selection
     * @param selection selection on HTML rendered page
     * @param context context of selection
     * @param offset offset of selection in context
     * @return altered selection
     */
    AlteredSelection getAlteredHTMLSelection(ContentAlterer alterer, CharSequence selection, CharSequence context,
        int offset);

    /**
     * Computes the source segment where the passed selection maps on the passed source.
     * 
     * @param selection the selection to map on the source
     * @param source the content on which the selection is made
     * @return the segment of source that generates selection
     * @throws SelectionMappingException if the selection cannot be mapped on the passed source
     */
    SourceSegment mapToSource(AlteredSelection selection, AlteredContent source) throws SelectionMappingException;
}
