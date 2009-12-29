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

package org.xwiki.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores data and provides functions for manipulating mock documents loaded from test files. Use the
 * {@link TestDocumentFactory} to load such documents from files.
 * 
 * @see TestDocumentFactory
 * @version $Id$
 */
public class MockDocument
{
    /**
     * Properties map for this test document.
     */
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Sets the properties of this document.
     * 
     * @param key the key of the property
     * @param value the value of the property to set
     */
    public void set(String key, Object value)
    {
        properties.put(key, value);
    }

    /**
     * @return the rendered content of this document, corresponding to the source as returned by
     *         {@link #getTextSource()}
     */
    public String getRenderedContent()
    {
        // if not otherwise specified, the source is also the rendered content
        String renderedContent = (String) properties.get("rendered");
        return renderedContent != null ? renderedContent : getSource();
    }

    /**
     * @return the source of this document with the annotation markers inserted. Used for verification purposes, for
     *         identifying the calls to render content after the markers have been inserted, for which
     *         {@link #getRenderedContentWithMarkers()} should be returned.
     */
    public String getSourceWithMarkers()
    {
        String sourceWithMarkers = (String) properties.get("sourceWithMarkers");
        return sourceWithMarkers != null ? sourceWithMarkers : getSource();
    }

    /**
     * @return the annotated HTML of this document. Used for verification purposes.
     */
    public String getAnnotatedContent()
    {
        String annotatedContent = (String) properties.get("annotated");
        return annotatedContent != null ? annotatedContent : getRenderedContent();
    }

    /**
     * @return the rendered HTML of this document, with the annotation markers inserted. This should be the result of
     *         rendering the source returned by {@link #getSourceWithMarkers()}.
     */
    public String getRenderedContentWithMarkers()
    {
        String renderedWithMarkers = (String) properties.get("renderedWithMarkers");
        return renderedWithMarkers != null ? renderedWithMarkers : getRenderedContent();
    }

    /**
     * @return the annotations of this document, as specified in the corpus file. Note that no filtering on the state of
     *         the annotation is made.
     */
    @SuppressWarnings("unchecked")
    public List<Annotation> getSafeAnnotations()
    {
        return (List<Annotation>) properties.get("annotations");
    }

    /**
     * @return the source of this document, corresponding to the {@link #getTextSource()} content.
     */
    public String getSource()
    {
        return (String) properties.get("source");
    }

    /**
     * @return the syntax of this document's content
     */
    public String getSyntax()
    {
        String sourceSyntax = (String) properties.get("sourceSyntax");
        return sourceSyntax == null || sourceSyntax.length() == 0 ? "xwiki/2.0" : sourceSyntax;
    }
}
