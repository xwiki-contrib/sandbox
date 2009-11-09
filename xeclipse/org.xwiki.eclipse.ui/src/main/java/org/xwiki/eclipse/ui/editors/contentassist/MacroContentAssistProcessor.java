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
package org.xwiki.eclipse.ui.editors.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.xwiki.eclipse.ui.editors.PageEditor;
import org.xwiki.eclipse.ui.editors.VelocityDirectiveType;
import org.xwiki.eclipse.ui.editors.utils.Utils;

public class MacroContentAssistProcessor implements IContentAssistProcessor
{
    private static Pattern MACRO_DECLARATION_PATTERN = Pattern.compile("#macro *\\( *([\\p{Alnum}_]+)");

    public MacroContentAssistProcessor(PageEditor pageEditor)
    {
        super();
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        IDocument document = viewer.getDocument();
        String macroPrefix = Utils.getPrefix(document, offset, "#", "$(\n");
        if (macroPrefix != null) {
            for (String directive : getMacros(document, offset)) {
                if (directive.startsWith(macroPrefix)) {
                    int cursorPos;
                    if (directive.equals(VelocityDirectiveType.ELSE) || directive.equals(VelocityDirectiveType.END)
                        || directive.equals(VelocityDirectiveType.STOP)) {
                        cursorPos = directive.length() - 1;
                    } else {
                        directive += "()";
                        cursorPos = directive.length() - 2;
                    }

                    result.add(new CompletionProposal(directive, offset - macroPrefix.length(), macroPrefix.length(),
                        cursorPos, null, "#" + directive, null, null));
                }
            }

            Collections.sort(result, new Comparator<ICompletionProposal>()
            {
                public int compare(ICompletionProposal proposal1, ICompletionProposal proposal2)
                {
                    return proposal1.getDisplayString().compareTo(proposal2.getDisplayString());
                }

                public boolean equals(Object proposal)
                {
                    return false;
                }
            });

            return result.toArray(new ICompletionProposal[result.size()]);
        }

        if (result.size() > 0) {
            return result.toArray(new ICompletionProposal[result.size()]);
        }

        return null;
    }

    private List<String> getMacros(IDocument document, int offset)
    {
        List<String> list = new ArrayList<String>();
        for (VelocityDirectiveType d : VelocityDirectiveType.values()) {
            list.add(d.toString().toLowerCase());
        }

        try {
            String text = document.get(0, offset);

            Matcher m = MACRO_DECLARATION_PATTERN.matcher(text);
            while (m.find()) {
                list.add(m.group(1));
            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return list;
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset)
    {
        return null;
    }

    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return null;
    }

    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }

    public String getErrorMessage()
    {
        return null;
    }

}
