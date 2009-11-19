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

package org.xwiki.annotation;

import java.util.Collection;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Component responsible for providing annotations related services: the management of annotations (adding, removing,
 * updating) and with rendering them on the xwiki documents.
 * 
 * @version $Id$
 */
@ComponentRole
public interface AnnotationService
{
    /**
     * Request insertion of new annotation in a specified document.
     * 
     * @param metadata annotation content
     * @param selection HTML selection concerned by annotations
     * @param selectionContext HTML selection context
     * @param offset offset of the selection in context
     * @param documentName the name of the document containing annotation
     * @param user the author of the annotation
     * @throws AnnotationServiceException if selection resolution fail or if an XWikiException occurred
     */
    void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user) throws AnnotationServiceException;

    /**
     * Returns the HTML of the requested document, along with annotations inserted as {@code span} elements inside it.
     * 
     * @param documentName the name of the document to render with annotations
     * @return rendered and annotated document
     * @throws AnnotationServiceException if anything goes wrong retrieving or rendering the requested document
     */
    CharSequence getAnnotatedHTML(CharSequence documentName) throws AnnotationServiceException;

    /**
     * Removes the annotation with the specified ID.
     * 
     * @param documentName containing document
     * @param annotationID id of the annotation
     * @throws AnnotationServiceException if anything goes wrong accessing the annotations store
     */
    void removeAnnotation(CharSequence documentName, CharSequence annotationID) throws AnnotationServiceException;

    /**
     * Returns all the annotations on the passed document.
     * 
     * @param documentName name of the document to return annotations for
     * @return all annotations present in the specified document
     * @throws AnnotationServiceException if anything goes wrong accessing the annotations store
     */
    Collection<Annotation> getAnnotations(CharSequence documentName) throws AnnotationServiceException;

    /**
     * Returns all annotations marked as safe, i.e. which are still valid on the document, regardless of the edits the
     * document suffered from creation.
     * 
     * @param documentName name of the document to return annotations for
     * @return all safe annotations in the document
     * @throws AnnotationServiceException if anything goes wrong accessing the annotations store
     * @see {@link org.xwiki.annotation.maintainment.AnnotationState#SAFE}
     */
    Collection<Annotation> getSafeAnnotations(CharSequence documentName) throws AnnotationServiceException;
}
