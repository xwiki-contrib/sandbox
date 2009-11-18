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

package org.xwiki.annotation.target;

import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;

/**
 * This interface defines services relative to the target of an annotation (the document on which it is added).
 * 
 * @version $Id$
 */
@ComponentRole
public interface AnnotationTarget
{
    /**
     * Request addition of new annotation.
     * 
     * @param metadata annotation content
     * @param selection HTML selection concerned by annotation
     * @param selectionContext HTML selection context
     * @param offset offset of the selection in context
     * @param documentName the name of the document containing annotation
     * @param user the author of the annotation
     * @param context the XWiki context to manipulate XWiki objects
     * @throws AnnotationServiceException can be thrown if selection resolution fail or if an XWikiException occurred
     */
    void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user, XWikiContext context) throws AnnotationServiceException;

    /**
     * @param documentName refers document to render
     * @param context the XWiki context to manipulate XWiki objects
     * @return annotated and rendered document
     * @throws AnnotationServiceException can be thrown if selection resolution fail or if an XWikiException occurred
     */
    CharSequence getAnnotatedHTML(CharSequence documentName, XWikiContext context) throws AnnotationServiceException;
}
