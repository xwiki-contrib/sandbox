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
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
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
 * Event listener to listen to documents update events and update the annotations that are impacted by the document
 * change, to update the selection and context to match the new document content. <br />
 * FIXME: fix me: sky high complexity of the functions & fan-out. Split to be able to potentially test on small pieces,
 * and decouple event handling logic & actual update logic.
 * 
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
     * Entity reference serializer, to serialize the modified document reference to send to the annotations service.
     */
    @Requirement
    protected EntityReferenceSerializer<String> serializer;

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
     * @param documentReference is name of document concerned by update
     * @param previousContent the previous content of the document (before the update)
     * @param currentContent the current content of the document (after the update)
     */
    protected void maintainDocumentAnnotations(String documentReference, String previousContent, String currentContent)
    {
        Collection<Annotation> annotations;
        try {
            annotations = ioService.getAnnotations(documentReference);

            if (annotations.size() == 0) {
                // no annotations, nothing to do
                return;
            }

            // produce the ptr of the previous and current, wrt to syntax
            String syntaxId = ioContentService.getSourceSyntax(documentReference);
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
            ioService.updateAnnotations(documentReference, annotations);
        } catch (Exception e) {
            getLogger()
                .error("An exception occurred while updating annotations for content at " + documentReference, e);
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
        PrintRenderer renderer = componentManager.lookup(PrintRenderer.class, "normalizer-plain/1.0");

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
        // TODO: do we still want this here? Do we want to try to recover altered annotations?
        if (annotation.getState().equals(AnnotationState.ALTERED)) {
            return;
        }

        // TODO: remove this from here if ever selection & context normalization of annotation will be done on add
        String normalizedSelection = normalizeContent(annotation.getSelection());
        String normalizedContext = normalizeContent(annotation.getSelectionContext());
        int cStart = renderedPreviousContent.indexOf(normalizedContext);

        if (cStart < 0) {
            // annotation context could not be found in the previous rendered content, it must be somewhere in the
            // generated content or something like that, skip it
            return;
        }

        // save initial annotation state, to check how it needs to be updated afterwards
        AnnotationState initialState = annotation.getState();

        // assume at this point that selection appears only once in the context
        int cLeftSize = normalizedContext.indexOf(normalizedSelection);
        int sStart = cStart + cLeftSize;
        int sEnd = sStart + normalizedSelection.length();
        int cRightSize = cStart + normalizedContext.length() - sEnd;

        int alteredCStart = cStart;
        int alteredSLength = sEnd - sStart;

        for (XDelta diff : differences) {
            int dStart = diff.getOffset();
            int dEnd = diff.getOffset() + diff.getOriginal().length();
            // 1/ if the diff is before the selection, or ends exactly where selection starts, update the position of
            // the context, to preserve the selection offset
            if (dEnd <= sStart) {
                alteredCStart += diff.getSignedDelta();
            }
            // 2/ diff is inside the selection (and not the previous condition)
            if (dEnd > sStart && dStart >= sStart && dStart < sEnd && dEnd <= sEnd) {
                // update the selection length
                alteredSLength += diff.getSignedDelta();
                annotation.setState(AnnotationState.UPDATED);
            }

            // 3/ the edit overlaps the annotation selection completely
            if (dStart <= sStart && dEnd >= sEnd) {
                // mark annotation as altered and drop it
                annotation.setState(AnnotationState.ALTERED);
                break;
            }

            // 4/ the edit overlaps the start of the annotation
            if (dStart < sStart && dEnd > sStart && dEnd <= sEnd) {
                // shift with the signed delta to the right, assume that the edit took place before the annotation and
                // keep its size. This way it will be mapped at the position as if the edit would have taken place
                // before it and will contain the new content at the start of the annotation
                alteredCStart += diff.getSignedDelta();
                annotation.setState(AnnotationState.UPDATED);
            }

            // 5/ the edit overlaps the end of the annotation
            if (dStart < sEnd && dEnd > sEnd) {
                // nothing, behave as if the edit would have taken place after the annotation
                annotation.setState(AnnotationState.UPDATED);
            }
        }

        if (annotation.getState() != AnnotationState.ALTERED) {
            // recompute the annotation context and all
            String newContext =
                renderedCurrentContent
                    .substring(alteredCStart, alteredCStart + cLeftSize + alteredSLength + cRightSize);
            // if this annotation was updated first time during this update, set its original selection
            if (annotation.getState() == AnnotationState.UPDATED && initialState == AnnotationState.SAFE) {
                annotation.setOriginalSelection(annotation.getSelection());
            }
            // and finally update the context & selection
            annotation.setSelection(newContext, cLeftSize, alteredSLength);

            // make sure annotation stays unique
            ensureUnique(annotation, renderedCurrentContent, alteredCStart, cLeftSize, alteredSLength, cRightSize);
        }
    }

    /**
     * Helper function to adjust passed annotation to make sure it is unique in the content.
     * 
     * @param annotation the annotation to ensure uniqueness for
     * @param content the content in which the annotation must be unique
     * @param cStart precomputed position where the annotation starts, passed here for cache reasons
     * @param cLeftSize precomputed length of the context to the left side of the selection inside the annotation
     *            context, passed here for cache reasons
     * @param sLength precomputed length of the annotation selection, passed here for cache reasons
     * @param cRightSize precomputed length of the context to the right side of the selection inside the annotation,
     *            passed here for cache reasons
     */
    private void ensureUnique(Annotation annotation, String content, int cStart, int cLeftSize, int sLength,
        int cRightSize)
    {
        // find out if there is another encounter of the selection text & context than the one at cStart
        List<Integer> occurrences = getOccurrences(content, annotation.getSelectionContext(), cStart);
        if (occurrences.size() == 0) {
            // it appears only once, it's done
            return;
        }

        // enlarge the context to the left and right with one character, until it is unique
        boolean isUnique = false;
        int cLength = cLeftSize + sLength + cRightSize;
        // size expansion of the context of the annotation such as it becomes unique
        int expansionLeft = 0;
        int expansionRight = 0;
        // the characters corresponding to the ends of the expanded context, to compare with all other occurrences and
        // check if they're unique
        // TODO: an odd situation can happen by comparing characters: at each expansion position there's another
        // occurrence that matches, therefore an unique context is never found although it exists
        // TODO: maybe expansion should be considered by words?
        char charLeft = content.charAt(cStart - expansionLeft);
        char charRight = content.charAt(cStart + cLength + expansionRight - 1);
        while (!isUnique) {
            boolean updated = false;
            // get the characters at left and right and expand, but only if the positions are valid. If one stops being
            // valid, only the other direction will be expanded in search of a new context
            if (cStart - expansionLeft - 1 > 0) {
                expansionLeft++;
                charLeft = content.charAt(cStart - expansionLeft);
                updated = true;
            }
            if (cStart + cLength + expansionRight + 1 <= content.length()) {
                expansionRight++;
                charRight = content.charAt(cStart + cLength + expansionRight - 1);
                updated = true;
            }
            if (!updated) {
                // couldn't update the context to the left nor to the right
                break;
            }
            if (charLeft == ' ' || charRight == ' ') {
                // don't consider uniqueness from space chars
                continue;
            }
            // assume it's unique
            isUnique = true;
            // and check again all occurrences
            for (int occurence : occurrences) {
                // get the chars relative to the current occurrence at the respective expansion positions to the right
                // and left
                Character occurenceCharLeft = getSafeCharacter(content, occurence - expansionLeft);
                Character occurenceCharRight = getSafeCharacter(content, occurence + cLength + expansionRight - 1);
                if ((occurenceCharLeft != null && occurenceCharLeft.charValue() == charLeft)
                    && (occurenceCharRight != null && occurenceCharRight.charValue() == charRight)) {
                    isUnique = false;
                    break;
                }
            }
        }
        if (isUnique) {
            // update the context with the new indexes
            // expand the context to the entire word that it touches (just to make more sense and not depend with only
            // one letter)
            expansionLeft = expansionLeft + toNextWord(content, cStart - expansionLeft, true);
            expansionRight = expansionRight + toNextWord(content, cStart + cLength + expansionRight, false);
            String newContext = content.substring(cStart - expansionLeft, cStart + cLength + expansionRight);
            // normally selection is not updated here, only the context therefore we don't set original selection
            annotation.setSelection(newContext, cLeftSize + expansionLeft, sLength);
        } else {
            // left the loop for other reasons: for example couldn't expand context
            // leave it unchanged there's not much we could do anyway
        }
    }

    /**
     * Helper function to get all occurrences of {@code pattern} in {@code subject}.
     * 
     * @param subject the subject of the search
     * @param pattern the pattern of the search
     * @param exclude value to exclude from the results set
     * @return the list of all occurrences of {@code pattern} in {@code subject}
     */
    private List<Integer> getOccurrences(String subject, String pattern, int exclude)
    {
        List<Integer> indexes = new ArrayList<Integer>();
        int lastIndex = subject.indexOf(pattern);
        while (lastIndex != -1) {
            if (lastIndex != exclude) {
                indexes.add(lastIndex);
            }
            lastIndex = subject.indexOf(pattern, lastIndex + 1);
        }

        return indexes;
    }

    /**
     * Helper function to advance to the next word in the subject, until the first space is encountered, starting from
     * {@code position} and going to the left or to the right, as {@code toLeft} specifies. The returned value is the
     * length of the offset from position to where the space was found.
     * 
     * @param subject the string to search for spaces in
     * @param position the position to start the search from
     * @param toLeft {@code true} if the search should be done to the left of the string, {@code false} otherwise
     * @return the offset starting from position, to the left or to the right, until the next word starts (or the
     *         document ends)
     */
    private int toNextWord(String subject, int position, boolean toLeft)
    {
        int expansion = 1;
        // advance until the next space is encountered in subject, from position, to the right by default and left if
        // it's specified otherwise
        boolean isSpaceOrEnd =
            toLeft ? position - expansion < 0 || subject.charAt(position - expansion) == ' '
                : position + expansion > subject.length() || subject.charAt(position + expansion - 1) == ' ';
        while (!isSpaceOrEnd) {
            expansion++;
            isSpaceOrEnd =
                toLeft ? position - expansion < 0 || subject.charAt(position - expansion) == ' '
                    : position + expansion > subject.length() || subject.charAt(position + expansion - 1) == ' ';
        }

        return expansion - 1;
    }

    /**
     * Helper function to safely get the character at position {@code position} in the passed content, or null
     * otherwise.
     * 
     * @param content the content to get the character from
     * @param position the position to get character at
     * @return the character at position {@code position} or {@code null} otherwise.
     */
    private Character getSafeCharacter(String content, int position)
    {
        if (position >= 0 && position < content.length()) {
            return content.charAt(position);
        } else {
            return null;
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
            // create the document reference
            EntityReference docReference =
                new EntityReference(currentDocument.getPageName(), EntityType.DOCUMENT, new EntityReference(
                    currentDocument.getSpaceName(), EntityType.SPACE, new EntityReference(
                        currentDocument.getWikiName(), EntityType.WIKI)));
            // serialize
            maintainDocumentAnnotations(serializer.serialize(docReference), previousContent, content);
            isUpdating = false;
        }
    }
}
