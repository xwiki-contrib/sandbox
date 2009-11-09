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
import org.xwiki.annotation.SourceAlterer;
import org.xwiki.annotation.internal.context.AlteredSource;
import org.xwiki.annotation.internal.context.AlteredSourceImpl;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Plain text alterer from source. It uses synthaxFilter in order to remove forbidden characters.
 * 
 * @version $Id$
 */
@Component("PLAINTEXT")
public class PlainTextSourceAlterer implements SourceAlterer
{
    @Requirement("PLAINTEXT")
    private static ContentAlterer contentAlterer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.SourceAlterer#alter(org.xwiki.annotation.internal.context.Source)
     */
    public AlteredSource alter(Source context)
    {
        return new AlteredSourceImpl(contentAlterer.alter(context.getSource()));
    }
}
