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
import org.xwiki.eclipse.ui.editors.Preferences;
import org.xwiki.eclipse.ui.editors.scanners.rules.BalancedParenthesisRule;

public class VelocityScanner extends RuleBasedScanner
{
    public VelocityScanner()
    {
        IToken identifierToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.IDENTIFIER));
        IToken otherStyleToken = new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.UNDERLINE));

        List<IRule> rules = new ArrayList<IRule>();

        rules.add(new SingleLineRule("'", "'", otherStyleToken, '\\'));
        rules.add(new SingleLineRule("\"", "\"", otherStyleToken, '\\'));
        // rules.add(new RegExRule("\\$[\\p{Alnum}\\.\\(\\)]*", identifierToken));
        rules.add(new BalancedParenthesisRule('$', identifierToken));

        setRules(rules.toArray(new IRule[rules.size()]));

        setDefaultReturnToken(new Token(Preferences.getDefault().getTextAttribute(Preferences.Style.MACRO)));
    }
}
