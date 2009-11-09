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

package org.xwiki.annotation.internal.target;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.xwiki.annotation.IOService;
import org.xwiki.annotation.internal.annotation.AnnotationImpl;
import org.xwiki.annotation.internal.context.AlteredSource;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.annotation.internal.exception.SelectionMappingException;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.internal.selection.AlteredHTMLSelection;
import org.xwiki.annotation.internal.selection.SourceSegment;

import com.xpn.xwiki.XWikiContext;

/**
 * This class factors logic for adding an annotation to an XWiki document.
 * 
 * @version $Id$
 */
public abstract class AbstractDocumentContentTarget extends AbstractAnnotationTarget
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationTarget#addAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      java.lang.CharSequence, int, java.lang.CharSequence, java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user, XWikiContext context) throws AnnotationServiceException
    {
        try {
            Source source = getIOTargetService().getSource(documentName, context);
            AlteredSource alteredContext = getSourceAlterer().alter(source);
            AlteredHTMLSelection sel =
                getSelectionService().getAlteredHTMLSelection(getContentAlterer(), selection, selectionContext, offset);

            SourceSegment location = sel.mapToSource(alteredContext);
            AnnotationImpl annotation =
                new AnnotationImpl(documentName, user, new SimpleDateFormat(IOService.DATE_FORMAT).format(new Date()),
                    AnnotationState.SAFE, metadata, selection, selectionContext, 0, location.offset, location.length);
            getIOService().addAnnotation(documentName, annotation, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        } catch (SelectionMappingException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }
}
