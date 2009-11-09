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

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.xwiki.eclipse.ui.editors.Constants;
import org.xwiki.eclipse.ui.editors.Preferences;
import org.xwiki.eclipse.ui.editors.scanners.rules.BalancedParenthesisRule;
import org.xwiki.eclipse.ui.editors.scanners.rules.RegExRule;

public class XWikiMarkupScanner extends RuleBasedScanner
{
    public XWikiMarkupScanner()
    {
        IToken boldToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.BOLD));

        IToken italicToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.ITALIC));

        IToken linkToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.LINK));

        IToken listBulletToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.LIST_BULLET));

        IToken definitionTermToken =
            new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.DEFINITION_TERM));

        IToken heading1Token = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.HEADING1));
        IToken heading2Token = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.HEADING2));
        IToken heading3Token = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.HEADING3));
        IToken heading4Token = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.HEADING4));
        IToken heading5Token = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.HEADING5));
        IToken heading6Token = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.HEADING6));

        IToken imageToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.IMAGE));

        IToken identifierToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.IDENTIFIER));

        IToken otherStyleToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.UNDERLINE));

        List<IRule> rules = new ArrayList<IRule>();

        /* RegEx rules work better with respect to SingleLineRules */
        RegExRule regExRule = new RegExRule("1 .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("= .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("1.1 .*\n?", heading2Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("== .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("1.1.1 .*\n?", heading3Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("=== .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("1.1.1.1 .*\n?", heading4Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("==== .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("1.1.1.1.1 .*\n?", heading5Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("===== .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("1.1.1.1.1.1 .*\n?", heading6Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule("====== .*\n?", heading1Token);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule(Constants.LIST_BULLET_PATTERN, listBulletToken);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        regExRule = new RegExRule(Constants.DEFINITION_TERM_PATTERN, definitionTermToken);
        regExRule.setColumnConstraint(0);
        rules.add(regExRule);

        rules.add(new SingleLineRule("**", "**", boldToken, '\\'));
        rules.add(new SingleLineRule("*", "*", boldToken, '\\'));
        rules.add(new SingleLineRule("~~", "~~", italicToken, '\\'));
        rules.add(new SingleLineRule("//", "//", italicToken, '\\'));
        rules.add(new SingleLineRule("[", "]", linkToken, '\\'));
        rules.add(new SingleLineRule("__", "__", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("--", "--", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("<tt>", "</tt>", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("##", "##", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("<sub>", "</sub>", otherStyleToken, '\\'));
        rules.add(new SingleLineRule(",,", ",,", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("<sup>", "</sup>", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("^^", "^^", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("(% style", "%)", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("{image:", "}", imageToken, '\\'));
        rules.add(new SingleLineRule("image:", " ", imageToken, '\\'));

        rules.add(new BalancedParenthesisRule('$', identifierToken));

        setRules(rules.toArray(new IRule[rules.size()]));
    }
}
