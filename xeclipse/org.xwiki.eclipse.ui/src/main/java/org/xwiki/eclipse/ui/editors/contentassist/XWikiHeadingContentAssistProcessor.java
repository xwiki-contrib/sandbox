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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class XWikiHeadingContentAssistProcessor implements IContentAssistProcessor
{
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        try {
            IDocument document = viewer.getDocument();
            ITypedRegion partition = document.getPartition(offset);
            if (isOffsetValidForProposal(document, offset, partition.getOffset())) {
                result.add(new CompletionProposal("1 ", offset, 0, 2, null, "1 Heading level 1", null, null));
                result.add(new CompletionProposal("1.1 ", offset, 0, 4, null, "1.1 Heading level 2", null, null));
                result.add(new CompletionProposal("1.1.1 ", offset, 0, 6, null, "1.1.1 Heading level 3", null, null));
                result
                    .add(new CompletionProposal("1.1.1.1 ", offset, 0, 8, null, "1.1.1.1 Heading level 4", null, null));
                result.add(new CompletionProposal("1.1.1.1.1 ", offset, 0, 10, null, "1.1.1.1.1 Heading level 5", null,
                    null));
                result.add(new CompletionProposal("1.1.1.1.1.1 ", offset, 0, 12, null, "1.1.1.1.1.1 Heading level 6",
                    null, null));
            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (result.size() > 0) {
            return result.toArray(new ICompletionProposal[result.size()]);
        }

        return null;
    }

    private boolean isOffsetValidForProposal(IDocument document, int offset, int partitionStartOffset)
    {
        if (offset == 0) {
            return true;
        }

        try {
            if (document.getChar(offset - 1) == '\n') {
                return true;
            }
        } catch (BadLocationException e) {
        }

        return false;
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
