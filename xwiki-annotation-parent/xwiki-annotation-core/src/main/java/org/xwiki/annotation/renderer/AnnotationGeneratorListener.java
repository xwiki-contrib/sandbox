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
package org.xwiki.annotation.renderer;

import java.util.Collection;

import org.xwiki.annotation.Annotation;
import org.xwiki.rendering.listener.Listener;

/**
 * An annotations generator listener is a listener that produces a list of annotation bookmarks for a list of
 * annotations with respect to the events it listens to.
 * 
 * @version $Id$
 */
public interface AnnotationGeneratorListener extends Listener
{
    /**
     * Returns the list of annotation bookmarks for the passed annotations. The bookmarks returned by this function have
     * to be valid after the listening has ended (after endDocument was called), there is no restriction about their
     * validity before this moment.
     * 
     * @return the annotation bookmarks computed for the passed list of annotations
     */
    AnnotationBookmarks getAnnotationBookmarks();

    /**
     * @param annotations the annotations to produce bookmarks for
     */
    void setAnnotations(Collection<Annotation> annotations);
}
