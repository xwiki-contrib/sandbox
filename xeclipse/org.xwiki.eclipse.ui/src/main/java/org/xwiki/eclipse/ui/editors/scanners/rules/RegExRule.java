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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.xwiki.eclipse.ui.editors.utils.CharacterScannerCharSequence;

/**
 * This rule is capable of detecting a sequence that matches against a given regular expression.
 */
public class RegExRule implements IRule
{
    /**
     * The regular expression pattern.
     */
    private Pattern pattern;

    /**
     * The token to be returned if the a match is found.
     */
    private IToken token;

    /**
     * Optional constraint on the starting column.
     */
    private int columnConstraint;

    /**
     * Constructor
     * 
     * @param patternString The regular expression defining the pattern.
     * @param token The token to be returned on success.
     */
    public RegExRule(String patternString, IToken token)
    {
        pattern = Pattern.compile(patternString);
        this.token = token;
        columnConstraint = -1;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner)
    {
        /* Wrap the character scanner into a char sequence to be used later for pattern matching. */
        CharacterScannerCharSequence charSequence = new CharacterScannerCharSequence(scanner);

        /* If there is a column constraint, check that it is satisfied. */
        if (columnConstraint != -1) {
            if (charSequence.getColumn() != columnConstraint) {
                /*
                 * On failure, push all the read character back into the scanner, so that other rules can process them.
                 */
                charSequence.unread();
                return Token.UNDEFINED;
            }
        }

        Matcher matcher = pattern.matcher(charSequence);

        if (matcher.lookingAt()) {
            /* Push all the characters that are not part of the match back to the scanner. */
            charSequence.unread(charSequence.length() - matcher.end());
            return token;
        }

        /*
         * On failure, push all the read character back to the scanner, so that other rules can process them.
         */
        charSequence.unread();

        return Token.UNDEFINED;
    }

    /**
     * Set the column constraint.
     * 
     * @param columnConstraint The column at which the matched sequence should start.
     */
    public void setColumnConstraint(int columnConstraint)
    {
        this.columnConstraint = columnConstraint;
    }

}
