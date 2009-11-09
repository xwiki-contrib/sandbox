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
package org.xwiki.eclipse.ui.editors.scanners.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class BalancedParenthesisRule implements IPredicateRule
{
    private IToken token;

    private char startingChar;

    public BalancedParenthesisRule(char startingChar, IToken token)
    {
        this.startingChar = startingChar;
        this.token = token;
    }

    public IToken evaluate(ICharacterScanner scanner, boolean resume)
    {
        /*
         * Here the logic is the following: isolate the partition between # and a white space provided that all
         * parenthesis are balanced. If there are open parenthesis, keep scanning until the last parenthesis is closed.
         */
        int parenthesis = 0;

        int c = scanner.read();
        if (c == startingChar) {
            while ((c = scanner.read()) != ICharacterScanner.EOF) {
                if (Character.isWhitespace(c) && parenthesis == 0) {
                    scanner.unread();
                    return token;
                } else if (c == '(') {
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                    /* If this is the last parenthesis, then end scanning */
                    if (parenthesis == 0) {
                        return token;
                    }
                    /*
                     * If we find some closing parenthesis that leads to an unbalanced situation, then this parenthesis
                     * is part of the enclosing #directive. Unread it
                     */
                    else if (parenthesis < 0) {
                        scanner.unread();
                        return token;
                    }
                }
            }

            return token;
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    public IToken getSuccessToken()
    {
        return token;
    }

    public IToken evaluate(ICharacterScanner scanner)
    {
        return evaluate(scanner, false);
    }

}
