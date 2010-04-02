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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * An XML filter that rewrites some of the servlet URLs into portlet URLs. Only relative URLs that are prefixed with a
 * specified context path are changed. The following components are affected:
 * <ul>
 * <li>anchors' {@code href} attribute</li>
 * <li>forms' {@code action} attribute</li>
 * <li>URLs in-lined in script tags</li>
 * </ul>
 * 
 * @version $Id$
 */
public class URLRewriterXMLFilter extends XMLFilterImpl
{
    /**
     * The name of the anchor attribute holding the URL.
     */
    private static final String HREF = "href";

    /**
     * The name of the form attribute holding the URL to the resource that handled the form submission.
     */
    private static final String ACTION = "action";

    /**
     * The name of the script tag.
     */
    private static final String SCRIPT = "script";

    /**
     * The object used to create portlet URLs.
     */
    private final DispatchURLFactory dispatchURLFactory;

    /**
     * The servlet context path. Relative URLs prefixed with this string are transformed into portlet URLs.
     */
    private final String contextPath;

    /**
     * Flag indicating if we are inside a script tag.
     */
    private boolean inScript;

    /**
     * Creates a new URL rewriter XML filter which converts relative servlet URLs prefixed with the given context path
     * to portlet URLs created using the given URL factory.
     * 
     * @param dispatchURLFactory the object used to create portlet URLs
     * @param contextPath the servlet context path
     */
    public URLRewriterXMLFilter(DispatchURLFactory dispatchURLFactory, String contextPath)
    {
        this.dispatchURLFactory = dispatchURLFactory;
        this.contextPath = contextPath;
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
            String action = atts.getValue(ACTION);
            if (action == null) {
                action = dispatchURLFactory.createURL(null, RequestType.ACTION).toString();
                AttributesImpl attsImpl = new AttributesImpl(atts);
                attsImpl.addAttribute(uri, ACTION, ACTION, "CDATA", action);
                super.startElement(uri, localName, qName, attsImpl);
                return;
            } else if (action.length() == 0) {
                action = dispatchURLFactory.createURL(null, RequestType.ACTION).toString();
                AttributesImpl attsImpl = new AttributesImpl(atts);
                attsImpl.setValue(atts.getIndex(ACTION), action);
                super.startElement(uri, localName, qName, attsImpl);
                return;
            } else if (action.startsWith(contextPath)) {
                action =
                    dispatchURLFactory.createURL(action.substring(contextPath.length()), RequestType.ACTION).toString();
                AttributesImpl attsImpl = new AttributesImpl(atts);
                attsImpl.setValue(atts.getIndex(ACTION), action);
                super.startElement(uri, localName, qName, attsImpl);
                return;
            }
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
