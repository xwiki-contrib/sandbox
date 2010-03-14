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

import static org.xwiki.wikiimporter.internal.mediawiki.MediaWikiConstants.MW_PROPERTIES;
import static org.xwiki.wikiimporter.internal.mediawiki.MediaWikiConstants.PAGE_REVISION_TAG;
import static org.xwiki.wikiimporter.internal.mediawiki.MediaWikiConstants.PAGE_TAG;
import static org.xwiki.wikiimporter.internal.mediawiki.MediaWikiConstants.TEXT_CONTENT_TAG;

import java.io.StringReader;
import java.util.Stack;

import org.wikimodel.wem.WikiParserException;
import org.wikimodel.wem.mediawiki.MediaWikiParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.internal.parser.wikimodel.XWikiGeneratorListener;
import org.xwiki.rendering.parser.ImageParser;
import org.xwiki.rendering.parser.LinkParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.renderer.PrintRendererFactory;
import org.xwiki.rendering.util.IdGenerator;
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

    private StreamParser streamParser;

    private LinkParser linkParser;

    private ImageParser imageParser;

    private IdGenerator idGenerator;

    protected PrintRendererFactory plainRendererFactory;

    private ComponentManager componentManager;

    public MediaWikiXmlHandler(WikiImporterListener listener)
    {
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
        strBuf.append(ch, start, length);
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

        if (PAGE_TAG.equals(qName)) {
            listener.beginWikiPage();
        } else if (PAGE_REVISION_TAG.equals(qName)) {
            listener.beginWikiPageRevision();
        }

        // Set Current Element
        currElement.push(qName);
        strBuf.setLength(0);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {

        if (MW_PROPERTIES.contains(currElement.peek())) {
            listener.onProperty(currElement.pop(), strBuf.toString());
        }

        if (TEXT_CONTENT_TAG.equals(currElement.peek())) {
            try {
                this.parseText(strBuf);
            } catch (Exception e) {
                // Do Nothing.
            }
            currElement.pop();
        }

        if (PAGE_TAG.equals(qName)) {
            listener.endWikiPage();
        } else if (PAGE_REVISION_TAG.equals(qName)) {
            listener.endWikiPageRevision();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException
    {
        super.error(e);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(SAXParseException arg0) throws SAXException
    {
        super.fatalError(arg0);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException
    {
        // TODO Auto-generated method stub
        super.ignorableWhitespace(arg0, arg1, arg2);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException
    {
        super.endDocument();
    }

    private void parseText(StringBuilder str) throws MediaWikiImporterException
    {

        MediaWikiParser wemMediaWikiParser = new MediaWikiParser();
        idGenerator = new IdGenerator();
        try {
            streamParser = componentManager.lookup(StreamParser.class, "plain/1.0");
            plainRendererFactory = componentManager.lookup(PrintRendererFactory.class, "plain/1.0");
            linkParser = componentManager.lookup(LinkParser.class);
            imageParser = componentManager.lookup(ImageParser.class);

            XWikiGeneratorListener wikimodelListener =
                new XWikiGeneratorListener(streamParser, listener, linkParser, imageParser, this.plainRendererFactory,
                    idGenerator);

            wemMediaWikiParser.parse(new StringReader(str.toString()), wikimodelListener);
        } catch (Exception e) {
            throw new MediaWikiImporterException("Unable to parse text with MediaWikiParser", e);
        }
    }

    public void setComponentManager(ComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

}
