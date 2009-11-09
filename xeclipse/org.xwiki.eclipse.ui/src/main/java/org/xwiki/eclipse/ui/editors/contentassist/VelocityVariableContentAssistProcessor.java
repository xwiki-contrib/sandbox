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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.xwiki.eclipse.ui.editors.XWikiApiType;
import org.xwiki.eclipse.ui.editors.utils.Utils;

public class VelocityVariableContentAssistProcessor implements IContentAssistProcessor
{
    private Pattern VARIABLE_REFERENCE_PATTERN = Pattern.compile("\\$([\\p{Alnum}_]+)");

    public VelocityVariableContentAssistProcessor(PageEditor pageEditor)
    {
        super();
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        IDocument document = viewer.getDocument();
        String variablePrefix = Utils.getPrefix(document, offset, "$", " (\n");

        if (variablePrefix != null) {
            if (!variablePrefix.contains(".")) { // Normal variable proposal

                List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
                for (String variable : getVariables(document, offset)) {
                    if (variable.startsWith(variablePrefix)) {
                        result.add(new CompletionProposal(variable, offset - variablePrefix.length(), variablePrefix
                            .length(), variable.length(), null, "$" + variable, null, null));
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
            } else { // API proposal
                int index = variablePrefix.indexOf('.');

                try {
                    XWikiApiType xwikiApiType = XWikiApiType.valueOf(variablePrefix.substring(0, index).toUpperCase());
                    XWikiApiCompletionProcessor xwikiAPIProcessor = new XWikiApiCompletionProcessor(xwikiApiType);

                    return xwikiAPIProcessor.computeCompletionProposals(viewer, offset);
                } catch (Exception e) {
                    return null;
                }
            }
        }

        return null;
    }

    private Set<String> getVariables(IDocument document, int offset)
    {
        Set<String> variables = new HashSet<String>();

        for (XWikiApiType xwikiApiType : XWikiApiType.values()) {
            variables.add(xwikiApiType.toString().toLowerCase());
        }

        try {
            String text = document.get(0, offset);

            Matcher m = VARIABLE_REFERENCE_PATTERN.matcher(text);
            while (m.find()) {
                variables.add(m.group(1));
            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return variables;
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
