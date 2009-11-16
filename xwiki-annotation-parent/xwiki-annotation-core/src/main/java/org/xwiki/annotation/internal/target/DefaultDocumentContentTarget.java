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
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.ContentAlterer;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.IOTargetService;
import org.xwiki.annotation.SelectionService;
import org.xwiki.annotation.SourceAlterer;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.annotation.AnnotationImpl;
import org.xwiki.annotation.internal.context.AlteredSource;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.context.SourceImpl;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.annotation.internal.exception.SelectionMappingException;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.internal.selection.AlteredHTMLSelection;
import org.xwiki.annotation.internal.selection.SourceSegment;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.xpn.xwiki.XWikiContext;

/**
 * Defines components used for XWiki document target.
 * 
 * @version $Id$
 */
@Component()
public class DefaultDocumentContentTarget implements AnnotationTarget
{
    /**
     * The storage service for annotations.
     */
    @Requirement
    private IOService ioService;

    /**
     * The service providing selection cleanup functions.
     */
    @Requirement
    private SelectionService selectionService;

    /**
     * The storage service for annotation targets (documents).
     */
    @Requirement
    private IOTargetService documentContentTargetService;

    /**
     * The alterer for the source of the annotation target, to perform cleanup before mapping.
     */
    @Requirement("DOCUMENTCONTENT")
    private SourceAlterer documentSourceAlterer;

    /**
     * The alterer for the annotation selection, to perform cleanup before mapping. <br />
     * FIXME: this should be injected in the SelectionService implementation, as the selection cleanup does not depend
     * on the target of the annotation, but on the nature of the selection.
     */
    @Requirement("DOCUMENTCONTENT")
    private ContentAlterer documentContentAlterer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationTarget#addAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      java.lang.CharSequence, int, java.lang.CharSequence, java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user, XWikiContext context) throws AnnotationServiceException
    {
        Source source = null;
        try {
            source = documentContentTargetService.getSource(documentName, context);
            AlteredSource alteredContext = documentSourceAlterer.alter(source);
            AlteredHTMLSelection sel =
                selectionService.getAlteredHTMLSelection(documentContentAlterer, selection, selectionContext, offset);

            SourceSegment location = sel.mapToSource(alteredContext);
            AnnotationImpl annotation =
                new AnnotationImpl(documentName, user, new SimpleDateFormat(IOService.DATE_FORMAT).format(new Date()),
                    AnnotationState.SAFE, metadata, selection, selectionContext, 0, location.offset, location.length);
            ioService.addAnnotation(documentName, annotation, context);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException("An exception occurred when accessing the storage services: "
                + e.getMessage());
        } catch (SelectionMappingException e) {
            throw new AnnotationServiceException("Selection \"" + selection + "\" could not be mapped on source \""
                + source.getSource() + "\". \nCaused by: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationTarget#getAnnotatedHTML(java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public CharSequence getAnnotatedHTML(CharSequence documentName, XWikiContext context)
        throws AnnotationServiceException
    {
        try {
            Source source = documentContentTargetService.getSource(documentName, context);
            Collection<Annotation> annotations = ioService.getSafeAnnotations(documentName, context);

            if (annotations.isEmpty()) {
                return documentContentTargetService.getRenderedContent(documentName, source, context);
            }

            StringBuilder wikiSource =
                new StringBuilder(documentContentTargetService.getSource(documentName, context).getSource());
            Map<Integer, Integer> offsets = new TreeMap<Integer, Integer>();

            for (Annotation it : annotations) {
                // Determination of offset induced by others annotations
                int startOffset = 0;
                int endOffset = 0;
                for (Entry<Integer, Integer> couple : offsets.entrySet()) {
                    if (couple.getKey() <= it.getOffset()) {
                        startOffset += couple.getValue();
                    }
                    if (couple.getKey() <= it.getOffset() + it.getLength()) {
                        endOffset += couple.getValue();
                    }
                }

                // Updating map of offsets
                Integer value = offsets.get(it.getOffset());
                if (value == null) {
                    offsets.put(it.getOffset(), getAnnotationBeginTag(it.getId()).length());
                } else {
                    offsets.put(it.getOffset(), value + getAnnotationBeginTag(it.getId()).length());
                }
                value = offsets.get(it.getOffset() + it.getLength());
                if (value == null) {
                    offsets.put(it.getOffset() + it.getLength(), getAnnotationEndTag(it.getId()).length());
                } else {
                    offsets.put(it.getOffset() + it.getLength(), value + getAnnotationEndTag(it.getId()).length());
                }

                // Insertion
                wikiSource.insert(it.getOffset() + startOffset, getAnnotationBeginTag(it.getId()));
                wikiSource.insert(it.getOffset() + (getAnnotationBeginTag(it.getId()).length()) + (it.getLength())
                    + endOffset, getAnnotationEndTag(it.getId()));

            }

            Source annotatedSource = new SourceImpl(wikiSource);
            // Rendering
            String htmlContent =
                documentContentTargetService.getRenderedContent(documentName, annotatedSource, context).toString();
            int fromIndex;
            int toIndex;
            String oldSelection;
            String newSelection;
            for (Annotation it : annotations) {
                fromIndex = htmlContent.indexOf(getAnnotationBeginTag(it.getId()));
                toIndex = htmlContent.indexOf(getAnnotationEndTag(it.getId()));
                if (fromIndex < 0 || toIndex < 0) {
                    continue;
                }
                String annotationStartSpan =
                    "<span class=\"annotation ID" + it.getId() + "\" title=\"" + it.getAnnotation() + "\">";
                oldSelection = htmlContent.substring(fromIndex + (getAnnotationBeginTag(it.getId()).length()), toIndex);
                newSelection = oldSelection.replaceAll("<(/)?([^>]+?)>", "</span><$1$2>" + annotationStartSpan);
                htmlContent =
                    htmlContent.replace(htmlContent.subSequence(fromIndex, toIndex
                        + getAnnotationEndTag(it.getId()).length()), annotationStartSpan + newSelection + "</span>");
            }
            return htmlContent;
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }

    /**
     * @param annoID is ID of annotation (as saved in the annotation object)
     * @return mark to inject in xwiki source before rendering
     */
    private String getAnnotationBeginTag(int annoID)
    {
        return getAnnotationTag(annoID, false);
    }

    /**
     * @param annoID is ID of annotation (as saved in the annotation object)
     * @return mark to inject in xwiki source before rendering
     */
    private String getAnnotationEndTag(int annoID)
    {
        return getAnnotationTag(annoID, true);
    }

    /**
     * Builds and returns the annotation tag to insert in the source to mark the place of an annotation.
     * 
     * @param annoID the id of the annotation to get marker for
     * @param end {@code true} if this is a tag marking the end of an annotation, {@code false} if it's a start tag
     * @return the string representing the annotation tag
     */
    private String getAnnotationTag(int annoID, boolean end)
    {
        String annotationSeparator = "@@@";
        String annotationMarker = annotationSeparator + "annotation" + annotationSeparator;
        return end ? (annotationSeparator + annoID + annotationMarker)
            : (annotationMarker + annoID + annotationSeparator);
    }
}
