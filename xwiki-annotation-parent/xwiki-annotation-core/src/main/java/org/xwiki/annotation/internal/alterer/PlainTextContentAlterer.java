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

import org.xwiki.annotation.SyntaxFilter;
import org.xwiki.annotation.internal.content.AlteredContent;
import org.xwiki.annotation.internal.content.AlteredContentImpl;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Plain text alterer from content. It uses synthaxFilter in order to remove forbidden characters.
 * 
 * @version $Id$
 */
@Component("PLAINTEXT")
public class PlainTextContentAlterer extends AbstractContentAlterer
{
    @Requirement
    private SyntaxFilter syntaxFilter;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.alterer.AbstractContentAlterer#alter(java.lang.CharSequence)
     */
    public AlteredContent alter(CharSequence sequence)
    {
        StringBuffer buffer = new StringBuffer();
        Map<Integer, Integer> initialToAltered = new HashMap<Integer, Integer>();
        Map<Integer, Integer> alteredToInitial = new HashMap<Integer, Integer>();

        // index altered
        int j = 0;
        // number of refused chars
        int z = 0;
        Character c;
        for (int i = 0; i < sequence.length(); ++i) {
            c = sequence.charAt(i);
            if (syntaxFilter.accept(c)) {
                buffer.append(c);
                for (int t = 0; t <= z; ++t) {
                    // 1+0;1 // 1+1;1
                    initialToAltered.put(i - t, j);
                }
                alteredToInitial.put(j, i);
                ++j;
                z = 0;
            } else {
                z++;
            }
        }
        if (j != 0) {
            for (int t = 0; t < z; ++t) {
                initialToAltered.put(sequence.length() - 1 - t, j - 1);
            }
        }
        return new AlteredContentImpl(buffer.toString(), sequence.length(), initialToAltered, alteredToInitial);
    }
}
