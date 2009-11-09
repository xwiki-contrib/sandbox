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

package org.xwiki.xoo.xwikilib.converter.internal;

import java.io.StringReader;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.xoo.xwikilib.converter.BidirectionalConverter;

/**
 * Default bidirectional converter xhtml - XWiki syntax .
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class DefaultBidirectionalConverter implements BidirectionalConverter
{

    private EmbeddableComponentManager ecm;

    public DefaultBidirectionalConverter(EmbeddableComponentManager cm)
    {
        this.ecm = cm;
    }

    /**
     * {@inheritDoc}
     */
    public String fromXHTML(String html) throws ConversionException
    {
        try {

            Converter converter = ecm.lookup(Converter.class);
            WikiPrinter printer = new DefaultWikiPrinter();
            converter.convert(new StringReader(html), Syntax.XHTML_1_0, Syntax.XWIKI_2_0, printer);
            return printer.toString();

        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String toXHTML(String source) throws ConversionException
    {
        try {

            Converter converter = ecm.lookup(Converter.class);
            WikiPrinter printer = new DefaultWikiPrinter();
            converter.convert(new StringReader(source), Syntax.XWIKI_2_0, Syntax.XHTML_1_0, printer);
            String ret = printer.toString();
            return compatibilityHacks(ret);

        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Solves some of the compatibility problems XHTML-HTML user agents
     * <a>http://www.w3.org/TR/xhtml1/guidelines.html</a>
     * 
     * @param xhtml the input XHTML text
     * @return modified xhtml
     */
    private String compatibilityHacks(String xhtml)
    {
        String ret = xhtml;
        ret.replace("<br/>", "<br />");
        ret.replace("<p/>", "<p></p>");

        return ret;
    }
}
