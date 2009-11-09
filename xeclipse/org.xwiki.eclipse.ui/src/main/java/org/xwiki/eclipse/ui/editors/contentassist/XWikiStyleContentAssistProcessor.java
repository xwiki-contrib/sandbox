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
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.xwiki.eclipse.ui.editors.utils.Utils;

public class XWikiStyleContentAssistProcessor implements IContentAssistProcessor
{
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        IDocument document = viewer.getDocument();

        /* Check if we are in the middle of something where markup is not allowed */
        String linkPrefix = Utils.getPrefix(document, offset, "[>", "]");
        String variablePrefix = Utils.getPrefix(document, offset, "$", " (\n");
        String macroPrefix = Utils.getPrefix(document, offset, "#", "$(\n");

        if (linkPrefix == null && variablePrefix == null && macroPrefix == null) {
            result.add(new CompletionProposal("**", offset, 0, 1, null, "* Bold", null, null));
            result.add(new CompletionProposal("~~~~", offset, 0, 2, null, "~~ Italic", null, null));
            result.add(new CompletionProposal("____", offset, 0, 2, null, "__ Underline", null, null));
            result.add(new CompletionProposal("----", offset, 0, 2, null, "-- Strikeout", null, null));
            result.add(new CompletionProposal("{table}\n{table}", offset, 0, 2, null, "{table}", null, null));
            result.add(new CompletionProposal("{code}\n{code}", offset, 0, 2, null, "{code}", null, null));
            result.add(new CompletionProposal("{pre}\n{/pre}", offset, 0, 2, null, "{pre}", null, null));
        }

        if (result.size() > 0) {
            return result.toArray(new ICompletionProposal[result.size()]);
        }

        return null;
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
