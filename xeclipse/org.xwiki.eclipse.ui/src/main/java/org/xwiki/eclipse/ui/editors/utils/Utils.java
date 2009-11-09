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
package org.xwiki.eclipse.ui.editors.utils;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class Utils
{
    /**
     * Get a prefix from the current offset to a given character (not included). Examples (_ is the current offset):
     * <code>
     * [test_ : getPrefix(..., _, 0, '[', "]") = 'test'
     * [test]..._ : getPrefix(..., _, 0, '[', "]") = null
     * _ is the current offset
     * </code>
     * 
     * @param document
     * @param offset The starting offset *
     * @param startCharacter The start character that marks the beginning of the scanning region.
     * @param blockingCharacters A string containing all the blocking characters that will make the scanning fail.
     * @return The found prefix or null if a blocking character is encountered or if the starCharacter is not found.
     */
    public static String getPrefix(IDocument document, int offset, String startCharacters, String blockingCharacters)
    {
        String result = null;

        if (offset == 0) {
            return null;
        }

        try {
            int currentOffset = offset - 1;

            while (currentOffset >= 0) {
                if (blockingCharacters.indexOf(document.getChar(currentOffset)) != -1) {
                    result = null;
                    break;
                }

                if (startCharacters.indexOf(document.getChar(currentOffset)) != -1) {
                    result = document.get(currentOffset + 1, offset - currentOffset - 1);
                    break;
                }

                currentOffset--;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return result;
    }
}
