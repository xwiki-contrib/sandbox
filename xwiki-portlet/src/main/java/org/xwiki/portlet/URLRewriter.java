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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Rewrites servlet URLs into portlet URLs.
 * 
 * @version $Id$
 */
public class URLRewriter extends XMLFilterImpl
{
    /**
     * The logger instance.
     */
    private static final Log LOG = LogFactory.getLog(URLRewriter.class);

    /**
     * The name of the anchor attribute holding the URL.
     */
    private static final String HREF = "href";

    /**
     * The name of the script tag.
     */
    private static final String SCRIPT = "script";

    /**
     * The object used to create portlet URLs.
     */
    private final DispatchURLFactory dispatchURLFactory;

    /**
     * The servlet context path. Relative links should be prefixed with this string.
     */
    private final String contextPath;

    /**
     * Flag indicating if we are inside a script tag.
     */
    private boolean inScript;

    /**
     * Creates a new URL rewriter which converts relative servlet URLs prefixed with the given context path to portlet
     * URLs created using the given URL factory.
     * 
     * @param dispatchURLFactory the object used to create portlet URLs
     * @param contextPath the servlet context path
     */
    public URLRewriter(DispatchURLFactory dispatchURLFactory, String contextPath)
    {
        this.dispatchURLFactory = dispatchURLFactory;
        this.contextPath = contextPath;
    }

    /**
     * Rewrites the servlet URLs from the given HTML input stream and writes the result in the given output stream.
     * 
     * @param in the input stream, whose URLs will be rewritten
     * @param out the output stream, where the new URLs will be written
     */
    public void rewrite(InputStream in, OutputStream out)
    {
        try {
            setParent(createXMLReader());
            setContentHandler(new XMLSerializer(out, new OutputFormat()));
            parse(new InputSource(in));
        } catch (Exception e) {
            LOG.error("Failed to rewrite the URLs from input stream.", e);
        }
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
            setParent(createXMLReader());
            setContentHandler(new XMLSerializer(writer, new OutputFormat()));
            parse(new InputSource(reader));
        } catch (Exception e) {
            LOG.error("Failed to rewrite the URLs from reader.", e);
        }
    }

    /**
     * @return a new XML reader
     * @throws SAXException if creating the XML reader fails
     */
    private XMLReader createXMLReader() throws SAXException
    {
        XMLReader reader = XMLReaderFactory.createXMLReader("org.cyberneko.html.parsers.SAXParser");
        reader.setFeature("http://cyberneko.org/html/features/balance-tags", true);
        return reader;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if ("a".equalsIgnoreCase(qName)) {
            String href = atts.getValue(HREF);
            if (href != null && href.startsWith(contextPath)) {
                href = dispatchURLFactory.createURL(href.substring(contextPath.length())).toString();
                AttributesImpl attsImpl = new AttributesImpl(atts);
                attsImpl.setValue(atts.getIndex(HREF), href);
                super.startElement(uri, localName, qName, attsImpl);
                return;
            }
        } else if ("form".equalsIgnoreCase(qName)) {
            String action = atts.getValue("action");
            // TODO: Rewrite form action.
        } else if (SCRIPT.equalsIgnoreCase(qName) && atts.getValue("src") != null) {
            inScript = true;
        }
        super.startElement(uri, localName, qName, atts);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#endElement(String, String, String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (SCRIPT.equalsIgnoreCase(qName)) {
            inScript = false;
        }
        super.endElement(uri, localName, qName);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (inScript && length > 0) {
            // TODO: Rewrite script URLs.
        }
        super.characters(ch, start, length);
    }
}
