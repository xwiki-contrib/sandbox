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

package org.xwiki.annotation.maintainer.internal;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.maintainer.AnnotationMaintainer;

/**
 * @version $Id$
 */
public class XAddition extends AbstractXDelta
{
    /**
     * @param offset the offset of the current addition
     * @param length the length of the current addition
     */
    public XAddition(int offset, int length)
    {
        super(offset, length);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainer.XDelta#getSignedDelta()
     */
    public int getSignedDelta()
    {
        return getLength();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainer.XDelta#update(Annotation,
     *      org.xwiki.annotation.maintainer.AnnotationMaintainer, String, String)
     */
    public void update(Annotation annotation, AnnotationMaintainer maintainer, String previousContent,
        String currentContent)
    {
        int offset = annotation.getOffset();
        int length = annotation.getLength();

        if (this.getOffset() <= offset) {
            // addition before
            maintainer.updateOffset(annotation, offset + getSignedDelta());
        } else if (this.getOffset() >= offset + length) {
            // addition after
        } else {
            // NOP
            maintainer.onSpecialCaseAddition(annotation, this, previousContent, currentContent);
        }
    }
}
