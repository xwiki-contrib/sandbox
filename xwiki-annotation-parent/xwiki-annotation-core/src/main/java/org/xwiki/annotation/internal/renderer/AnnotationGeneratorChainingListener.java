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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.content.AlteredContent;
import org.xwiki.annotation.content.ContentAlterer;
import org.xwiki.annotation.renderer.AnnotationBookmarks;
import org.xwiki.annotation.renderer.AnnotationEvent;
import org.xwiki.annotation.renderer.AnnotationGeneratorListener;
import org.xwiki.annotation.renderer.EventReference;
import org.xwiki.annotation.renderer.AnnotationEvent.AnnotationEventType;
import org.xwiki.rendering.internal.renderer.BasicLinkRenderer;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.LinkLabelGenerator;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Chaining default implementation of the {@link AnnotationGeneratorListener}. It operates by buffering all events,
 * creating the plain text representation of the listened content, mapping the annotations on this content and
 * identifying the events in the stream that hold the start and end of the annotations.
 * 
 * @version $Id$
 */
public class AnnotationGeneratorChainingListener extends QueueListener implements ChainingListener,
    AnnotationGeneratorListener
{
    /**
     * Version number of this class.
     */
    private static final long serialVersionUID = -2790330640900288463L;

    /**
     * To generate a string representation of a link that we output when no link label generator exist or when the link
     * is an external link (ie not a document link).
     */
    private BasicLinkRenderer linkRenderer = new BasicLinkRenderer();

    /**
     * Generate link label.
     */
    private LinkLabelGenerator linkLabelGenerator;

    /**
     * The chain listener from which this listener is part of.
     */
    private ListenerChain chain;

    /**
     * Buffer to store the plain text version of the content to be rendered, so that annotations are mapped on it.
     */
    private StringBuffer plainTextContent = new StringBuffer();

    /**
     * Map to store the ranges in the plainTextContent and their corresponding events. The ranges will be stored by
     * their end index (inclusive) and ordered from smallest to biggest.
     */
    private SortedMap<Integer, EventReference> eventsMapping = new TreeMap<Integer, EventReference>();

    /**
     * Map to store the events whose content has been altered upon append to the plain text representation, along with
     * the altered content objects to allow translation of offsets back to the original offsets.
     */
    private Map<EventReference, AlteredContent> alteredEventsContent = new HashMap<EventReference, AlteredContent>();

    /**
     * Map to store the events count to be able to identify an event in the emitted events.
     */
    private Map<EventType, Integer> eventsCount = new HashMap<EventType, Integer>();

    /**
     * The collection of annotations to generate annotation events for, by default the empty list.
     */
    private Collection<Annotation> annotations = Collections.<Annotation> emptyList();

    /**
     * Cleaner for the annotation selection text, so that it can be mapped on the content.
     */
    private ContentAlterer selectionAlterer;

    /**
     * The list of generated annotation bookmarks by this mapper.
     */
    private AnnotationBookmarks bookmarks = new AnnotationBookmarks();

    /**
     * Builds an annotation generator listener from the passed link generator in the passed chain.
     * 
     * @param linkLabelGenerator the generator for link labels so that the annotation text can be recognized.
     * @param selectionAlterer cleaner for the annotation selection text, so that it can be mapped on the content
     * @param listenerChain the chain in which this listener is part of
     */
    public AnnotationGeneratorChainingListener(LinkLabelGenerator linkLabelGenerator, ContentAlterer selectionAlterer,
        ListenerChain listenerChain)
    {
        this.linkLabelGenerator = linkLabelGenerator;
        this.chain = listenerChain;
        this.selectionAlterer = selectionAlterer;
    }

    /**
     * Builds an annotation generator listener from the passed link generator in the passed chain. Also, this
     * constructor takes the bookmarks to fill in as a parameter so that the same object can be used to be filled by
     * this generator and read by an {@link org.xwiki.annotation.renderer.AnnotationRenderer} further in the chain.
     * Normally the bookmarks should be passed by this generator to the next listener in the chain, but we want to allow
     * the {@link org.xwiki.annotation.renderer.AnnotationRenderer} to be placed further in the chain, not immediately
     * after.
     * 
     * @param linkLabelGenerator the generator for link labels so that the annotation text can be recognized.
     * @param selectionAlterer cleaner for the annotation selection text, so that it can be mapped on the content
     * @param bookmarks the bookmarks to be filled by this generator
     * @param listenerChain the chain in which this listener is part of
     */
    public AnnotationGeneratorChainingListener(LinkLabelGenerator linkLabelGenerator, ContentAlterer selectionAlterer,
        AnnotationBookmarks bookmarks, ListenerChain listenerChain)
    {
        this(linkLabelGenerator, selectionAlterer, listenerChain);
        this.bookmarks = bookmarks;
    }

    /**
     * @return the state of the current empty block.
     */
    protected EmptyBlockChainingListener getEmptyBlockState()
    {
        return (EmptyBlockChainingListener) getListenerChain().getListener(EmptyBlockChainingListener.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onWord(java.lang.String)
     */
    @Override
    public void onWord(String word)
    {
        // queue this event
        super.onWord(word);
        // put it in the buffer
        plainTextContent.append(word);
        // store the mapping of the range to the just added event
        eventsMapping.put(plainTextContent.length() - 1, new EventReference(getLast().eventType,
            getAndIncrement(EventType.ON_WORD)));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onSpecialSymbol(char)
     */
    @Override
    public void onSpecialSymbol(char symbol)
    {
        super.onSpecialSymbol(symbol);
        plainTextContent.append("" + symbol);
        eventsMapping.put(plainTextContent.length() - 1, new EventReference(getLast().eventType,
            getAndIncrement(EventType.ON_SPECIAL_SYMBOL)));
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onVerbatim(java.lang.String, boolean, java.util.Map)
     */
    @Override
    public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters)
    {
        super.onVerbatim(protectedString, isInline, parameters);
        // normalize the protected string before adding it to the plain text version
        AlteredContent cleanedContent = selectionAlterer.alter(protectedString);
        plainTextContent.append(cleanedContent.getContent().toString());
        EventReference currentEvt = new EventReference(getLast().eventType, getAndIncrement(EventType.ON_VERBATIM));
        eventsMapping.put(plainTextContent.length() - 1, currentEvt);
        // also store this event in the list of events with altered content
        alteredEventsContent.put(currentEvt, cleanedContent);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#onRawText(java.lang.String, org.xwiki.rendering.syntax.Syntax)
     */
    @Override
    public void onRawText(String text, Syntax syntax)
    {
        // Store the raw text as it is ftm. Should handle syntax in the future
        super.onRawText(text, syntax);
        // Similar approach to verbatim FTM. In the future, syntax specific cleaner could be used for various syntaxes
        // (which would do the great job for HTML, for example)
        // normalize the protected string before adding it to the plain text version
        AlteredContent cleanedContent = selectionAlterer.alter(text);
        plainTextContent.append(cleanedContent.getContent().toString());
        EventReference currentEvt = new EventReference(getLast().eventType, getAndIncrement(EventType.ON_RAW_TEXT));
        eventsMapping.put(plainTextContent.length() - 1, currentEvt);
        // also store this event in the list of events with altered content
        alteredEventsContent.put(currentEvt, cleanedContent);
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
        super.endLink(link, isFreeStandingURI, parameters);
        // special handling only if the link is empty
        if (getEmptyBlockState().isCurrentContainerBlockEmpty()) {
            String linkPlainText = "";
            if (link.getType() == LinkType.DOCUMENT && this.linkLabelGenerator != null) {
                linkPlainText = this.linkLabelGenerator.generate(link);
            } else {
                linkPlainText = this.linkRenderer.renderLinkReference(link);
            }

            // normalize the protected string before adding it to the plain text version
            AlteredContent cleanedContent = selectionAlterer.alter(linkPlainText);
            plainTextContent.append(cleanedContent.getContent().toString());
            EventReference currentEvt = new EventReference(getLast().eventType, getAndIncrement(EventType.END_LINK));
            eventsMapping.put(plainTextContent.length() - 1, currentEvt);
            // also store this event in the list of events with altered content
            alteredEventsContent.put(currentEvt, cleanedContent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument(Map<String, String> parameters)
    {
        super.endDocument(parameters);

        // create the bookmarks
        mapAnnotations();

        // now get the next listener in the chain and consume all events to it
        ChainingListener renderer = chain.getNextListener(getClass());

        // send the events forward to the next annotation listener
        consumeEvents(renderer);
    }

    /**
     * Helper method to map the annotations on the plainTextContent and identify the events where annotations start and
     * end.
     */
    private void mapAnnotations()
    {
        for (Annotation ann : annotations) {
            // clean it up and its context
            AlteredContent cleanedContext = selectionAlterer.alter(ann.getSelectionContext());
            // find it in the plaintextContent
            int contextIndex = plainTextContent.indexOf(cleanedContext.getContent().toString());
            // find the selection inside the context in the plainTextContent
            // assume at this point that the selection appears only once in the context
            String alteredSelection = selectionAlterer.alter(ann.getInitialSelection()).getContent().toString();
            int selectionIndexInContext = cleanedContext.getContent().toString().indexOf(alteredSelection);
            // check that the context is in the plainText representation and selection was found inside it
            if (contextIndex >= 0 && selectionIndexInContext >= 0) {
                // compute annotation index in the plain text repr
                int annotationIndex = contextIndex + selectionIndexInContext;
                // get the start and end events for the annotation
                Map.Entry<Integer, EventReference> startEvt = getEventForIndex(annotationIndex);
                Map.Entry<Integer, EventReference> endEvt =
                    getEventForIndex(annotationIndex + alteredSelection.length() - 1);
                if (startEvt != null & endEvt != null) {
                    // compute the start event offset
                    int startOffset = getOffset(startEvt.getValue(), startEvt.getKey(), annotationIndex);
                    int endOffset =
                        getOffset(endEvt.getValue(), endEvt.getKey(), annotationIndex + alteredSelection.length() - 1);
                    // store the bookmarks
                    bookmarks.addBookmark(startEvt.getValue(), new AnnotationEvent(AnnotationEventType.START, ann),
                        startOffset);
                    bookmarks.addBookmark(endEvt.getValue(), new AnnotationEvent(AnnotationEventType.END, ann),
                        endOffset);
                } else {
                    // cannot find the events for the start and / or end of annotation, ignore it
                    // TODO: mark it somehow...
                    continue;
                }
            } else {
                // cannot find the context of the annotation or the annotation selection cannot be found in the
                // annotation context, ignore it
                // TODO: mark it somehow...
                continue;
            }
        }
    }

    /**
     * Finds and returns the event in whose range (generated text in the plainTextRepresentation) the passed index is
     * falling.
     * 
     * @param index the index to find event for
     * @return the pair of startIndex, Event in whose range index falls
     */
    private Map.Entry<Integer, EventReference> getEventForIndex(int index)
    {
        // iterate through all the mappings, for start index and event
        Iterator<Map.Entry<Integer, EventReference>> rangeIt = eventsMapping.entrySet().iterator();
        Map.Entry<Integer, EventReference> range = rangeIt.next();
        while (rangeIt.hasNext() && range.getKey() < index) {
            // while there is a next event and the index to find is after behind the end position, try the next range
            range = rangeIt.next();
        }
        return range;
    }

    /**
     * Returns the offset of the index inside the passed rendering event, ending at the specified index in the plain
     * text representation.
     * 
     * @param evt the rendering event in which the index needs to be mapped
     * @param eventEndIndex the end index of the event
     * @param index the index to map to an offset inside the passed event
     * @return the offset of the index with respect to the event {@code evt}
     */
    private int getOffset(EventReference evt, int eventEndIndex, int index)
    {
        // get the previous end index in the events mapping map
        // FIXME: this is an issue, creating so many submaps that big might be problematic
        int previousIndex = -1;
        try {
            previousIndex = eventsMapping.subMap(0, index).lastKey();
        } catch (NoSuchElementException e) {
            // nothing, stay -1
        }
        // compute the difference previousIndex + 1 is the start index of the current event
        int offset = index - (previousIndex + 1);
        // check if the content of this event is altered
        AlteredContent alteredContent = alteredEventsContent.get(evt);
        if (alteredContent != null) {
            // get the original offset of the computed offset
            offset = alteredContent.getInitialOffset(offset);
        }
        return offset;
    }

    /**
     * Helper function to get the current event count of the specified type, and increment it. Similar to a ++ operation
     * on the Integer mapped to the passed event type.
     * 
     * @param type the event type
     * @return the current event count for the passed type.
     */
    private int getAndIncrement(EventType type)
    {
        Integer currentCount = eventsCount.get(type);
        if (currentCount == null) {
            currentCount = 0;
        }
        eventsCount.put(type, currentCount + 1);
        return currentCount;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.chaining.ChainingListener#getListenerChain()
     */
    public ListenerChain getListenerChain()
    {
        return chain;
    }

    /**
     * Sets the collections of annotations to identify on the listened content and send notifications for.
     * 
     * @param annotations the collection of annotations to generate events for
     */
    public void setAnnotations(Collection<Annotation> annotations)
    {
        this.annotations = annotations;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.renderer.AnnotationGeneratorListener#getAnnotationBookmarks()
     */
    public AnnotationBookmarks getAnnotationBookmarks()
    {
        return bookmarks;
    }
}
