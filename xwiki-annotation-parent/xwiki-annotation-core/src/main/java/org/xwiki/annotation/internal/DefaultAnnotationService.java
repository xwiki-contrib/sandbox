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

package org.xwiki.annotation.internal;

import java.util.Collection;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.target.AnnotationTarget;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.xpn.xwiki.XWikiContext;

/**
 * Default annotation service, using the default IOService and annotation target.
 * 
 * @version $Id$
 */
@Component
public class DefaultAnnotationService implements AnnotationService
{
    /**
     * The storage service for annotations.
     */
    @Requirement
    private IOService ioService;

    /**
     * The service managing functions for the target of an annotation (a document).
     */
    @Requirement
    private AnnotationTarget annotationTarget;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#addAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      java.lang.CharSequence, int, java.lang.CharSequence, java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user, XWikiContext context) throws AnnotationServiceException
    {
        try {
            annotationTarget.addAnnotation(metadata, selection, selectionContext, offset, documentName, user, context);
        } catch (AnnotationServiceException e) {
            throw new AnnotationServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotatedHTML(java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public CharSequence getAnnotatedHTML(CharSequence documentName, XWikiContext context)
        throws AnnotationServiceException
    {
        try {
            return annotationTarget.getAnnotatedHTML(documentName, context);
        } catch (AnnotationServiceException e) {
            throw new AnnotationServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotations(java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public Collection<Annotation> getAnnotations(CharSequence documentName, XWikiContext context)
        throws AnnotationServiceException
    {
        try {
            return ioService.getAnnotations(documentName, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getSafeAnnotations(java.lang.CharSequence,
     *      com.xpn.xwiki.XWikiContext)
     */
    public Collection<Annotation> getSafeAnnotations(CharSequence documentName, XWikiContext context)
        throws AnnotationServiceException
    {
        try {
            return ioService.getSafeAnnotations(documentName, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#removeAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      com.xpn.xwiki.XWikiContext)
     */
    public void removeAnnotation(CharSequence documentName, CharSequence annotationID, XWikiContext context)
        throws AnnotationServiceException
    {
        try {
            ioService.removeAnnotation(documentName, annotationID, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }
}
