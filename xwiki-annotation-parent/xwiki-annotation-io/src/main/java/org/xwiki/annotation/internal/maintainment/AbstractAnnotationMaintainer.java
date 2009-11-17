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

package org.xwiki.annotation.internal.maintainment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xwiki.annotation.AnnotationMaintainer;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.annotation.internal.maintainment.diff.XDelta;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.context.Execution;
import org.xwiki.observation.event.DocumentUpdateEvent;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationMaintainer extends AbstractLogEnabled implements AnnotationMaintainer
{
    @Requirement
    protected Execution execution;

    @Requirement
    protected IOService ioService;

    protected volatile boolean recursionFlag;

    protected static final String LISTENER_NAME = "AnnotationMaintainer";

    /**
     * The events observed by this observation manager.
     */
    protected final List<Event> eventsList = new ArrayList<Event>(Arrays.asList(new DocumentUpdateEvent()));

    protected String content;

    protected String previousContent;

    protected Annotation currentAnnotation;

    protected abstract Collection<XDelta> getDifferences(CharSequence previous, CharSequence current);

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
        return LISTENER_NAME;
    }

    /**
     * Proceed to update of annotations location.
     * 
     * @param documentName is name of document concerned by update
     * @param deprecatedContext the XWiki context used to manipulate XWiki objects
     */
    protected void maintainDocumentAnnotations(CharSequence documentName, XWikiContext deprecatedContext)
    {
        Collection<Annotation> annotations;
        try {
            annotations = ioService.getSafeAnnotations(documentName, deprecatedContext);
            for (Annotation annotation : annotations) {
                currentAnnotation = annotation;
                recomputeProperties();
            }
            ioService.updateAnnotations(documentName, annotations, deprecatedContext);
        } catch (IOServiceException e) {
            getLogger().error(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationMaintainer#updateOffset(int)
     */
    public void updateOffset(int offset)
    {
        currentAnnotation.setOffset(offset);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationMaintainer
     *      #onAnnotationModification(org.xwiki.annotation.internal.maintainment.diff.XDelta)
     */
    public void onAnnotationModification(XDelta delta)
    {
        currentAnnotation.setState(AnnotationState.ALTERED);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationMaintainer
     *      #onSpecialCase(org.xwiki.annotation.internal.maintainment.diff.XDelta)
     */
    public void onSpecialCaseDeletion(XDelta delta, int offset, int length)
    {
        if (previousContent.substring(0, delta.getOffset()).endsWith(
            previousContent.substring(offset, delta.getOffset() + delta.getLength()))) {
            updateOffset(offset + delta.getSignedDelta());
        } else {
            onAnnotationModification(delta);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationMaintainer
     *      #onSpecialCaseAddition(org.xwiki.annotation.internal.maintainment.diff.XDelta, int, int)
     */
    public void onSpecialCaseAddition(XDelta delta, int offset, int length)
    {
        if (content.substring(delta.getOffset(), delta.getOffset() + delta.getLength()).endsWith(
            content.substring(offset, delta.getOffset()))) {
            updateOffset(offset + delta.getSignedDelta());
        } else {
            onAnnotationModification(delta);
        }
    }

    /**
     * For each safe annotation, recompute location.
     */
    protected void recomputeProperties()
    {
        if (currentAnnotation.getState().equals(AnnotationState.ALTERED)) {
            return;
        }
        for (XDelta diff : getDifferences(previousContent, content)) {
            diff.update(this, currentAnnotation.getOffset(), currentAnnotation.getLength());
        }
    }

    /**
     * Since previous content and current content use indifferently \r\n and \n we have to make content homogeneous.
     */
    protected void clearContent()
    {
        previousContent = previousContent.replace("\r", "");
        content = content.replace("\r", "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiContext deprecatedContext = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        XWikiDocument currentDocument = (XWikiDocument) source;

        XWikiDocument previousDocument = currentDocument.getOriginalDocument();

        if (!recursionFlag && !previousDocument.getContent().equals(currentDocument.getContent())) {
            recursionFlag = true;
            content = currentDocument.getContent();
            previousContent = previousDocument.getContent();
            clearContent();
            maintainDocumentAnnotations(currentDocument.getFullName(), deprecatedContext);
            recursionFlag = false;
        }
    }
}
