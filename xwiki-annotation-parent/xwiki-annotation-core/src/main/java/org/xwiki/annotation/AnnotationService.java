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

import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;

/**
 * This component is responsive for providing services related to annotation management and annotated document
 * rendering.
 * 
 * @version $Id$
 */
@ComponentRole
public interface AnnotationService
{

    /**
     * This enumeration defines what kind of media are supported by the system of annotation.
     * 
     * @version $Id$
     */
    public enum Target
    {
        feedEntry,
        documentContent;
    }

    /**
     * Request insertion of new annotation in a given page.
     * 
     * @param metadata annotation content
     * @param selection HTML selection concerned by annotations
     * @param selectionContext HTML selection context
     * @param offset offset of the selection in context
     * @param documentName the name of the document containing annotation
     * @param user the author of the annotation
     * @throws AnnotationServiceException can be thrown if selection resolution fail or if an XWikiException occurred
     */
    void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user, XWikiContext context, Target target)
        throws AnnotationServiceException;

    /**
     * @param documentName
     * @param context
     * @param target kind of document
     * @return rendered and annotated document
     * @throws AnnotationServiceException
     */
    CharSequence getAnnotatedHTML(CharSequence documentName, XWikiContext context, Target target)
        throws AnnotationServiceException;

    /**
     * remove annotation which has a given id.
     * 
     * @param documentName containing document
     * @param annotationID id of the annotation
     * @param context
     * @param target
     * @throws AnnotationServiceException can be thrown if selection resolution fail or if an XWikiException occurred
     */
    void removeAnnotation(CharSequence documentName, CharSequence annotationID, XWikiContext context, Target target)
        throws AnnotationServiceException;

    /**
     * @param documentName name of concerned document
     * @param context
     * @param target define the kind of document
     * @return all annotations present in given document.
     * @throws AnnotationServiceException can be thrown if selection resolution fail or if an XWikiException occurred
     */
    Collection<Annotation> getAnnotations(CharSequence documentName, XWikiContext context, Target target)
        throws AnnotationServiceException;

    /**
     * NOTE: a safe annotation is an annotation which content hasn't been altered after annotation creation.
     * 
     * @param documentName
     * @param context
     * @param target define the kind of document
     * @return all safe annotations in the document
     * @throws AnnotationServiceException
     */
    Collection<Annotation> getSafeAnnotations(CharSequence documentName, XWikiContext context, Target target)
        throws AnnotationServiceException;
}
