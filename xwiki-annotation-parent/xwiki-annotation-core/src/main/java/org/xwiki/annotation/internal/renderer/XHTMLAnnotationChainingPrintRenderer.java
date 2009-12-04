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

import java.util.LinkedHashMap;
import java.util.Map;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.renderer.AnnotationChainingPrintRenderer;
import org.xwiki.rendering.internal.renderer.xhtml.XHTMLChainingRenderer;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.xhtml.XHTMLImageRenderer;
import org.xwiki.rendering.renderer.xhtml.XHTMLLinkRenderer;

/**
 * Extends the default XHTML renderer to add handling of annotations.<br />
 * FIXME: this implementation is a very simple handling of annotation events, it should handle annotation markers
 * splitting & nesting in other elements.
 * 
 * @version $Id$
 */
public class XHTMLAnnotationChainingPrintRenderer extends XHTMLChainingRenderer implements
    AnnotationChainingPrintRenderer
{
    /**
     * The annotation marker element in HTML.
     */
    private static final String ANNOTATION_MARKER = "span";

    /**
     * Constructor from super class.
     * 
     * @param linkRenderer the renderer for links
     * @param imageRenderer the renderer for images
     * @param listenerChain the listener chain in which to add this listener
     */
    public XHTMLAnnotationChainingPrintRenderer(XHTMLLinkRenderer linkRenderer, XHTMLImageRenderer imageRenderer,
        ListenerChain listenerChain)
    {
        super(linkRenderer, imageRenderer, listenerChain);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.renderer.AnnotationListener#beginAnnotation(Annotation)
     */
    public void beginAnnotation(Annotation annotation)
    {
        Map<String, String> attributes = new LinkedHashMap<String, String>();

        attributes.put("class", createAnnotationClass(annotation.getId()));
        attributes.put("title", annotation.getAnnotation().toString());
        getXHTMLWikiPrinter().printXMLStartElement(ANNOTATION_MARKER, attributes);
    }

    /**
     * Creates the class value of the annotation.
     * 
     * @param annotationId the id of the annotation
     * @return the class value for the passed annotation
     */
    private String createAnnotationClass(int annotationId)
    {
        return "annotation ID" + annotationId;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.renderer.AnnotationListener#endAnnotation(Annotation)
     */
    public void endAnnotation(Annotation annotation)
    {
        getXHTMLWikiPrinter().printXMLEndElement(ANNOTATION_MARKER);
    }
}
