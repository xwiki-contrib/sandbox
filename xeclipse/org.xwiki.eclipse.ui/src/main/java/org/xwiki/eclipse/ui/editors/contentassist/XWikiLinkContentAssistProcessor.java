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
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.editors.utils.Utils;

public class XWikiLinkContentAssistProcessor implements IContentAssistProcessor
{
    private DataManager dataManager;

    public XWikiLinkContentAssistProcessor(DataManager dataManager)
    {
        this.dataManager = dataManager;
    }

    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

        IDocument document = viewer.getDocument();

        String linkPrefix = Utils.getPrefix(document, offset, "[>", "]");

        if (linkPrefix != null) {
            List<XWikiEclipsePageSummary> pageSummaries =
                UIPlugin.getDefault().getAllPageSummariesForDataManager(dataManager, linkPrefix);

            for (XWikiEclipsePageSummary pageSummary : pageSummaries) {
                String pageId = pageSummary.getData().getId();
                if (pageId.startsWith(linkPrefix)
                    || dataManager.getSupportedFunctionalities().contains(Functionality.EFFICIENT_RETRIEVAL)) {
                    result.add(new CompletionProposal(pageId, offset - linkPrefix.length(), linkPrefix.length(), pageId
                        .length(), null, pageId, null, null));
                }
            }
            if (dataManager.getSupportedFunctionalities().contains(Functionality.EFFICIENT_RETRIEVAL)) {
                result
                    .add(new CompletionProposal("", offset, 0, 0, null, "Type to Constrain Proposals...", null, null));
            }
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
