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

import org.xwiki.annotation.ContentAlterer;
import org.xwiki.annotation.SelectionService;
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
     * @see org.xwiki.annotation.SelectionService#getAlteredHTMLSelection(java.lang.CharSequence,
     *      java.lang.CharSequence, int)
     */
    public AlteredHTMLSelection getAlteredHTMLSelection(ContentAlterer alterer, CharSequence selection,
        CharSequence context, int offset)
    {
        return new AlteredHTMLSelectionImpl(alterer.alter(selection), alterer.alter(context), offset);
    }
}
