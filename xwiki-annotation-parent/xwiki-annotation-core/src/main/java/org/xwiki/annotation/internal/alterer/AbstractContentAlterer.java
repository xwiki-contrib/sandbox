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

/**
 * This abstract class is used in order to factor piped alterations logic.
 * 
 * @version $Id$
 */
public abstract class AbstractContentAlterer implements ContentAlterer
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.ContentAlterer#alter(org.xwiki.annotation.internal.content.AlteredContent)
     */
    public AlteredContent alter(final AlteredContent alteredContent)
    {
        final AlteredContent ac = alter(alteredContent.getContent().toString());
        return new AlteredContent()
        {

            public int getInitialOffset(int i)
            {
                int tmp = ac.getInitialOffset(i);
                int rez = alteredContent.getInitialOffset(tmp);
                return rez;
            }

            public int getInitialLength()
            {
                return alteredContent.getInitialLength();
            }

            public CharSequence getContent()
            {
                return ac.getContent();
            }

            public int getAlteredOffset(int i)
            {
                int tmp = alteredContent.getAlteredOffset(i);
                int rez = ac.getAlteredOffset(tmp);
                return rez;
            }
        };
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.ContentAlterer#alter(java.lang.CharSequence)
     */
    public abstract AlteredContent alter(CharSequence sequence);
}
