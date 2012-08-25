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

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.url.URLRewriter;

/**
 * An XML filter that outputs meta data useful for client side scripts.
 * 
 * @version $Id$
 */
public class HTMLMetaDataXMLFilter extends XMLFilterImpl
{
    /**
     * The name of the id element attribute.
     */
    private static final String ID = "id";

    /**
     * The name of the 'name' element attribute.
     */
    private static final String NAME = "name";

    /**
     * The name of the value attribute.
     */
    private static final String VALUE = "value";

    /**
     * The name of the type attribute.
     */
    private static final String TYPE = "type";

    /**
     * The CDATA XML attribute type.
     */
    private static final String CDATA = "CDATA";

    /**
     * The name of the input HTML element.
     */
    private static final String INPUT = "input";

    /**
     * The object used to rewrite URLs.
     */
    private final URLRewriter urlRewriter;

    /**
     * Flag that specifies if the filtered HTML is a fragment (e.g. the response to an AJAX request) or an entire
     * document.
     */
    private final boolean fragment;

    /**
     * Creates a new HTML XML filter that outputs meta data useful for client side scripts.
     * 
     * @param urlRewriter the object used to rewrite URLs
     * @param fragment {@code true} if we are filtering an HTML fragment, {@code false} if we are filtering an entire
     *            HTML document
     */
    public HTMLMetaDataXMLFilter(URLRewriter urlRewriter, boolean fragment)
    {
        this.urlRewriter = urlRewriter;
        this.fragment = fragment;
    }

    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();

        if (!fragment) {
            // Make the resource URL available to client side scripts. The resource URL could be used to create portlet
            // URLs from the client side. Use this URL to send AJAX requests.
            outputMetaData("resourceURL", urlRewriter.rewrite("", RequestType.RESOURCE), true, false);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(uri, localName, qName, atts);

        if ("form".equalsIgnoreCase(localName)) {
            // Submitting an HTML form with an AJAX request poses a particular problem in the portlet world: the form
            // action URL must be an Action PortletURL (in order for the form to work without JavaScript) while the AJAX
            // request must be send to a ResourceURL (to avoid having the portal HTML mark-up in the response). We pass
            // the original form action URL in a hidden input element so that the form can be correctly submitted to the
            // 'resourceURL' previously generated in #startDocument().
            outputMetaData("org.xwiki.portlet.parameter.dispatchURL", atts.getValue("action"), false, true);
        }
    }

    /**
     * Outputs an {@code <input type="hidden">} HTML element that holds the given meta data.
     * 
     * @param key the meta data key
     * @param value the meta data value
     * @param withId {@code true} to output the 'id' attribute, {@code false} otherwise
     * @param withName {@code true} to output the 'name' attribute, {@code false} otherwise
     * @throws SAXException if writing the meta data fails
     */
    private void outputMetaData(String key, String value, boolean withId, boolean withName) throws SAXException
    {
        AttributesImpl attributes = new AttributesImpl();
        if (withId) {
            attributes.addAttribute(null, ID, ID, "ID", key);
        }
        if (withName) {
            attributes.addAttribute(null, NAME, NAME, CDATA, key);
        }
        attributes.addAttribute(null, TYPE, TYPE, CDATA, "hidden");
        attributes.addAttribute(null, VALUE, VALUE, CDATA, StringUtils.defaultString(value));
        super.startElement(null, INPUT, INPUT, attributes);
        super.endElement(null, INPUT, INPUT);
    }
}
