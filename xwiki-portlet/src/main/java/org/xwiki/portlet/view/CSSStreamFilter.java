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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.xwiki.portlet.url.URLRewriter;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS21;

/**
 * Rewrites a CSS stream so that it can be used by a portlet in a portal page without affecting other portlets or the
 * portal page itself. The following transformations are executed:
 * <ul>
 * <li>Name-space all CSS selectors by prefixing them with a given string (e.g. the identifier of the portlet content
 * container)</li>
 * <li>Rename identifiers used in selectors to ensure their uniqueness inside the portal page</li>
 * <li>Rewrite import URLs so that CSS style sheets are requested through the portlet</li>
 * </ul>
 * .
 * 
 * @version $Id$
 */
public class CSSStreamFilter implements StreamFilter
{
    /**
     * The logger instance.
     */
    private static final Log LOG = LogFactory.getLog(CSSStreamFilter.class);

    /**
     * The object used to parse the CSS.
     */
    private final CSSOMParser cssParser = new CSSOMParser(new SACParserCSS21());

    /**
     * The object used to filter a CSS style sheet object.
     */
    private final CSSStyleSheetFilter styleSheetFilter;

    /**
     * Creates a new CSS stream filter that uses the given URL rewriter to transform import URLs and the given
     * name-space to prevent style rules from affecting other portlets or the portal page itself.
     * 
     * @param namespace the portlet name-space
     * @param urlRewriter the object used to rewrite servlet URLs
     */
    public CSSStreamFilter(String namespace, URLRewriter urlRewriter)
    {
        styleSheetFilter = new CSSStyleSheetFilter(namespace, urlRewriter);
    }

    /**
     * {@inheritDoc}
     * 
     * @see StreamFilter#filter(Reader, Writer)
     */
    public void filter(Reader reader, Writer writer)
    {
        try {
            // Parse the CSS.
            CSSStyleSheet styleSheet = cssParser.parseStyleSheet(new InputSource(reader), null, null);

            // Filter the CSS.
            styleSheetFilter.filter(styleSheet);

            // Serialize the CSS.
            CSSRuleList rules = styleSheet.getCssRules();
            for (int i = 0; i < rules.getLength(); i++) {
                writer.write(rules.item(i).getCssText());
            }
        } catch (IOException e) {
            LOG.error("Failed to rewrite servlet CSS.", e);
        }
    }
}
