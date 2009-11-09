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

import org.xwiki.annotation.internal.selection.AlteredHTMLSelection;
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
     * FIXME: shouldn't the selection alterer be injected in this class' implementations rather than passed from the one
     * injected in the annotation target subclasses?
     * 
     * @param alterer the content alterer to clean up the selection
     * @param selection selection on HTML rendered page
     * @param context context of selection
     * @param offset offset of selection in context
     * @return altered selection
     */
    AlteredHTMLSelection getAlteredHTMLSelection(ContentAlterer alterer, CharSequence selection, CharSequence context,
        int offset);
}
