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

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * A content assist processor that re-groups other assist processor and queries them for getting proposals.
 */
public class CompoundContentAssistProcessor implements IContentAssistProcessor
{
    private List<IContentAssistProcessor> contentAssistProcessors;

    public CompoundContentAssistProcessor()
    {
        contentAssistProcessors = new ArrayList<IContentAssistProcessor>();
    }

    public void addContentAssistProcessor(IContentAssistProcessor contentAssistProcessor)
    {
        contentAssistProcessors.add(contentAssistProcessor);
    }

    public void removeContentAssistProcessor(IContentAssistProcessor contentAssistProcessor)
    {
        contentAssistProcessors.remove(contentAssistProcessor);
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        /* Query all the registered assist processors and collect all the returned proposals. */
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
        for (IContentAssistProcessor contentAssistProcessor : contentAssistProcessors) {
            ICompletionProposal[] proposals = contentAssistProcessor.computeCompletionProposals(viewer, offset);
            if (proposals != null && proposals.length > 0) {
                for (ICompletionProposal proposal : proposals) {
                    result.add(proposal);
                }
            }
        }

        return result.toArray(new ICompletionProposal[result.size()]);
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
