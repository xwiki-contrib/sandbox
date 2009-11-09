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
package org.xwiki.eclipse.ui.handlers;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.dialogs.RenameAttachmentDialog;
import org.xwiki.eclipse.ui.dialogs.RenamePageDialog;
import org.xwiki.eclipse.ui.utils.UIUtils;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class RenameHandler extends AbstractHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();
            if (selectedObject instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                if (!pageSummary.getDataManager().getSupportedFunctionalities().contains(Functionality.RENAME)) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), "Rename not supported",
                        "This data manager is connected to an XWiki that does not support page renaming.");

                    return null;
                }

                final RenamePageDialog dialog = new RenamePageDialog(HandlerUtil.getActiveShell(event), pageSummary);
                dialog.open();

                if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
                    SafeRunner.run(new XWikiEclipseSafeRunnable()
                    {
                        public void run() throws Exception
                        {
                            pageSummary.getDataManager().renamePage(pageSummary.getData().getId(),
                                dialog.getNewSpace(), dialog.getNewPageName());
                        }
                    });

                }
            }
            if (selectedObject instanceof XWikiEclipseAttachmentSummary) {
                final XWikiEclipseAttachmentSummary attachmentSummary = (XWikiEclipseAttachmentSummary) selectedObject;

                if (!attachmentSummary.getDataManager().getSupportedFunctionalities().contains(
                    Functionality.EFFICIENT_RENAME)) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), "Rename not supported",
                        "This data manager is connected to an XWiki that does not support attachment renaming.");

                    return null;
                }

                final RenameAttachmentDialog dialog =
                    new RenameAttachmentDialog(HandlerUtil.getActiveShell(event), attachmentSummary);
                dialog.open();

                if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
                    SafeRunner.run(new XWikiEclipseSafeRunnable()
                    {
                        public void run() throws Exception
                        {
                            attachmentSummary.getDataManager().renameAttachment(attachmentSummary,
                                dialog.getToFileName(), dialog.getToPageName(), dialog.getToSpace());
                        }
                    });

                }
            }
        }

        return null;
    }
}
