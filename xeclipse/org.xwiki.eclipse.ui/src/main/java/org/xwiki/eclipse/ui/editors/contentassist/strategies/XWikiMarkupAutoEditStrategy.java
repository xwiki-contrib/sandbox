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
package org.xwiki.eclipse.ui.editors.contentassist.strategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.xwiki.eclipse.ui.editors.Constants;

public class XWikiMarkupAutoEditStrategy implements IAutoEditStrategy
{
    private Pattern listBulletPattern = Pattern.compile(String.format("^(?:%s)", Constants.LIST_BULLET_PATTERN));

    public void customizeDocumentCommand(IDocument document, DocumentCommand command)
    {
        try {
            if (command.text.equals("*")) {
                if (!isListStarBullet(document, command)) {
                    configureCommand(command, "**", 1);
                }
            } else if (command.text.equals("~")) {
                if (document.getChar(command.offset - 1) == '~') {
                    configureCommand(command, "~~~", 1);
                }
            } else if (command.text.equals("_")) {
                if (document.getChar(command.offset - 1) == '_') {
                    configureCommand(command, "___", 1);
                }
            } else if (command.text.equals("-")) {
                if (document.getChar(command.offset - 1) == '-') {
                    configureCommand(command, "---", 1);
                }
            } else if (command.text.equals("[")) {
                configureCommand(command, "[]", 1);
            } else if (command.text.equals(">")) {
                String tag = getTag(document, '<', command.offset - 1);
                if (tag != null) {
                    String closingTag = String.format("></%s>", tag.substring(1));

                    configureCommand(command, closingTag, 1);
                }
            } else if (command.text.equals("\n")) {
                String bullet = getListBullet(document, command.offset);
                if (bullet != null) {
                    configureCommand(command, bullet, bullet.length());
                }
            } else if (command.text.equals("/")) {
                if (document.getChar(command.offset - 1) == '/') {
                    configureCommand(command, "///", 1);
                }
            } else if (command.text.equals(",")) {
                if (document.getChar(command.offset - 1) == ',') {
                    configureCommand(command, ",,,", 1);
                }
            } else if (command.text.equals("^")) {
                if (document.getChar(command.offset - 1) == '^') {
                    configureCommand(command, "^^^", 1);
                }
            }
        } catch (BadLocationException e) {
        }
    }

    private String getListBullet(IDocument document, int offset)
    {
        try {
            IRegion lineRegion = document.getLineInformationOfOffset(offset);
            String line = document.get(lineRegion.getOffset(), lineRegion.getLength());

            Matcher m = listBulletPattern.matcher(line);
            if (m.find()) {
                if (!line.equals(m.group())) {
                    return String.format("\n%s", m.group());
                }
            }
        } catch (BadLocationException e) {
        }

        return null;

    }

    private boolean isListStarBullet(IDocument document, DocumentCommand command)
    {
        try {
            IRegion lineRegion = document.getLineInformationOfOffset(command.offset);
            String line = document.get(lineRegion.getOffset(), lineRegion.getLength());

            return Pattern.matches("^\\**", line);
        } catch (BadLocationException e) {
        }

        return false;
    }

    private String getTag(IDocument document, char openingChar, int endOffset)
    {
        try {
            int startOffset = endOffset;
            int character;

            while (startOffset >= 0) {
                character = document.getChar(startOffset);
                if (character == '\n') {
                    return null;
                }

                if (character == openingChar) {
                    return document.get(startOffset, endOffset - startOffset + 1);
                }

                startOffset--;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void configureCommand(DocumentCommand command, String text, int caretAdjustement)
    {
        command.text = text;
        command.caretOffset = command.offset + caretAdjustement;
        command.shiftsCaret = false;
    }
}
