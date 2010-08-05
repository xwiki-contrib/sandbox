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
package org.xwiki.wikiimporter.internal.mediawiki;

import java.io.StringReader;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.wikiimporter.listener.WikiImporterListener;

/**
 * Call back events for MediaWiki XML parser ( SAX Parser ).
 * 
 * @version $Id$
 */
public class MediaWikiXmlHandler extends DefaultHandler
{
    private WikiImporterListener listener;

    private Stack<String> currElement = new Stack<String>();

    private StringBuilder strBuf = new StringBuilder();

    protected PrintRendererFactory plainRendererFactory;

    private StreamParser mediawikiParser;

    public MediaWikiXmlHandler(ComponentManager componentManager, WikiImporterListener listener)
        throws ComponentLookupException
    {
        this.mediawikiParser = componentManager.lookup(StreamParser.class, "mediawiki/1.0");

        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        this.strBuf.append(ch, start, length);
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
        if (MediaWikiConstants.PAGE_TAG.equals(qName)) {
            this.listener.beginWikiPage();
        } else if (MediaWikiConstants.PAGE_REVISION_TAG.equals(qName)) {
            this.listener.beginWikiPageRevision();
        }

        // Set Current Element
        this.currElement.push(qName);
        this.strBuf.setLength(0);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (MediaWikiConstants.MW_PROPERTIES.contains(currElement.peek())) {
            this.listener.onProperty(currElement.pop(), strBuf.toString());
        }

        if (MediaWikiConstants.TEXT_CONTENT_TAG.equals(currElement.peek())) {
            try {
                this.parseText(strBuf);
            } catch (Exception e) {
                // Do Nothing.
            }
            this.currElement.pop();
        }

        if (MediaWikiConstants.PAGE_TAG.equals(qName)) {
            this.listener.endWikiPage();
        } else if (MediaWikiConstants.PAGE_REVISION_TAG.equals(qName)) {
            this.listener.endWikiPageRevision();
        }
    }

    private void parseText(StringBuilder str) throws MediaWikiImporterException
    {
        try {
            this.mediawikiParser.parse(new StringReader(str.toString()), listener);
        } catch (Exception e) {
            throw new MediaWikiImporterException("Unable to parse page content", e);
        }
    }
}
