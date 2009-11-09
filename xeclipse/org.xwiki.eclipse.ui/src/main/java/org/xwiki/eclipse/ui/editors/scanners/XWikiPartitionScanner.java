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
 *
 */
package org.xwiki.eclipse.ui.editors.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.xwiki.eclipse.ui.editors.scanners.rules.BalancedParenthesisRule;

public class XWikiPartitionScanner extends RuleBasedPartitionScanner
{
    public static final String XWIKI_HTML = "__xwiki_html";

    public static final String XWIKI_CODE = "__xwiki_code";

    public static final String XWIKI_PRE = "__xwiki_pre";

    public static final String XWIKI_DL = "__xwiki_dl";

    public static final String XWIKI_TABLE = "__xwiki_table";

    public static final String XWIKI_STYLE = "__xwiki_style";

    public static final String VELOCITY = "__velocity";

    public static final String[] ALL_PARTITIONS =
        {XWIKI_HTML, XWIKI_CODE, XWIKI_PRE, XWIKI_DL, XWIKI_TABLE, XWIKI_STYLE, VELOCITY};

    public XWikiPartitionScanner()
    {
        IToken htmlToken = new Token(XWIKI_HTML);
        IToken codeToken = new Token(XWIKI_CODE);
        IToken preToken = new Token(XWIKI_PRE);
        IToken tableToken = new Token(XWIKI_TABLE);
        IToken dlToken = new Token(XWIKI_DL);
        IToken styleToken = new Token(XWIKI_STYLE);
        IToken velocityToken = new Token(VELOCITY);

        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();

        rules.add(new MultiLineRule("{{html}}", "{{/html}}", htmlToken));
        rules.add(new MultiLineRule("{code}", "{code}", codeToken));
        rules.add(new MultiLineRule("{pre}", "{/pre}", preToken));
        rules.add(new MultiLineRule("{{{", "}}}", preToken));
        rules.add(new MultiLineRule("{table}", "{table}", tableToken));
        rules.add(new MultiLineRule("{style:", "{style}", styleToken));
        rules.add(new MultiLineRule("<dl>", "</dl>", dlToken));
        rules.add(new MultiLineRule("{{velocity", "{{/velocity}}", velocityToken));
        rules.add(new BalancedParenthesisRule('#', velocityToken));

        setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
    }
}
