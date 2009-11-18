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

package org.xwiki.annotation.selection;

/**
 * This class stores information that defines a segment of document source.
 * 
 * @version $Id$
 */
public class SourceSegment
{
    /**
     * This is the begin offset of the segment. Note that first character has offset zero.
     */
    public final int offset;

    /**
     * This is the length of the segment.
     */
    public final int length;

    /**
     * Instantiate a SourceSegment Object.
     * 
     * @param offset offset of the segment
     * @param length length of the segment
     */
    public SourceSegment(int offset, int length)
    {
        this.length = length;
        this.offset = offset;
    }
}
