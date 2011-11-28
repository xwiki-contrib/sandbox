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
package org.xwiki.portlet.view;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xwiki.portlet.url.URLRewriter;

/**
 * An XML filter that transforms the code (e.g. CSS, JavaScript) in-lined in the HTML document.
 * 
 * @version $Id$
 */
public class HTMLInlineCodeXMLFilter extends XMLFilterImpl
{
    /**
     * The list of HTML event attributes. They take JavaScript code as value.
     */
    private static final List<String> EVENT_ATTRIBUTES = Arrays.asList("onclick", "ondblclick", "onmousedown",
        "onmouseup", "onmouseover", "onmousemove", "onmouseout", "onkeypress", "onkeydown", "onkeyup", "onfocus",
        "onblur", "onload", "onunload", "onsubmit", "onreset", "onselect", "onchange");

    /**
     * The name of the HTML {@code <script>} tag.
     */
    private static final String SCRIPT = "script";

    /**
     * The current stream filter. It depends on the current element.
     */
    private StreamFilter currentFilter;

    /**
     * The text to be filtered, i.e. the text of the current element if the current element has an associated filter.
     */
    private final StringBuilder filterInput = new StringBuilder();

    /**
     * The mapping between tag names and corresponding filters.
     */
    private final Map<String, StreamFilter> filters = new HashMap<String, StreamFilter>();

    /**
     * Creates a new HTML XML filter that rewrites the CSS and JavaScript code in-lined in the HTML document.
     * 
     * @param urlRewriter the object used to rewrite URLs
     * @param namespace the string used to name-space HTML element identifiers in the context of the portal page
     */
    public HTMLInlineCodeXMLFilter(URLRewriter urlRewriter, String namespace)
    {
        filters.put("style", new CSSStreamFilter(namespace, urlRewriter));
        filters.put(SCRIPT, new JavaScriptStreamFilter(namespace));
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        currentFilter = filters.get(qName);
        super.startElement(uri, localName, qName, rewriteEventAttributes(atts));
    }

    /**
     * Rewrites the JavaScript code in-lined in HTML event attributes.
     * 
     * @param atts some element attributes
     * @return the given attributes, where the value of the event attributes have been rewritten
     */
    private Attributes rewriteEventAttributes(Attributes atts)
    {
        AttributesImpl newAtts = null;
        for (int i = 0; i < atts.getLength(); i++) {
            if (EVENT_ATTRIBUTES.contains(atts.getQName(i))) {
                if (newAtts == null) {
                    newAtts = atts instanceof AttributesImpl ? (AttributesImpl) atts : new AttributesImpl(atts);
                }
                // Add the function wrapper to prevent exceptions like "invalid return".
                String script = String.format("function (event) {\n%s}", atts.getValue(i));
                StringReader reader = new StringReader(script);
                StringWriter writer = new StringWriter();
                filters.get(SCRIPT).filter(reader, writer);
                script = writer.toString();
                // Remove the function wrapper.
                newAtts.setValue(i, script.substring(20, script.length() - 2));
            }
        }
        return newAtts != null ? newAtts : atts;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (currentFilter != null) {
            String output = filterInput.toString();
            filterInput.delete(0, filterInput.length());

            String input = output.trim();
            if (input.length() > 0) {
                StringWriter writer = new StringWriter();
                currentFilter.filter(new StringReader(input), writer);
                output = writer.toString();
            }

            currentFilter = null;
            super.characters(output.toCharArray(), 0, output.length());
        }
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (length > 0 && currentFilter != null) {
            filterInput.append(ch, start, length);
        } else {
            super.characters(ch, start, length);
        }
    }
}
