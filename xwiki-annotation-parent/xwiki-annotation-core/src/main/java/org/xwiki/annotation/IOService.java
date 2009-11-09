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
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;

/**
 * This component provides services related to annotations storage and retrieval. <br />
 * TODO: this service definition should NOT depend on XWikiContext
 * 
 * @version $Id$
 */
@ComponentRole
public interface IOService
{
    // FIXME: remove this, format has nothing to do here, it's an XWiki implementation detail
    static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    /**
     * @param documentName name of concerned document
     * @param deprecatedContext the XWiki context needed to operate with XWiki objects
     * @return annotations concerning given document
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    Collection<Annotation> getAnnotations(CharSequence documentName, XWikiContext deprecatedContext)
        throws IOServiceException;

    /**
     * @param documentName the name of the document to get annotations for
     * @param deprecatedContext tthe XWiki context needed to operate with XWiki objects
     * @return safe annotations of a given document
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    Collection<Annotation> getSafeAnnotations(CharSequence documentName, XWikiContext deprecatedContext)
        throws IOServiceException;

    /**
     * Add annotation to a given document.
     * 
     * @param documentName concerned document name
     * @param annotation concerned annotation
     * @param deprecatedContext the XWiki context needed to operate with XWiki objects
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    void addAnnotation(CharSequence documentName, Annotation annotation, XWikiContext deprecatedContext)
        throws IOServiceException;

    /**
     * Remove a given annotation.
     * 
     * @param documentName concerned document
     * @param annotationID concerned annotation
     * @param deprecatedContext the XWiki context needed to operate with XWiki objects
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    void removeAnnotation(CharSequence documentName, CharSequence annotationID, XWikiContext deprecatedContext)
        throws IOServiceException;

    /**
     * Update given annotations information in database.
     * 
     * @param documentName concerned document
     * @param annotations annotations to update
     * @param deprecatedContext the XWiki context needed to operate with XWiki objects
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    void updateAnnotations(CharSequence documentName, Collection<Annotation> annotations, 
        XWikiContext deprecatedContext) throws IOServiceException;
}
