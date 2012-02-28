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

import java.util.regex.Pattern;

import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.xwiki.portlet.model.RequestType;
import org.xwiki.portlet.url.URLRewriter;

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
public class CSSStyleSheetFilter
{
    /**
     * A pattern that can be used to capture id selectors.
     */
    private static final Pattern ID_SELECTOR_PATTERN = Pattern.compile("#([a-zA-Z][\\w\\-\\:\\.]*)");

    /**
     * A pattern that can be used to capture BODY selectors.
     */
    private static final Pattern BODY_SELECTOR_PATTERN =
        Pattern.compile("(^|[^\\w\\-\\:\\.#])(body(?:[^\\w\\-]|$))", Pattern.CASE_INSENSITIVE);

    /**
     * A pattern that can be used to capture "BODY with id" selectors.
     */
    private static final Pattern BODY_WITH_ID_SELECTOR_PATTERN =
        Pattern.compile("(^|[^\\w\\-\\:\\.#])body(#[a-zA-Z][\\w\\-\\:\\.]*)", Pattern.CASE_INSENSITIVE);

    /**
     * A pattern that can be used to capture HTML selectors that are followed by an element, id or class selector.
     */
    private static final Pattern HTML_SELECTOR_PATTERN =
        Pattern.compile("((?:^|[^\\w\\-\\:\\.#])html)(\\s*(?:[a-zA-Z\\*\\.#]|$))", Pattern.CASE_INSENSITIVE);

    /**
     * Special characters that need to be escaped inside an URI as per http://www.w3.org/TR/CSS2/syndata.html#uri:
     * <p>
     * Some characters appearing in an unquoted URI, such as parentheses, white space characters, single quotes (') and
     * double quotes ("), must be escaped with a backslash so that the resulting URI value is a URI token: '\(', '\)'.
     */
    private static final char[] URI_SPECIAL_CHARS = new char[] {'(', ')', ' ', '"', '\'', '\\'};

    /**
     * The string used to name-space all style rules and element identifiers used in CSS selectors.
     */
    private final String namespace;

    /**
     * The object used to rewrite URLs.
     */
    private final URLRewriter urlRewriter;

    /**
     * Creates a new {@link CSSStyleSheet} filter that rewrites import URLs using the given URL rewriter and name-spaces
     * all style rules using the given name-space.
     * 
     * @param namespace the string used to name-space all style rules and element identifiers used in CSS selectors
     * @param urlRewriter the object used to rewrite URLs
     */
    public CSSStyleSheetFilter(String namespace, URLRewriter urlRewriter)
    {
        this.namespace = namespace;
        this.urlRewriter = urlRewriter;
    }

    /**
     * @param styleSheet the style sheet to be filtered
     */
    public void filter(CSSStyleSheet styleSheet)
    {
        // Should we filter only the style sheets that target the screen media type?
        CSSRuleList rules = styleSheet.getCssRules();
        for (int i = 0; i < rules.getLength(); i++) {
            filter(rules.item(i));
        }
    }

    /**
     * Filters the given CSS rule based on its type.
     * 
     * @param rule a CSS rule
     */
    private void filter(CSSRule rule)
    {
        switch (rule.getType()) {
            case CSSRule.IMPORT_RULE:
                filter((CSSImportRule) rule);
                break;
            case CSSRule.STYLE_RULE:
                filter((CSSStyleRule) rule);
                break;
            default:
                break;
        }
    }

    /**
     * Rewrites the URL of the given import rule.
     * 
     * @param importRule an import rule
     */
    private void filter(CSSImportRule importRule)
    {
        // It's a pity we can't set the import URL directly using the CSSImportRule interface. We don't want to cast the
        // passed object because we want to keep this class independent of the underlying implementation of
        // CSSImportRule.
        // Should we rewrite only import rules that target the screen media type?
        String media = importRule.getMedia().getMediaText();
        String url = urlRewriter.rewrite(importRule.getHref(), RequestType.RESOURCE);
        importRule.setCssText(String.format("@import url(%s) %s;", escapeUnquotedURI(url), media));
    }

    /**
     * Escapes URI values in CSS rules, as per http://www.w3.org/TR/CSS2/syndata.html#uri:
     * <p>
     * Some characters appearing in an unquoted URI, such as parentheses, white space characters, single quotes (') and
     * double quotes ("), must be escaped with a backslash so that the resulting URI value is a URI token: '\(', '\)'.
     * 
     * @param uri the URI to be escaped
     * @return the escaped URI
     */
    private String escapeUnquotedURI(CharSequence uri)
    {
        StringBuilder result = new StringBuilder(uri.length());
        for (int i = 0; i < uri.length(); i++) {
            char c = uri.charAt(i);
            for (int j = 0; j < URI_SPECIAL_CHARS.length; j++) {
                if (c == URI_SPECIAL_CHARS[j]) {
                    result.append('\\');
                    break;
                }
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Rewrites the selector of the given style rule to prevent CSS interference inside a portal page.
     * 
     * @param styleRule a style rule
     */
    private void filter(CSSStyleRule styleRule)
    {
        String[] selectorComponents = styleRule.getSelectorText().split("\\s*,\\s*");
        StringBuilder selectorText = new StringBuilder();
        String selectorComponentSeparator = "";
        String namespacedIdSelector = String.format("#%s-$1", namespace);
        String namespacedHTMLSelector = String.format("$1 #%s $2", namespace);
        for (int i = 0; i < selectorComponents.length; i++) {
            selectorText.append(selectorComponentSeparator);
            selectorComponentSeparator = ",";

            String selectorComponent = selectorComponents[i];

            // Remove the BODY when an id is specified.
            selectorComponent = BODY_WITH_ID_SELECTOR_PATTERN.matcher(selectorComponent).replaceAll("$1$2");
            // Transform BODY element selector into an ID selector.
            selectorComponent = BODY_SELECTOR_PATTERN.matcher(selectorComponent).replaceAll("$1#$2");

            // Ensure element identifiers are unique in the context of the portal page.
            selectorComponent = ID_SELECTOR_PATTERN.matcher(selectorComponent).replaceAll(namespacedIdSelector);

            // Name-space selectors to avoid CSS interference.
            String result = HTML_SELECTOR_PATTERN.matcher(selectorComponent).replaceAll(namespacedHTMLSelector);
            if (selectorComponent.equalsIgnoreCase(result)) {
                selectorText.append('#').append(namespace).append(' ').append(selectorComponent);
            } else {
                selectorText.append(result);
            }
        }
        styleRule.setSelectorText(selectorText.toString());
    }
}
