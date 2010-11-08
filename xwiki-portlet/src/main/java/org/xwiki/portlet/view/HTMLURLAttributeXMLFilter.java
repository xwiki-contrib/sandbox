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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.url.URLRewriter;

/**
 * An XML filter that rewrites some of the servlet URLs into portlet URLs. Only relative URLs are changed. The following
 * components are affected:
 * <ul>
 * <li>anchors' {@code href} attribute</li>
 * <li>forms' {@code action} attribute</li>
 * <li>scripts' {@code src} attribute</li>
 * <li>links' {@code href} attribute when style sheets are imported</li>
 * </ul>
 * 
 * @version $Id$
 */
public class HTMLURLAttributeXMLFilter extends XMLFilterImpl
{
    /**
     * Utility class used to rewrite URL attributes.
     */
    private static class URLAttributeRewriter
    {
        /**
         * The name of the attribute that is rewritten.
         */
        private final String attributeName;

        /**
         * Used to enforce a portlet URL type.
         */
        private final RequestType requestType;

        /**
         * The object used to rewrite URLs.
         */
        private final URLRewriter urlRewriter;

        /**
         * Creates a new URL attribute rewriter that will transform the value of the specified attribute using the given
         * URL rewriter.
         * 
         * @param attributeName the name of the attribute that is rewritten
         * @param requestType used to enforce a portlet URL type
         * @param urlRewriter the object used to rewrite URLs
         */
        public URLAttributeRewriter(String attributeName, RequestType requestType, URLRewriter urlRewriter)
        {
            this.attributeName = attributeName;
            this.requestType = requestType;
            this.urlRewriter = urlRewriter;
        }

        /**
         * Rewrites the value of the {@link #attributeName} taken from the given attributes using the underlying URL
         * rewriter.
         * 
         * @param attributes the element attributes
         * @return the new URL
         */
        public String rewriteURL(Attributes attributes)
        {
            String url = attributes.getValue(attributeName);
            return url != null ? urlRewriter.rewrite(url, requestType) : null;
        }

        /**
         * @return the name of the attribute that is rewritten
         */
        public String getAttributeName()
        {
            return attributeName;
        }
    }

    /**
     * The name of the anchor attribute holding the URL.
     */
    private static final String HREF = "href";

    /**
     * The mapping between tag names and their corresponding URL attribute rewriter.
     */
    private final Map<String, URLAttributeRewriter> urlAttributeRewriters = new HashMap<String, URLAttributeRewriter>();

    /**
     * Creates a new HTML XML filter which rewrites servlet URLs into portlet URLs.
     * 
     * @param urlRewriter the object used to rewrite URLs
     * @param portletNamespace the string used to name-space HTML element identifiers in the context of the portal page
     */
    public HTMLURLAttributeXMLFilter(URLRewriter urlRewriter, String portletNamespace)
    {
        urlAttributeRewriters.put("a", new URLAttributeRewriter(HREF, null, urlRewriter));
        urlAttributeRewriters.put("form", new URLAttributeRewriter("action", RequestType.ACTION, urlRewriter));
        urlAttributeRewriters.put("script", new URLAttributeRewriter("src", RequestType.RESOURCE, urlRewriter));
        urlAttributeRewriters.put("link", new URLAttributeRewriter(HREF, RequestType.RESOURCE, urlRewriter)
        {
            @Override
            public String rewriteURL(Attributes attributes)
            {
                if ("stylesheet".equalsIgnoreCase(attributes.getValue("rel"))) {
                    return super.rewriteURL(attributes);
                }
                return attributes.getValue(getAttributeName());
            }
        });
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(uri, localName, qName, rewriteURL(uri, qName, atts));
    }

    /**
     * Rewrites relative servlet URLs into portlet URLs.
     * 
     * @param uri the URI that defines the element name
     * @param qName the qualified element name
     * @param atts the element attributes
     * @return the new attributes
     */
    private Attributes rewriteURL(String uri, String qName, Attributes atts)
    {
        Attributes newAtts = atts;
        URLAttributeRewriter urlAttributeRewriter = urlAttributeRewriters.get(qName);
        if (urlAttributeRewriter != null) {
            String url = urlAttributeRewriter.rewriteURL(atts);
            if (url != null) {
                String attributeName = urlAttributeRewriter.getAttributeName();
                newAtts = atts instanceof AttributesImpl ? (AttributesImpl) atts : new AttributesImpl(atts);
                if (atts.getValue(attributeName) != null) {
                    // Modify existing attribute.
                    ((AttributesImpl) newAtts).setValue(atts.getIndex(attributeName), url);
                } else {
                    // Add a new attribute.
                    ((AttributesImpl) newAtts).addAttribute(uri, attributeName, attributeName, "CDATA", url);
                }
            }
        }
        return newAtts;
    }
}
