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

package org.xwiki.annotation.internal.alterer;

import java.util.HashMap;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;
import org.xwiki.annotation.internal.content.AlteredContent;
import org.xwiki.annotation.internal.content.AlteredContentImpl;
import org.xwiki.component.annotation.Component;

/**
 * HTML alterer from content. This alterer removes HTML markup, it only keeps text nodes content.
 * 
 * @version $Id$
 */
@Component("HTML")
public class HTMLContentAlterer extends AbstractContentAlterer
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.alterer.AbstractContentAlterer#alter(java.lang.CharSequence)
     */
    public AlteredContent alter(CharSequence sequence)
    {
        StringBuilder sb = new StringBuilder();
        Map<Integer, Integer> initialToAltered = new HashMap<Integer, Integer>();
        Map<Integer, Integer> alteredToInitial = new HashMap<Integer, Integer>();

        Lexer lexer = new Lexer(sequence.toString());
        Node node;
        int start;
        int length;
        String oldtext;
        try {
            while ((node = lexer.nextNode()) != null) {
                if (node instanceof TextNode) {
                    start = node.getStartPosition();
                    oldtext = node.getText();
                    length = node.getText().length();
                    for (int k = 0; k < length; ++k) {
                        initialToAltered.put(start + k, sb.length() + k);
                        alteredToInitial.put(sb.length() + k, start + k);
                    }
                    sb.append(oldtext);
                }
            }
            int last = -1;
            int lastAltered = -1;
            for (int k = 0; k < sequence.length(); ++k) {
                Integer value = initialToAltered.get(k);
                if (value == null) {
                    if (k < last || last == -1) {
                        last = k;
                    }
                } else {
                    lastAltered = value;
                    for (; last < k; ++last) {
                        initialToAltered.put(last, value);
                    }
                    last = k + 1;
                }
            }
            if (lastAltered != -1) {
                for (; last < sequence.length(); ++last) {
                    initialToAltered.put(last, lastAltered);
                }
            }
            return new AlteredContentImpl(sb.toString(), sequence.length(), initialToAltered, alteredToInitial);
        } catch (ParserException e) {
            return null;
        }
    }
}
