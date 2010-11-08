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
package org.xwiki.portlet.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A specialized {@link XMLWriter} for XHTML serialization.
 * 
 * @version $Id$
 */
public class XHTMLWriter extends XMLWriter
{
    /**
     * The default output format used for serializing XHTML.
     */
    private static final OutputFormat DEFAULT_XHTML_FORMAT;

    /**
     * The list of all HTML elements that must be empty. All of them appear as {@code <tagName/>} in the HTML code.
     */
    private static final List<String> HTML_EMPTY_ELEMENTS =
        Arrays.asList(new String[] {"area", "base", "basefont", "br", "col", "frame", "hr", "img", "input", "isindex",
            "link", "meta", "param", "nextid", "bgsound", "embed", "keygen", "spacer", "wbr"});

    /**
     * The list of all HTML elements whose text node children should be output unescaped (no character references).
     */
    private static final List<String> HTML_NON_ESCAPING_ELEMENTS = Arrays.asList(new String[] {"script", "style"});

    /**
     * Flag indicating if the current element is empty.
     */
    private boolean emptyElement;

    /**
     * The number of nested elements whose child text nodes shouldn't be escaped.
     */
    private int nonEscapingElementCount;

    static {
        DEFAULT_XHTML_FORMAT = new OutputFormat();
        DEFAULT_XHTML_FORMAT.setXHTML(true);
        DEFAULT_XHTML_FORMAT.setSuppressDeclaration(true);
        DEFAULT_XHTML_FORMAT.setExpandEmptyElements(true);
    }

    /**
     * Creates a new XHTML writer that outputs the serialized XHTML using the given writer.
     * 
     * @param writer where to serialize the XHTML
     * @throws UnsupportedEncodingException if the output format encoding is not supported
     */
    public XHTMLWriter(Writer writer) throws UnsupportedEncodingException
    {
        super(writer, DEFAULT_XHTML_FORMAT);

        // Escape all non US-ASCII to have as less encoding problems as possible.
        setMaximumAllowedCharacter(127);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#endDocument()
     */
    @Override
    public void endDocument() throws SAXException
    {
        finishStartElement();
        super.endDocument();
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        finishStartElement();
        // Change the writer with a fake one to prevent the closing bracket from being written.
        Writer originalWriter = writer;
        writer = new StringWriter();
        try {
            super.startElement(namespaceURI, localName, qName, attributes);
            String startTag = writer.toString();
            // Write everything except the closing bracket.
            originalWriter.write(startTag.substring(0, startTag.length() - 1));
            emptyElement = true;
            if (HTML_NON_ESCAPING_ELEMENTS.contains(qName)) {
                nonEscapingElementCount++;
            }
        } catch (IOException e) {
            handleException(e);
        } finally {
            // Restore the writer.
            writer = originalWriter;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#endElement(String, String, String)
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        super.endElement(namespaceURI, localName, qName);
        if (HTML_NON_ESCAPING_ELEMENTS.contains(qName)) {
            nonEscapingElementCount--;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        finishStartElement();
        boolean escapeText = isEscapeText();
        if (nonEscapingElementCount > 0) {
            setEscapeText(false);
        }
        try {
            super.characters(ch, start, length);
        } finally {
            setEscapeText(escapeText);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        finishStartElement();
        super.ignorableWhitespace(ch, start, length);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#processingInstruction(String, String)
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException
    {
        finishStartElement();
        super.processingInstruction(target, data);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#startEntity(String)
     */
    @Override
    public void startEntity(String name) throws SAXException
    {
        finishStartElement();
        super.startEntity(name);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#startCDATA()
     */
    @Override
    public void startCDATA() throws SAXException
    {
        finishStartElement();
        super.startCDATA();
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#comment(char[], int, int)
     */
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        finishStartElement();
        super.comment(ch, start, length);
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#writeClose(String)
     */
    @Override
    protected void writeClose(String qualifiedName) throws IOException
    {
        if (emptyElement) {
            writeEmptyElementClose(qualifiedName);
            emptyElement = false;
        } else {
            super.writeClose(qualifiedName);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLWriter#writeEmptyElementClose(String)
     */
    @Override
    protected void writeEmptyElementClose(String qualifiedName) throws IOException
    {
        boolean expandEmptyElements = getOutputFormat().isExpandEmptyElements();
        if (HTML_EMPTY_ELEMENTS.contains(qualifiedName)) {
            getOutputFormat().setExpandEmptyElements(false);
        }
        try {
            super.writeEmptyElementClose(qualifiedName);
        } finally {
            getOutputFormat().setExpandEmptyElements(expandEmptyElements);
        }
    }

    /**
     * If start element tag is still open, write closing bracket.
     * 
     * @throws SAXException if writing the closing bracket fails
     */
    private void finishStartElement() throws SAXException
    {
        try {
            if (emptyElement) {
                writer.write(">");
                emptyElement = false;
            }
        } catch (IOException e) {
            handleException(e);
        }
    }
}
