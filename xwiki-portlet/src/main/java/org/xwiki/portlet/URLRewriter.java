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
package org.xwiki.portlet;

import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Rewrites servlet URLs into portlet URLs.
 * 
 * @version $Id$
 */
public class URLRewriter
{
    /**
     * The logger instance.
     */
    private static final Log LOG = LogFactory.getLog(URLRewriter.class);

    /**
     * The XML filter used to rewrite the servlet URLs into portlet URLs.
     */
    private final URLRewriterXMLFilter filter;

    /**
     * Creates a new URL rewriter which converts relative servlet URLs prefixed with the given context path to portlet
     * URLs created using the given URL factory.
     * 
     * @param dispatchURLFactory the object used to create portlet URLs
     * @param contextPath the servlet context path
     */
    public URLRewriter(DispatchURLFactory dispatchURLFactory, String contextPath)
    {
        filter = new URLRewriterXMLFilter(dispatchURLFactory, contextPath);
    }

    /**
     * Rewrites the servlet URLs from the given HTML reader and writes the result using the given writer.
     * 
     * @param reader where to read the HTML from
     * @param writer where to write the modified HTML to
     */
    public void rewrite(Reader reader, Writer writer)
    {
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
            xmlReader.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
            xmlReader.setProperty("http://cyberneko.org/html/properties/names/elems", "match");
            xmlReader.setProperty("http://cyberneko.org/html/properties/names/attrs", "no-change");

            filter.setParent(xmlReader);

            XHTMLWriter xhtmlWriter = new XHTMLWriter(writer);
            xhtmlWriter.setParent(filter);
            xhtmlWriter.parse(new InputSource(reader));
        } catch (Exception e) {
            LOG.error("Failed to rewrite the URLs.", e);
        }
    }
}
