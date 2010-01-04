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

package org.xwiki.annotation.maintainer.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.maintainer.AnnotationMaintainer;
import org.xwiki.annotation.maintainer.AnnotationState;
import org.xwiki.annotation.maintainer.XDelta;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.context.Execution;
import org.xwiki.observation.event.DocumentUpdateEvent;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationMaintainer extends AbstractLogEnabled implements AnnotationMaintainer
{
    /**
     * Execution object to get data about the current execution context.
     */
    @Requirement
    protected Execution execution;

    /**
     * Annotations storage service.
     */
    @Requirement
    protected IOService ioService;

    /**
     * Marks that there is currently an annotations update in progress so all the saves should not trigger a new update.
     * All document edits that take place because of updating the annotations for the current document shouldn't be
     * considered.
     */
    protected volatile boolean isUpdating;

    /**
     * The events observed by this observation manager.
     */
    protected final List<Event> eventsList = new ArrayList<Event>(Arrays.asList(new DocumentUpdateEvent()));

    /**
     * Returns the differences between the previous content and the current content, to be implemented by the subclasses
     * with the specific diff implementation.
     * 
     * @param previous the previous content
     * @param current the current content
     * @return the collection of differences between the old content and the new content
     */
    protected abstract Collection<XDelta> getDifferences(String previous, String current);

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getEvents()
     */
    public List<Event> getEvents()
    {
        return eventsList;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#getName()
     */
    public String getName()
    {
        return "AnnotationMaintainer";
    }

    /**
     * Proceed to update of annotations location.
     * 
     * @param documentName is name of document concerned by update
     * @param previousContent the previous content of the document (before the update)
     * @param currentContent the current content of the document (after the update)
     */
    protected void maintainDocumentAnnotations(String documentName, String previousContent, String currentContent)
    {
        Collection<Annotation> annotations;
        try {
            annotations = ioService.getSafeAnnotations(documentName);
            for (Annotation annotation : annotations) {
                recomputeProperties(annotation, previousContent, currentContent);
            }

            ioService.updateAnnotations(documentName, annotations);
        } catch (IOServiceException e) {
            getLogger().error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.AnnotationMaintainer#updateOffset(Annotation, int)
     */
    public void updateOffset(Annotation annotation, int offset)
    {
        annotation.setOffset(offset);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.AnnotationMaintainer #onAnnotationModification(Annotation,
     *      org.xwiki.annotation.maintainment.XDelta)
     */
    public void onAnnotationModification(Annotation annotation, XDelta delta)
    {
        annotation.setState(AnnotationState.ALTERED);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.AnnotationMaintainer
     *      #onSpecialCase(org.xwiki.annotation.maintainment.XDelta)
     */
    public void onSpecialCaseDeletion(Annotation annotation, XDelta delta, String previousContent, 
        String currentContent)
    {
        if (previousContent.substring(0, delta.getOffset()).endsWith(
            previousContent.substring(annotation.getOffset(), delta.getOffset() + delta.getLength()))) {
            updateOffset(annotation, annotation.getOffset() + delta.getSignedDelta());
        } else {
            onAnnotationModification(annotation, delta);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.maintainment.AnnotationMaintainer #onSpecialCaseAddition(Annotation,
     *      org.xwiki.annotation.maintainment.XDelta, String, String)
     */
    public void onSpecialCaseAddition(Annotation annotation, XDelta delta, String previousContent, 
        String currentContent)
    {
        if (currentContent.substring(delta.getOffset(), delta.getOffset() + delta.getLength()).endsWith(
            currentContent.substring(annotation.getOffset(), delta.getOffset()))) {
            updateOffset(annotation, annotation.getOffset() + delta.getSignedDelta());
        } else {
            onAnnotationModification(annotation, delta);
        }
    }

    /**
     * For each safe annotation, recompute location.
     * 
     * @param annotation the annotation to update properties for
     * @param previousContent the previous content of the updated document
     * @param currentContent the current content of the updated document
     */
    protected void recomputeProperties(Annotation annotation, String previousContent, String currentContent)
    {
        if (annotation.getState().equals(AnnotationState.ALTERED)) {
            return;
        }
        for (XDelta diff : getDifferences(previousContent, currentContent)) {
            diff.update(annotation, this, previousContent, currentContent);
        }
    }

    /**
     * Since previous content and current content use indifferently \r\n and \n we have to make content homogeneous.
     * 
     * @param content the content to normalize
     * @return the normalized content, with cleaned content
     */
    protected String normalizeContent(String content)
    {
        return content.replace("\r", "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument currentDocument = (XWikiDocument) source;

        XWikiDocument previousDocument = currentDocument.getOriginalDocument();

        // if it's not a modification triggered by the updates of the annotations while running the same annotation
        // maintainer, and the difference is in the content of the document
        if (!isUpdating && !previousDocument.getContent().equals(currentDocument.getContent())) {
            isUpdating = true;
            String content = currentDocument.getContent();
            String previousContent = previousDocument.getContent();
            content = normalizeContent(content);
            previousContent = normalizeContent(previousContent);
            maintainDocumentAnnotations(currentDocument.getFullName(), previousContent, content);
            isUpdating = false;
        }
    }
}
