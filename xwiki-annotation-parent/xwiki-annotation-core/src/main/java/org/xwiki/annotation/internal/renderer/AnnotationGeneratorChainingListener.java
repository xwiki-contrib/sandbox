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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.content.AlteredContent;
import org.xwiki.annotation.content.ContentAlterer;
import org.xwiki.annotation.renderer.AnnotationListener;
import org.xwiki.rendering.internal.renderer.BasicLinkRenderer;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.QueueListener;
import org.xwiki.rendering.listener.chaining.ChainingListener;
import org.xwiki.rendering.listener.chaining.EmptyBlockChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.LinkLabelGenerator;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Generates annotation events (as per the {@link AnnotationListener} specification) in a chain, to the next listener in
 * the chain. <br />
 * It operates by buffering all events, creating the plain text representation of the listened content, mapping the
 * annotations on this content and identifying the events in the stream that hold the start and end of the annotations.
 * On sending the buffered events, the events containing annotations will be split and annotation events will be sent
 * for them.
 * 
 * @version $Id$
 */
public class AnnotationGeneratorChainingListener extends QueueListener implements ChainingListener
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
    private SortedMap<Integer, Event> eventsMapping = new TreeMap<Integer, Event>();

    /**
     * The collection of annotations to generate annotation events for, by default the empty list.
     */
    private Collection<Annotation> annotations = Collections.<Annotation> emptyList();

    /**
     * Cleaner for the annotation selection text, so that it can be mapped on the content.
     */
    private ContentAlterer selectionAlterer;

    /**
     * Map holding the collection of annotations starting in the specified event. Used to send annotation events in the
     * events stream when consuming queued events.
     */
    private Map<Event, Collection<Annotation>> startEvents = new HashMap<Event, Collection<Annotation>>();

    /**
     * Map holding the collection of annotations ending in the specified event. Used to send annotation events in the
     * events stream when consuming queued events.
     */
    private Map<Event, Collection<Annotation>> endEvents = new HashMap<Event, Collection<Annotation>>();

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
        eventsMapping.put(plainTextContent.length() - 1, getLast());
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
        eventsMapping.put(plainTextContent.length() - 1, getLast());
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
        String cleanedProtectedString = selectionAlterer.alter(protectedString).getContent().toString();
        plainTextContent.append(cleanedProtectedString);
        eventsMapping.put(plainTextContent.length() - 1, getLast());
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
        plainTextContent.append(text);
        eventsMapping.put(plainTextContent.length() - 1, getLast());
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
            // TODO: should clean spaces out of the linkPlainText (but then the event offset inside this event wouldn't
            // work anymore)
            plainTextContent.append(linkPlainText);
            // TODO: maybe should store the begin link event...
            eventsMapping.put(plainTextContent.length() - 1, getLast());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument(Map<String, String> parameters)
    {
        super.endDocument(parameters);

        mapAnnotations();

        // now get the next listener in the chain and consume all events to it
        ChainingListener renderer = chain.getNextListener(getClass());

        // consumeEvents should check if the listener is annotation listener or not and send annotation events
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
                // TODO: should also store the offsets of the start and end annotation in the event
                Event startEvt = getEventForIndex(annotationIndex).getValue();
                Event endEvt = getEventForIndex(annotationIndex + alteredSelection.length() - 1).getValue();
                if (startEvt != null & endEvt != null) {
                    // store them in the maps
                    addEventMapping(startEvt, ann, startEvents);
                    addEventMapping(endEvt, ann, endEvents);
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
     * Adds a mapping for the passed event and annotation in the specified map.
     * 
     * @param event the event to add mapping for
     * @param annotation the annotation to add mapping for
     * @param map the map in which to add mapping
     */
    private void addEventMapping(Event event, Annotation annotation, Map<Event, Collection<Annotation>> map)
    {
        Collection<Annotation> mappedCollection = map.get(event);
        if (mappedCollection == null) {
            mappedCollection = new ArrayList<Annotation>();
            map.put(event, mappedCollection);
        }
        mappedCollection.add(annotation);
    }

    /**
     * Finds and returns the event in whose range (generated text in the plainTextRepresentation) the passed index is
     * falling.
     * 
     * @param index the index to find event for
     * @return the pair of startIndex, Event in whose range index falls
     */
    private Map.Entry<Integer, Event> getEventForIndex(int index)
    {
        // iterate through all the mappings, for start index and event
        Iterator<Map.Entry<Integer, Event>> rangeIt = eventsMapping.entrySet().iterator();
        Map.Entry<Integer, Event> range = rangeIt.next();
        while (rangeIt.hasNext() && range.getKey() < index) {
            // while there is a next event and the index to find is after behind the end position, try the next range
            range = rangeIt.next();
        }
        return range;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.listener.QueueListener#consumeEvents(org.xwiki.rendering.listener.Listener)
     */
    @Override
    public void consumeEvents(Listener listener)
    {
        if (listener instanceof AnnotationListener) {
            consumeAnnotationEvents((AnnotationListener) listener);
        } else {
            super.consumeEvents(listener);
        }
    }

    /**
     * Helper function to consume the events queued in this listener with annotation awareness, i.e. send annotation
     * events to the passed listener.
     * 
     * @param listener the annotation listener to send the annotation events to
     */
    private void consumeAnnotationEvents(AnnotationListener listener)
    {
        // FIXME: take annotation offsets into account and split events
        while (!isEmpty()) {
            Event event = remove();
            // annotations starting before
            if (startEvents.get(event) != null) {
                for (Annotation startAnn : startEvents.get(event)) {
                    listener.beginAnnotation(startAnn);
                }
            }
            event.eventType.fireEvent(listener, event.eventParameters);
            // annotations ending after
            if (endEvents.get(event) != null) {
                for (Annotation endAnn : endEvents.get(event)) {
                    listener.endAnnotation(endAnn);
                }
            }
        }
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
}
