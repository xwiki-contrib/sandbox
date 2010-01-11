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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.content.ContentAlterer;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.annotation.maintainer.AnnotationState;
import org.xwiki.annotation.maintainer.XDelta;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.DocumentUpdateEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.PrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationMaintainer extends AbstractLogEnabled implements EventListener
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
     * Content storage and manipulation service.
     */
    @Requirement
    protected IOTargetService ioContentService;

    /**
     * The component manager, used to grab the plain text renderer.
     */
    @Requirement
    protected ComponentManager componentManager;

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
     * Update the annotations on the passed content.
     * 
     * @param documentName is name of document concerned by update
     * @param previousContent the previous content of the document (before the update)
     * @param currentContent the current content of the document (after the update)
     */
    protected void maintainDocumentAnnotations(String documentName, String previousContent, String currentContent)
    {
        Collection<Annotation> annotations;
        try {
            annotations = ioService.getAnnotations(documentName);

            if (annotations.size() == 0) {
                // no annotations, nothing to do
                return;
            }

            // produce the ptr of the previous and current, wrt to syntax
            String syntaxId = ioContentService.getSourceSyntax(documentName);
            String renderedPreviousContent = renderPlainText(previousContent, syntaxId);
            String renderedCurrentContent = renderPlainText(currentContent, syntaxId);

            // create the diffs
            Collection<XDelta> differences = getDifferences(renderedPreviousContent, renderedCurrentContent);
            // if any differences: note that there can be updates on the content that have no influence on the plain
            // text space normalized version
            if (differences.size() > 0) {
                // recompute properties for all annotations
                for (Annotation annotation : annotations) {
                    recomputeProperties(annotation, differences, renderedPreviousContent, renderedCurrentContent);
                }
            }

            // finally store the updates
            ioService.updateAnnotations(documentName, annotations);
        } catch (Exception e) {
            getLogger().error("An exception occurred while updating annotations for content at " + documentName, e);
        }
    }

    /**
     * Helper method to render the plain text version of the passed content.
     * 
     * @param content the content to render in plain text
     * @param syntaxId the source syntax of the content to render
     * @throws Exception if anything goes wrong while rendering the content
     * @return the normalized plain text rendered content
     */
    private String renderPlainText(String content, String syntaxId) throws Exception
    {
        PrintRenderer renderer = componentManager.lookup(PrintRenderer.class, "annotations-maintainer-plain/1.0");

        // parse
        Parser parser = componentManager.lookup(Parser.class, syntaxId);
        XDOM xdom = parser.parse(new StringReader(content));

        // run transformations -> although it's going to be at least strange to handle rendered content since there
        // is no context
        SyntaxFactory syntaxFactory = componentManager.lookup(SyntaxFactory.class);
        Syntax sourceSyntax = syntaxFactory.createSyntaxFromIdString(syntaxId);
        TransformationManager transformationManager = componentManager.lookup(TransformationManager.class);
        transformationManager.performTransformations(xdom, sourceSyntax);

        // render
        WikiPrinter printer = new DefaultWikiPrinter();
        renderer.setPrinter(printer);

        xdom.traverse(renderer);

        return printer.toString();
    }

    /**
     * For each annotation, recompute its properties wrt the differences in the document.
     * 
     * @param annotation the annotation to update properties for
     * @param differences the differences between {@code renderedPreviousContent} and {@code renderedCurrentContent}
     * @param renderedPreviousContent the plain text space normalized rendered previous content
     * @param renderedCurrentContent the plain text space normalized rendered current content
     */
    protected void recomputeProperties(Annotation annotation, Collection<XDelta> differences,
        String renderedPreviousContent, String renderedCurrentContent)
    {
        // TODO: do we still want this here? Does altered make sense any more?
        if (annotation.getState().equals(AnnotationState.ALTERED)) {
            return;
        }

        // FIXME: remove this from here if ever selection & context normalization of annotation will be done on add
        String normalizedSelection = normalizeContent(annotation.getInitialSelection());
        String normalizedContext = normalizeContent(annotation.getSelectionContext());
        int cStart = renderedPreviousContent.indexOf(normalizedContext);

        if (cStart < 0) {
            // annotation context could not be found in the previous rendered content, it must be somewhere in the
            // generated content or something like that, skip it
            return;
        }

        // assume at this point that selection appears only once in the context
        int cLeftSize = normalizedContext.indexOf(normalizedSelection);
        int sStart = cStart + cLeftSize;
        int sEnd = sStart + normalizedSelection.length();
        int cRightSize = cStart + normalizedContext.length() - sEnd;

        int alteredCStart = cStart;
        int alteredSLenth = sEnd - sStart;

        for (XDelta diff : differences) {
            int dStart = diff.getOffset();
            int dEnd = diff.getOffset() + diff.getOriginal().length();
            // 1/ if the diff is before the selection, update the position of the context, to preserve the selection
            // offset
            if (dEnd < sStart) {
                alteredCStart += diff.getSignedDelta();
            }
            // 2/ diff is inside the selection
            if (dStart >= sStart && dEnd < sEnd) {
                // update the selection length
                alteredSLenth += diff.getSignedDelta();
                // FIXME: not yet, this is not recognized properly by the client, nor used
                // annotation.setState(AnnotationState.UPDATED);
            }

            // 3/ the edit overlaps the annotation selection completely
            if (dStart <= sStart && dEnd >= sEnd) {
                // mark annotation as altered and drop it
                annotation.setState(AnnotationState.ALTERED);
                break;
            }

            // 4/ the edit overlaps the start of the annotation
            if (dStart < dStart && dEnd >= sStart && dEnd <= sEnd) {
                // FIXME: ftm mark as altered
                annotation.setState(AnnotationState.ALTERED);
                break;
            }

            // 5/ the edit overlaps the end of the annotation
            if (dStart < sEnd && dEnd >= sEnd) {
                // FIXME: ftm mark as altered
                annotation.setState(AnnotationState.ALTERED);
                break;
            }
        }

        // recompute the annotation context and all
        String newContext =
            renderedCurrentContent.substring(alteredCStart, alteredCStart + cLeftSize + alteredSLenth + cRightSize);
        annotation.setSelection(newContext, cLeftSize, alteredSLenth);

        if (annotation.getState() != AnnotationState.ALTERED) {
            // TODO: ensure uniqueness
        }
    }

    /**
     * Helper function to clean the passed content. To be used to generate the normalized spaces version of the
     * selection and its context.
     * 
     * @param content the content to normalize
     * @return the content with normalized-spaces
     */
    protected String normalizeContent(String content)
    {
        ContentAlterer normalizerContentAlterer;
        try {
            normalizerContentAlterer = componentManager.lookup(ContentAlterer.class, "space-normalizer");
            return normalizerContentAlterer.alter(content).getContent().toString();
        } catch (ComponentLookupException e) {
            getLogger().error(e.getMessage(), e);
        }
        // if something went wrong fetching the alterer, return original version, assume it's ok
        return content;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.observation.EventListener#onEvent(org.xwiki.observation.event.Event, java.lang.Object,
     *      java.lang.Object)
     */
    public void onEvent(Event event, Object source, Object data)
    {
        DocumentModelBridge currentDocument = (DocumentModelBridge) source;

        DocumentModelBridge previousDocument = currentDocument.getOriginalDocument();

        // if it's not a modification triggered by the updates of the annotations while running the same annotation
        // maintainer, and the difference is in the content of the document
        // FIXME: should update also if an object is modified as the content of the object could be used in the
        // rendering of the document. But for this the transformations need to be run
        if (!isUpdating && !previousDocument.getContent().equals(currentDocument.getContent())) {
            isUpdating = true;
            String content = currentDocument.getContent();
            String previousContent = previousDocument.getContent();
            maintainDocumentAnnotations(currentDocument.getFullName(), previousContent, content);
            isUpdating = false;
        }
    }
}
