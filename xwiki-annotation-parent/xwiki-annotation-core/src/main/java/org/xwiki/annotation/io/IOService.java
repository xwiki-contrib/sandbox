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

package org.xwiki.annotation.io;

import java.util.Collection;

import org.xwiki.annotation.Annotation;
import org.xwiki.component.annotation.ComponentRole;

/**
 * This component provides services related to annotations storage and retrieval.
 * 
 * @version $Id$
 */
@ComponentRole
public interface IOService
{
    /**
     * @param documentName name of concerned document
     * @return annotations concerning given document
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    Collection<Annotation> getAnnotations(String documentName) throws IOServiceException;

    /**
     * @param documentName the name of the document to get annotations for
     * @return safe annotations of a given document
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    Collection<Annotation> getSafeAnnotations(String documentName) throws IOServiceException;

    /**
     * Add annotation to a given document.
     * 
     * @param documentName concerned document name
     * @param annotation concerned annotation
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    void addAnnotation(String documentName, Annotation annotation) throws IOServiceException;

    /**
     * Remove a given annotation.
     * 
     * @param documentName concerned document
     * @param annotationID concerned annotation
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    void removeAnnotation(String documentName, String annotationID) throws IOServiceException;

    /**
     * Update given annotations information in database.
     * 
     * @param documentName concerned document
     * @param annotations annotations to update
     * @throws IOServiceException can be thrown if any exception occurs while manipulating annotations store
     */
    void updateAnnotations(String documentName, Collection<Annotation> annotations) throws IOServiceException;
}
