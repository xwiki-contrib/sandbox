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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.context.SourceImpl;

/**
 * Stores data and provides functions for manipulating mock documents loaded from test files. Use the
 * {@link TestDocumentFactory} to load such documents from files.
 * 
 * @see TestDocumentFactory
 * @version $Id$
 */
public class MockDocument
{
    private Map<String, Object> properties = new HashMap<String, Object>();

    public String getTextSource()
    {
        return (String) properties.get("source");
    }

    public void set(String key, Object value)
    {
        properties.put(key, value);
    }

    public String getRenderedContent()
    {
        // if not otherwise specified, the source is also the rendered content
        String renderedContent = (String) properties.get("rendered");
        return renderedContent != null ? renderedContent : getTextSource();
    }

    public String getSourceWithMarkers()
    {
        String sourceWithMarkers = (String) properties.get("sourceWithMarkers");
        return sourceWithMarkers != null ? sourceWithMarkers : getTextSource();
    }

    public String getAnnotatedContent()
    {
        String annotatedContent = (String) properties.get("annotated");
        return annotatedContent != null ? annotatedContent : getRenderedContent();
    }

    public String getRenderedContentWithMarkers()
    {
        String renderedWithMarkers = (String) properties.get("renderedWithMarkers");
        return renderedWithMarkers != null ? renderedWithMarkers : getRenderedContent();
    }

    public List<Annotation> getSafeAnnotations()
    {
        return (List<Annotation>) properties.get("annotations");
    }

    public Source getSource() throws IOException
    {
        return new SourceImpl(getTextSource());
    }
}
