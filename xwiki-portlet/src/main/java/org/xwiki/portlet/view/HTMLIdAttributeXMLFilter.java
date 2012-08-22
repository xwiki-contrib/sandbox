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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * An XML filter that rewrites all element identifiers to ensure they are unique in the context of a portal page.
 * 
 * @version $Id$
 */
public class HTMLIdAttributeXMLFilter extends XMLFilterImpl
{
    /**
     * The name of the id element attribute.
     */
    private static final String ID = "id";

    /**
     * The name of the anchor attribute holding the URL.
     */
    private static final String HREF = "href";

    /**
     * The name of the DIV HTML tag.
     */
    private static final String DIV = "div";

    /**
     * The list of HTML attributes that are of type ID, IDREF or IDREFS. We have to hard-code this list because the
     * attribute type is not always properly determined (e.g. when the document type declaration is missing).
     */
    private static final List<String> ID_ATTRIBUTES = Arrays.asList(ID, "for", "headers");

    /**
     * The pattern used to split the value of a {@code IDREFS} attribute.
     */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * The string all element identifiers will be prefixed with.
     */
    private final String namespace;

    /**
     * Flag indicating if the output should be wrapped in a container that has the {@link #namespace} identifier or not.
     */
    private final boolean wrapOutput;

    /**
     * Creates a new XML filter that name-spaces all element identifiers.
     * 
     * @param namespace the string all element identifiers will be prefixed with
     * @param wrapOutput {@code true} to wrap the output in a container that has the {@code namespace} identifier,
     *            {@code false} otherwise
     */
    public HTMLIdAttributeXMLFilter(String namespace, boolean wrapOutput)
    {
        this.namespace = namespace;
        this.wrapOutput = wrapOutput;
    }

    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();

        if (wrapOutput) {
            // Start the portlet output container.
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(null, ID, ID, ID.toUpperCase(), namespace);
            super.startElement(null, DIV, DIV, attributes);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(uri, localName, qName, rewriteURLFragment(rewriteIds(atts)));
    }

    /**
     * Rewrites the value of the {@link #ID_ATTRIBUTES} in the given list of attributes.
     * 
     * @param attributes the list of element attributes
     * @return the given list of attributes where the value of the {@link #ID_ATTRIBUTES} have been changed
     */
    private Attributes rewriteIds(Attributes attributes)
    {
        Attributes newAttributes = null;
        for (String idAttribute : ID_ATTRIBUTES) {
            int index = attributes.getIndex(idAttribute);
            if (index >= 0) {
                if (newAttributes == null) {
                    newAttributes = attributes instanceof AttributesImpl ? attributes : new AttributesImpl(attributes);
                }
                ((AttributesImpl) newAttributes).setValue(index, namespace(attributes.getValue(index)));
            }
        }
        return newAttributes != null ? newAttributes : attributes;
    }

    /**
     * Rewrites URL fragments in anchor URLs relative to the current page.
     * 
     * @param atts the lists of element attributes
     * @return the given list of attributes where the value of the {@link #HREF} attribute has been changed
     */
    private Attributes rewriteURLFragment(Attributes atts)
    {
        String href = atts.getValue(HREF);
        if (href != null && href.startsWith("#")) {
            AttributesImpl newAtts = atts instanceof AttributesImpl ? (AttributesImpl) atts : new AttributesImpl(atts);
            newAtts.setValue(atts.getIndex(HREF), String.format("#%s", namespace(href.substring(1))));
            return newAtts;
        }
        return atts;
    }

    /**
     * Name-spaces an element identifier.
     * 
     * @param id an element id
     * @return a new id that is unique in the context of the portal page
     */
    private String namespace(String id)
    {
        // Handle ID, IDREF and IDREFS attribute types uniformly.
        String[] ids = WHITESPACE.split(id);
        for (int i = 0; i < ids.length; i++) {
            ids[i] = namespace + "-" + ids[i];
        }
        return StringUtils.join(ids, ' ');
    }

    @Override
    public void endDocument() throws SAXException
    {
        if (wrapOutput) {
            // End the portlet output container.
            super.endElement(null, DIV, DIV);
        }

        super.endDocument();
    }
}
