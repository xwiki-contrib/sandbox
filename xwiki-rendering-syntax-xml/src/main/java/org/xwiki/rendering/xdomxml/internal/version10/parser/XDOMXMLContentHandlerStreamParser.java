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
package org.xwiki.rendering.xdomxml.internal.version10.parser;

import org.dom4j.io.SAXContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.xml.ContentHandlerStreamParser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml.internal.parser.BlockParser;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

/**
 * @version $Id: XDOMXMLContentHandlerStreamParser.java 29769 2010-06-27 11:01:42Z tmortagne $
 */
@Component("xdom+xml/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XDOMXMLContentHandlerStreamParser extends DefaultHandler implements ContentHandlerStreamParser,
    XDOMXMLConstants
{
    private Listener listener;

    private BlockParser documentParser;

    @Requirement
    private ComponentManager componentManager;

    /**
     * Avoid create a new SAXContentHandler for each block when the same can be used for all.
     */
    public SAXContentHandler currentDOMBuilder = new SAXContentHandler();

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.parser.xml.ContentHandlerStreamParser#getSyntax()
     */
    public Syntax getSyntax()
    {
        return XDOMXML_1_0;
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.documentParser != null) {
            this.documentParser.startElement(uri, localName, qName, attributes);
        } else if (ELEM_BLOCK.equals(qName)) {
            this.documentParser = new DefaultBlockParser(this.listener, this.componentManager);
            this.documentParser.setVersion(XDOMXML_1_0.getVersion());
            this.documentParser.startElement(uri, localName, qName, attributes);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.documentParser != null) {
            this.documentParser.characters(ch, start, length);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (this.documentParser != null) {
            this.documentParser.endElement(uri, localName, qName);
        }
    }
}
