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

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xwiki.portlet.url.URLRewriter;
import org.xwiki.portlet.util.XHTMLWriter;

/**
 * Rewrites a servlet HTML stream into a portlet HTML stream.
 * 
 * @version $Id$
 */
public class HTMLStreamFilter implements StreamFilter
{
    /**
     * The logger instance.
     */
    private static final Log LOG = LogFactory.getLog(HTMLStreamFilter.class);

    /**
     * The list of XML filters that are applied to the HTML stream.
     */
    private final List<XMLFilter> filters = new ArrayList<XMLFilter>();

    /**
     * Creates a new HTML stream filter which executes the following transformations:
     * <ul>
     * <li>converts relative servlet URLs prefixed with the given context path to portlet URLs created using the given
     * URL factory</li>
     * <li>name-space HTML element identifiers to prevent conflicts with other portlets and the portal page itself</li>
     * <li>rewrite in-line JavaScript code to use the name-spaced element identifiers</li>
     * <li>rewrite in-line CSS to prevent interference with other portlets and the portal page itself</li>
     * </ul>
     * .
     * 
     * @param urlRewriter the object used to rewrite servlet URLs into portlet URLs
     * @param portletNamespace the string used to name-space HTML element identifiers in the context of the portal page
     * @param wrapOutput {@code true} to wrap the output in a container that has the {@code namespace} identifier,
     *            {@code false} otherwise
     */
    public HTMLStreamFilter(URLRewriter urlRewriter, String portletNamespace, boolean wrapOutput)
    {
        filters.add(new HTMLConditionalCommentsXMLFilter(urlRewriter));
        filters.add(new HTMLIdAttributeXMLFilter(portletNamespace, wrapOutput));
        filters.add(new HTMLInlineCodeXMLFilter(urlRewriter, portletNamespace));
        filters.add(new HTMLURLAttributeXMLFilter(urlRewriter, portletNamespace));
    }

    /**
     * {@inheritDoc}
     * 
     * @see StreamFilter#filter(Reader, Writer)
     */
    public void filter(Reader reader, Writer writer)
    {
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
            xmlReader.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
            xmlReader.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
            xmlReader.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");

            XMLReader parent = xmlReader;
            for (XMLFilter filter : filters) {
                filter.setParent(parent);
                parent = filter;
            }

            XHTMLWriter xhtmlWriter = new XHTMLWriter(writer);
            xhtmlWriter.setParent(parent);
            xhtmlWriter.parse(new InputSource(reader));
        } catch (Exception e) {
            LOG.error("Failed to rewrite servlet HTML.", e);
        }
    }
}
