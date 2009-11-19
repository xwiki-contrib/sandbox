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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.content.AlteredContent;
import org.xwiki.annotation.content.ContentAlterer;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.annotation.maintainer.AnnotationState;
import org.xwiki.annotation.selection.AlteredSelection;
import org.xwiki.annotation.selection.SelectionMappingException;
import org.xwiki.annotation.selection.SelectionService;
import org.xwiki.annotation.selection.SourceSegment;
import org.xwiki.annotation.target.AnnotationTarget;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;


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
     * Component manager used to lookup the content alterer needed for the specific document.
     */
    @Requirement
    private ComponentManager componentManager;

    /**
     * The storage service for annotation targets (documents).
     */
    @Requirement
    private IOTargetService documentContentTargetService;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.target.AnnotationTarget#addAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      java.lang.CharSequence, int, java.lang.CharSequence, java.lang.CharSequence)
     */
    public void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user) throws AnnotationServiceException
    {
        String source = null;
        try {
            // get the documentContentAlterer to use for the document to add annotation on. And for the selection
            // TODO: this should be looked up depending on the document syntax
            // FIXME: a HTML filter could be used for the selection but since the selection is plain text we use the
            // same filtering as for document content
            ContentAlterer documentContentAlterer = componentManager.lookup(ContentAlterer.class, "xwiki/2.0");
            source = documentContentTargetService.getSource(documentName);
            // leave only relevant content in the document source
            AlteredContent alteredDocSource = documentContentAlterer.alter(source);
            // leave only relevant content in the selection
            AlteredSelection sel =
                new AlteredSelection(documentContentAlterer.alter(selection), documentContentAlterer
                    .alter(selectionContext), offset);
            // find the location of the selection in the source of the document
            SourceSegment location = selectionService.mapToSource(sel, alteredDocSource);

            // create the annotation with this data and send it to the storage service
            // FIXME: annotation date is not sure to be parsable back because there is no format for it, but it doesn't
            // matter as ftm it's not used at that level
            Annotation annotation =
                new Annotation(documentName, user, new Date().toString(), AnnotationState.SAFE, metadata, selection,
                    selectionContext, 0, location.offset, location.length);
            ioService.addAnnotation(documentName, annotation);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException("An exception occurred when accessing the storage services", e);
        } catch (SelectionMappingException e) {
            throw new AnnotationServiceException("Selection \"" + selection + "\" could not be mapped on source \""
                + source + "\".", e);
        } catch (ComponentLookupException e) {
            throw new AnnotationServiceException(
                "No suitable filter was found for mapping the selection on the source document", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.target.AnnotationTarget#getAnnotatedHTML(java.lang.CharSequence)
     */
    public CharSequence getAnnotatedHTML(CharSequence documentName)
        throws AnnotationServiceException
    {
        try {
            String source = documentContentTargetService.getSource(documentName);
            Collection<Annotation> annotations = ioService.getSafeAnnotations(documentName);

            if (annotations.isEmpty()) {
                return documentContentTargetService.getRenderedContent(documentName, source);
            }

            StringBuilder markedSource =
                new StringBuilder(documentContentTargetService.getSource(documentName));
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
                markedSource.insert(it.getOffset() + startOffset, getAnnotationBeginTag(it.getId()));
                markedSource.insert(it.getOffset() + (getAnnotationBeginTag(it.getId()).length()) + (it.getLength())
                    + endOffset, getAnnotationEndTag(it.getId()));

            }

            // Rendering
            String htmlContent =
                documentContentTargetService.getRenderedContent(documentName, markedSource.toString())
                    .toString();
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
            throw new AnnotationServiceException(e);
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
