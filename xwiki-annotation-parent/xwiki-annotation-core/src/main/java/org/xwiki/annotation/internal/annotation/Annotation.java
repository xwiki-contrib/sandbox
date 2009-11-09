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

package org.xwiki.annotation.internal.annotation;

import org.xwiki.annotation.internal.maintainment.AnnotationState;

/**
 * This class models an annotation.
 * 
 * @version $Id$
 */
public interface Annotation
{
    /**
     * @return page of annotation.
     */
    CharSequence getPage();

    /**
     * @return author of annotation.
     */
    CharSequence getAuthor();

    /**
     * @return date of annotation.
     */
    CharSequence getDate();

    /**
     * @return annotation content.
     */
    CharSequence getAnnotation();

    /**
     * @return initial selection of selection
     */
    CharSequence getInitialSelection();

    /**
     * @return selection context of annotation
     */
    CharSequence getSelectionContext();

    /**
     * @return id of annotation
     */
    int getId();

    /**
     * @return state of annotation
     */
    AnnotationState getState();

    /**
     * @param state to set
     */
    void setState(AnnotationState state);

    /**
     * @return offset of annotation's selection
     */
    int getOffset();

    /**
     * modify offset of annotation's selection.
     * 
     * @param offset to set
     */
    void setOffset(int offset);

    /**
     * @return length of annotation's selection.
     */
    int getLength();
}
