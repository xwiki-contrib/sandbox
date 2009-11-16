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

package org.xwiki.annotation.internal.alterer;

import org.xwiki.annotation.ContentAlterer;
import org.xwiki.annotation.internal.content.AlteredContent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Composite alterer chaining the HTML alterer and the plain text alterer.
 * 
 * @version $Id$
 */
@Component("COMPOSITE")
public class CompositeContentAlterer extends AbstractContentAlterer
{
    @Requirement("HTML")
    ContentAlterer htmlAlterer;

    @Requirement("xwiki/2.0")
    ContentAlterer plainAlterer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.ContentAlterer#alter(java.lang.CharSequence)
     */
    public AlteredContent alter(CharSequence sequence)
    {
        return plainAlterer.alter(htmlAlterer.alter(sequence));
    }
}
