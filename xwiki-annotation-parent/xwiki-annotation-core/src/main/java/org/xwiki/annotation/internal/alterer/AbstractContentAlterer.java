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
        return new ComposedAlteredContent(alteredContent, ac);
    }

    /**
     * Composes two altered contents, by composing the offsets provided by the two alterers, applying first the {@code
     * initial} altered content offsets and then the {@code altered} altered content offsets.
     * 
     * @version $Id$
     */
    static class ComposedAlteredContent implements AlteredContent
    {
        /**
         * The initial altered content.
         */
        private AlteredContent initial;

        /**
         * The altered content.
         */
        private AlteredContent altered;

        /**
         * Builds a composed content alterer for the passed initial altered content and the passed altered content.
         * 
         * @param initial the initial altered content
         * @param altered the altering of the initial altered content
         */
        public ComposedAlteredContent(AlteredContent initial, AlteredContent altered)
        {
            this.initial = initial;
            this.altered = altered;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xwiki.annotation.internal.content.AlteredContent#getInitialOffset(int)
         */
        public int getInitialOffset(int i)
        {
            int tmp = altered.getInitialOffset(i);
            int rez = initial.getInitialOffset(tmp);
            return rez;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xwiki.annotation.internal.content.AlteredContent#getInitialLength()
         */
        public int getInitialLength()
        {
            return initial.getInitialLength();
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xwiki.annotation.internal.content.AlteredContent#getContent()
         */
        public CharSequence getContent()
        {
            return altered.getContent();
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xwiki.annotation.internal.content.AlteredContent#getAlteredOffset(int)
         */
        public int getAlteredOffset(int i)
        {
            int tmp = initial.getAlteredOffset(i);
            int rez = altered.getAlteredOffset(tmp);
            return rez;
        }
    }
}
