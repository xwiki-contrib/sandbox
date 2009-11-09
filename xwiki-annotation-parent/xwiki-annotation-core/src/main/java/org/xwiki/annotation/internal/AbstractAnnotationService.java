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

import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.TargetResolver;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationService implements AnnotationService
{
    protected abstract IOService getIOService();

    protected abstract TargetResolver getTargetSelector();

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#addAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      java.lang.CharSequence, int, java.lang.CharSequence, java.lang.CharSequence, com.xpn.xwiki.XWikiContext,
     *      org.xwiki.annotation.AnnotationService.Target)
     */
    public void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user, XWikiContext context, Target target)
        throws AnnotationServiceException
    {
        try {
            getTargetSelector().resolve(target).addAnnotation(metadata, selection, selectionContext, offset,
                documentName, user, context);
        } catch (AnnotationServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotatedHTML(java.lang.CharSequence, com.xpn.xwiki.XWikiContext,
     *      org.xwiki.annotation.AnnotationService.Target)
     */
    public CharSequence getAnnotatedHTML(CharSequence documentName, XWikiContext context, Target target)
        throws AnnotationServiceException
    {
        try {
            return getTargetSelector().resolve(target).getAnnotatedHTML(documentName, context);
        } catch (AnnotationServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotations(java.lang.CharSequence, com.xpn.xwiki.XWikiContext,
     *      org.xwiki.annotation.AnnotationService.Target)
     */
    public Collection<Annotation> getAnnotations(CharSequence documentName, XWikiContext context, Target target)
        throws AnnotationServiceException
    {
        try {
            return getIOService().getAnnotations(documentName, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getSafeAnnotations(java.lang.CharSequence,
     *      com.xpn.xwiki.XWikiContext, org.xwiki.annotation.AnnotationService.Target)
     */
    public Collection<Annotation> getSafeAnnotations(CharSequence documentName, XWikiContext context, Target target)
        throws AnnotationServiceException
    {
        try {
            return getIOService().getSafeAnnotations(documentName, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#removeAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      com.xpn.xwiki.XWikiContext, org.xwiki.annotation.AnnotationService.Target)
     */
    public void removeAnnotation(CharSequence documentName, CharSequence annotationID, XWikiContext context,
        Target target) throws AnnotationServiceException
    {
        try {
            getIOService().removeAnnotation(documentName, annotationID, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }
}
