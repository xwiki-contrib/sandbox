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

package org.xwiki.annotation.internal.maintainment;

import org.xwiki.annotation.maintainment.AnnotationMaintainer;
import org.xwiki.annotation.maintainment.XDelta;

/**
 * @version $Id$
 */
public abstract class AbstractXDelta implements XDelta
{
    protected final int offset;

    protected final int length;

    /**
     * @param offset
     * @param length
     */
    public AbstractXDelta(int offset, int length)
    {
        this.offset = offset;
        this.length = length;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.XDelta#getLength()
     */
    public int getLength()
    {
        return length;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.XDelta#getOffset()
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.XDelta#update(org.xwiki.annotation.AnnotationMaintainer,
     *      int, int)
     */
    public abstract void update(AnnotationMaintainer maintainer, int offset, int length);

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.XDelta#getSignedDelta()
     */
    public abstract int getSignedDelta();
}
