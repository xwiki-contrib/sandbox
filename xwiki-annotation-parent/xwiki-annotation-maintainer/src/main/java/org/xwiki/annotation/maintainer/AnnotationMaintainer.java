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

package org.xwiki.annotation.maintainer;

import org.xwiki.annotation.Annotation;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.observation.EventListener;

/**
 * Component responsible for updating annotation location when an annotated document is modified.
 * 
 * @version $Id$
 */
@ComponentRole
public interface AnnotationMaintainer extends EventListener
{
    /**
     * Method triggered by XDelta instance when an annotation is moved.
     * 
     * @param annotation the annotation to update offset for
     * @param offset new annotation location
     */
    void updateOffset(Annotation annotation, int offset);

    /**
     * Method triggered by XDelta instance when an annotation is altered.
     * 
     * @param annotation the annotation which was altered
     * @param delta XDelta instance which modified annotation
     */
    void onAnnotationModification(Annotation annotation, XDelta delta);

    /**
     * Triggered when we need to determine if we have a "regular" deletion. See tests for explanations about "regular"
     * deletion
     * 
     * @param annotation the annotation for which to detect the type of deletion
     * @param delta the source difference to test
     * @param previousContent the previous content of the changed entity
     * @param currentContent the current content of the changed entity
     */
    void onSpecialCaseDeletion(Annotation annotation, XDelta delta, String previousContent, String currentContent);

    /**
     * Triggered when we need to determine if we have a "regular" addition. See tests for explanations about "regular"
     * addition
     * 
     * @param annotation the annotation for which to detect the type of deletion
     * @param delta the source difference to test
     * @param previousContent the previous content of the changed entity
     * @param currentContent the current content of the changed entity
     */
    void onSpecialCaseAddition(Annotation annotation, XDelta delta, String previousContent, String currentContent);
}
