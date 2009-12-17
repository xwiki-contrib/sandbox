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
package org.xwiki.annotation.internal.renderer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.renderer.AnnotationBookmarks;
import org.xwiki.annotation.renderer.AnnotationChainingPrintRenderer;
import org.xwiki.annotation.renderer.AnnotationEvent;
import org.xwiki.annotation.renderer.EventReference;
import org.xwiki.annotation.renderer.AnnotationEvent.AnnotationEventType;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.xhtml.XHTMLImageRenderer;
import org.xwiki.rendering.renderer.xhtml.XHTMLLinkRenderer;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Extends the default XHTML renderer to add handling of annotations.<br />
 * FIXME: FTM don't consider offsets, just to see how it works with bookmarks.
 * 
 * @version $Id$
 */
public class AnnotationXHTMLChainingRenderer extends XHTMLChainingRenderer implements AnnotationChainingPrintRenderer
{
    /**
     * The annotation marker element in HTML.
     */
    private static final String ANNOTATION_MARKER = "span";

    /**
     * The bookmarks of the annotations to render on this content.
     */
    private AnnotationBookmarks bookmarks = new AnnotationBookmarks();

    /**
     * Map to store the events count to be able to identify an event in the emitted events.
     */
    private Map<EventType, Integer> eventsCount = new HashMap<EventType, Integer>();

    /**
     * Flag to signal if the annotations in the openAnnotations stack are actually opened in the printed XHTML. Namely
     * this will become true when a text event will occur (like a word or space or ...) and will become false when and
     * end event will occur.
     */
    private boolean open;

    /**
     * The current opened annotations but not closed (i.e. for which beginAnnotation was signaled but not
     * endAnnotation). Used for correctly nesting the annotations markers with other XHTML elements. <br />
     * TODO: find a better name, which would mean "all annotations which are currently being rendered"
     */
    private List<Annotation> openAnnotations = new LinkedList<Annotation>();

    /**
     * Constructor from super class.
     * 
     * @param linkRenderer the renderer for links
     * @param imageRenderer the renderer for images
     * @param listenerChain the listener chain in which to add this listener
     */
    public AnnotationXHTMLChainingRenderer(XHTMLLinkRenderer linkRenderer, XHTMLImageRenderer imageRenderer,
        ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, listenerChain);
    }

    /**
     * Handles the beginning of a new annotation.
     * 
     * @param annotation the annotation that begins
     */
    public void beginAnnotation(Annotation annotation)
    {
        // and put it in the stack of open annotations
        openAnnotations.add(annotation);
        // if all other annotations are opened, open this one too. Otherwise it will be opened whenever all the others
        // are opened.
        if (open) {
            printAnnotationStartMarker(annotation);
        }
    }

    /**
     * Handles the end of an annotation.
     * 
     * @param annotation the annotation that ends
     */
    public void endAnnotation(Annotation annotation)
    {
        // all annotations which are opened after this one must be closed before this close and reopened after
        int annIndex = openAnnotations.indexOf(annotation);
        // close all annotations opened after this one, in reverse order
        for (int i = openAnnotations.size() - 1; i > annIndex; i--) {
            printAnnotationEndMarker(openAnnotations.get(i));
        }
        // close this annotation
        printAnnotationEndMarker(annotation);
        // open all previously closed annotations in the order they were initially opened
        for (int i = annIndex + 1; i < openAnnotations.size(); i++) {
            printAnnotationStartMarker(openAnnotations.get(i));
        }
        // and remove it from the list of open annotations
        openAnnotations.remove(annotation);
    }

    /**
     * Prints the start marker for the passed annotation.
     * 
     * @param annotation the annotation to print the start marker for
     */
    private void printAnnotationStartMarker(Annotation annotation)
    {
        Map<String, String> attributes = new LinkedHashMap<String, String>();

        attributes.put("class", "annotation ID" + annotation.getId());
        attributes.put("title", annotation.getAnnotation().toString());
        getXHTMLWikiPrinter().printXMLStartElement(ANNOTATION_MARKER, attributes);
    }

    /**
     * Prints the end marker for the passed annotation.
     * 
     * @param annotation the annotation to print end marker for
     */
    private void printAnnotationEndMarker(Annotation annotation)
    {
        getXHTMLWikiPrinter().printXMLEndElement(ANNOTATION_MARKER);
    }

    /**
     * Helper function to handle closing all annotations. To be called either when elements close or when an element
     * opens (all annotation spans will only wrap text, not inner elements). It will close all opened annotations
     * markers and set the flag to specify that annotations are closed and they should be opened at next text element.
     */
    private void closeAllAnnotations()
    {
        // if the annotations are opened
        if (open) {
            // for each annotation from the last opened to the first opened
            for (int i = openAnnotations.size() - 1; i >= 0; i--) {
                // close it
                printAnnotationEndMarker(openAnnotations.get(i));
            }
            // set the flag so that next end event doesn't close them as well
            open = false;
        }
    }

    /**
     * Helper function to handle opening all annotations. If the annotations are not already opened, it should open them
     * all and set the flag to opened so that next text event doesn't do the same.
     */
    private void openAllAnnotations()
    {
        // if annotations are not opened
        if (!open) {
            // for each annotation in the order in which they were opened
            for (int i = 0; i < openAnnotations.size(); i++) {
                // re-open it
                printAnnotationStartMarker(openAnnotations.get(i));
            }
            // and mark the annotations as opened
            open = true;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onWord(java.lang.String)
     */
    @Override
    public void onWord(String word)
    {
        // if there is a begin annotation bookmark in this event (regardless of the offset ftm, call begin annotation
        // for all of them)
        // build an event that would allow to search in the bookmarks
        EventReference currentEvt = new EventReference(EventType.ON_WORD, getAndIncrement(EventType.ON_WORD));
        // and go, for all events which are start events, begin them before
        beginAllAnnotations(currentEvt);
        openAllAnnotations();
        super.onWord(word);
        // for all the events which are end events, end them after
        endAllAnnotations(currentEvt);
    }

    /**
     * Helper function to begin all the annotations that start in the passed event, regardless of the offset. <br />
     * FIXME: start annotations at the right offsets and remove this function
     * 
     * @param currentEvent the event to begin all annotations for
     */
    private void beginAllAnnotations(EventReference currentEvent)
    {
        if (bookmarks.get(currentEvent) == null) {
            // nothing to do if there is no bookmark on this event
            return;
        }

        for (Map.Entry<Integer, List<AnnotationEvent>> bookmark : bookmarks.get(currentEvent).entrySet()) {
            for (AnnotationEvent annEvt : bookmark.getValue()) {
                if (annEvt.getType() == AnnotationEventType.START) {
                    beginAnnotation(annEvt.getAnnotation());
                }
            }
        }
    }

    /**
     * Helper function to end all the annotations that end in the passed event, regardless of the offset. <br />
     * FIXME: start annotations at the right offsets and remove this function
     * 
     * @param currentEvent the event end all annotations for
     */
    private void endAllAnnotations(EventReference currentEvent)
    {
        if (bookmarks.get(currentEvent) == null) {
            // nothing to do if there is no bookmark on this event
            return;
        }

        for (Map.Entry<Integer, List<AnnotationEvent>> bookmark : bookmarks.get(currentEvent).entrySet()) {
            for (AnnotationEvent annEvt : bookmark.getValue()) {
                if (annEvt.getType() == AnnotationEventType.END) {
                    endAnnotation(annEvt.getAnnotation());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#onSpace()
     */
    @Override
    public void onSpace()
    {
        openAllAnnotations();
        super.onSpace();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onSpecialSymbol(char)
     */
    @Override
    public void onSpecialSymbol(char symbol)
    {
        EventReference currentEvt =
            new EventReference(EventType.ON_SPECIAL_SYMBOL, getAndIncrement(EventType.ON_SPECIAL_SYMBOL));
        beginAllAnnotations(currentEvt);

        openAllAnnotations();
        super.onSpecialSymbol(symbol);

        endAllAnnotations(currentEvt);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onVerbatim(java.lang.String, boolean, java.util.Map)
     */
    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        // FIXME: need to handle the dirty case when verbatim block is block, in which case adding a span around it is
        // not such a good idea
        EventReference currentEvt = new EventReference(EventType.ON_VERBATIM, getAndIncrement(EventType.ON_VERBATIM));
        beginAllAnnotations(currentEvt);

        openAllAnnotations();
        super.onVerbatim(protectedString, isInline, parameters);

        endAllAnnotations(currentEvt);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onRawText(java.lang.String, org.xwiki.rendering.syntax.Syntax)
     */
    @Override
    public void onRawText(String text, Syntax syntax)
    {
        // FIXME: this is going to be messy, messy because of the raw block syntax which can be HTML and produce very
        // invalid html.
        EventReference currentEvt = new EventReference(EventType.ON_RAW_TEXT, getAndIncrement(EventType.ON_RAW_TEXT));
        beginAllAnnotations(currentEvt);

        openAllAnnotations();
        // Store the raw text as it is ftm. Should handle syntax in the future
        super.onRawText(text, syntax);

        endAllAnnotations(currentEvt);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endLink(org.xwiki.rendering.listener.Link,
     *      boolean, java.util.Map)
     */
    @Override
    public void endLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        if (getEmptyBlockState().isCurrentContainerBlockEmpty()) {
            // special handling only if the link is empty, in which case we wrap all annotations around it
            // FIXME: This will generate a whole mess of html
            EventReference currentEvt = new EventReference(EventType.END_LINK, getAndIncrement(EventType.END_LINK));
            beginAllAnnotations(currentEvt);

            openAllAnnotations();
            super.endLink(link, isFreeStandingURI, parameters);
            closeAllAnnotations();

            endAllAnnotations(currentEvt);
        } else {
            // otherwise, if the link has content and it was rendered as words, handle the end and then call the super
            closeAllAnnotations();
            super.endLink(link, isFreeStandingURI, parameters);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endDefinitionDescription()
     */
    @Override
    public void endDefinitionDescription()
    {
        closeAllAnnotations();
        super.endDefinitionDescription();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endDefinitionList(java.util.Map)
     */
    @Override
    public void endDefinitionList(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endDefinitionList(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endDefinitionTerm()
     */
    @Override
    public void endDefinitionTerm()
    {
        closeAllAnnotations();
        super.endDefinitionTerm();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endDocument(java.util.Map)
     */
    @Override
    public void endDocument(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endDocument(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endFormat(org.xwiki.rendering.listener.Format,
     *      java.util.Map)
     */
    @Override
    public void endFormat(Format format, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endFormat(format, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endGroup(java.util.Map)
     */
    @Override
    public void endGroup(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endGroup(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endHeader(org.xwiki.rendering.listener.HeaderLevel,
     *      java.lang.String, java.util.Map)
     */
    @Override
    public void endHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endHeader(level, id, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endList(org.xwiki.rendering.listener.ListType,
     *      java.util.Map)
     */
    @Override
    public void endList(ListType listType, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endList(listType, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endListItem()
     */
    @Override
    public void endListItem()
    {
        closeAllAnnotations();
        super.endListItem();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endParagraph(java.util.Map)
     */
    @Override
    public void endParagraph(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endParagraph(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endQuotation(java.util.Map)
     */
    @Override
    public void endQuotation(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endQuotation(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endQuotationLine()
     */
    @Override
    public void endQuotationLine()
    {
        closeAllAnnotations();
        super.endQuotationLine();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endSection(java.util.Map)
     */
    @Override
    public void endSection(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endSection(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endTable(java.util.Map)
     */
    @Override
    public void endTable(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endTable(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endTableCell(java.util.Map)
     */
    @Override
    public void endTableCell(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endTableCell(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endTableHeadCell(java.util.Map)
     */
    @Override
    public void endTableHeadCell(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endTableHeadCell(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#endTableRow(java.util.Map)
     */
    @Override
    public void endTableRow(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.endTableRow(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginDefinitionDescription()
     */
    @Override
    public void beginDefinitionDescription()
    {
        closeAllAnnotations();
        super.beginDefinitionDescription();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginDefinitionList(java.util.Map)
     */
    @Override
    public void beginDefinitionList(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginDefinitionList(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginDefinitionTerm()
     */
    @Override
    public void beginDefinitionTerm()
    {
        closeAllAnnotations();
        super.beginDefinitionTerm();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginFormat(org.xwiki.rendering.listener.Format,
     *      java.util.Map)
     */
    @Override
    public void beginFormat(Format format, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginFormat(format, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginGroup(java.util.Map)
     */
    @Override
    public void beginGroup(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginGroup(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginHeader(org.xwiki.rendering.listener.HeaderLevel,
     *      java.lang.String, java.util.Map)
     */
    @Override
    public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginHeader(level, id, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginLink(org.xwiki.rendering.listener.Link,
     *      boolean, java.util.Map)
     */
    @Override
    public void beginLink(Link link, boolean isFreeStandingURI, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginLink(link, isFreeStandingURI, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginList(org.xwiki.rendering.listener.ListType,
     *      java.util.Map)
     */
    @Override
    public void beginList(ListType listType, Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginList(listType, parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginListItem()
     */
    @Override
    public void beginListItem()
    {
        closeAllAnnotations();
        super.beginListItem();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginParagraph(java.util.Map)
     */
    @Override
    public void beginParagraph(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginParagraph(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginQuotation(java.util.Map)
     */
    @Override
    public void beginQuotation(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginQuotation(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginQuotationLine()
     */
    @Override
    public void beginQuotationLine()
    {
        closeAllAnnotations();
        super.beginQuotationLine();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#beginSection(java.util.Map)
     */
    @Override
    public void beginSection(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginSection(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginTable(java.util.Map)
     */
    @Override
    public void beginTable(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginTable(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginTableCell(java.util.Map)
     */
    @Override
    public void beginTableCell(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginTableCell(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginTableHeadCell(java.util.Map)
     */
    @Override
    public void beginTableHeadCell(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginTableHeadCell(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer#beginTableRow(java.util.Map)
     */
    @Override
    public void beginTableRow(Map<String, String> parameters)
    {
        closeAllAnnotations();
        super.beginTableRow(parameters);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.renderer.AnnotationRenderer
     *      #setAnnotationsBookmarks(org.xwiki.annotation.renderer.AnnotationBookmarks)
     */
    public void setAnnotationsBookmarks(AnnotationBookmarks bookmarks)
    {
        this.bookmarks = bookmarks;
    }

    /**
     * Helper function to get the current event count of the specified type, and increment it. Similar to a ++ operation
     * on the Integer mapped to the passed event type.
     * 
     * @param type the event type
     * @return the current event count for the passed type.
     */
    protected int getAndIncrement(EventType type)
    {
        Integer currentCount = eventsCount.get(type);
        if (currentCount == null) {
            currentCount = 0;
        }
        eventsCount.put(type, currentCount + 1);
        return currentCount;
    }
}
