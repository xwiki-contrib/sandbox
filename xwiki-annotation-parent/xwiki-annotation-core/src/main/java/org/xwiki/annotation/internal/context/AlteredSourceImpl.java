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

package org.xwiki.annotation.internal.context;

import org.xwiki.annotation.internal.content.AlteredContent;

/**
 * Default {@link AlteredSource} implementation.
 * 
 * @version $Id$
 */
public class AlteredSourceImpl implements AlteredSource
{
    private final AlteredContent content;

    /**
     * @param content
     */
    public AlteredSourceImpl(AlteredContent content)
    {
        this.content = content;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.content.AlteredContent#getContent()
     */
    public CharSequence getContent()
    {
        return content.getContent();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.content.AlteredContent#getInitialOffset(int)
     */
    public int getInitialOffset(int i)
    {
        return content.getInitialOffset(i);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.content.AlteredContent#getAlteredOffset(int)
     */
    public int getAlteredOffset(int i)
    {
        return content.getAlteredOffset(i);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.content.AlteredContent#getInitialLength()
     */
    public int getInitialLength()
    {
        return content.getInitialLength();
    }
}
