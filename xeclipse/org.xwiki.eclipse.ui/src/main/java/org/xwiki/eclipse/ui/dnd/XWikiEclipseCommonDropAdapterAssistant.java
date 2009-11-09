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
package org.xwiki.eclipse.ui.dnd;

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.ui.utils.UIUtils;

public class XWikiEclipseCommonDropAdapterAssistant extends CommonDropAdapterAssistant
{

    public XWikiEclipseCommonDropAdapterAssistant()
    {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter,
     * org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
     */
    @Override
    public IStatus handleDrop(CommonDropAdapter dropAdapter, DropTargetEvent dropTargetEvent, Object target)
    {
        dropAdapter.setExpandEnabled(true);
        dropAdapter.setFeedbackEnabled(true);
        dropAdapter.setScrollEnabled(true);
        dropAdapter.setScrollExpandEnabled(true);
        dropAdapter.setSelectionFeedbackEnabled(true);
        if (dropAdapter.getCurrentTarget() == null || dropTargetEvent.data == null) {
            UIUtils.setStatusMessage("Drop to Invalid Target", true);
            return Status.CANCEL_STATUS;
        }
        TransferData currentTransfer = dropAdapter.getCurrentTransfer();
        if (LocalSelectionTransfer.getTransfer().isSupportedType(currentTransfer)) {
            ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection StructuredSelection = (IStructuredSelection) selection;
                for (Iterator i = StructuredSelection.iterator(); i.hasNext();) {
                    Object o = i.next();
                    if (o instanceof XWikiEclipseAttachmentSummary) {
                        XWikiEclipseAttachmentSummary attachmentSummary = (XWikiEclipseAttachmentSummary) o;
                        XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) target;
                        try {
                            if (dropTargetEvent.detail == DND.DROP_MOVE) {
                                if (!attachmentSummary.getPageSummary().getId().equals(pageSummary.getData().getId())) {
                                    pageSummary.getDataManager()
                                        .renameAttachment(attachmentSummary, attachmentSummary.getData().getFileName(),
                                            pageSummary.getData().getId().split("\\.")[1],
                                            pageSummary.getData().getSpace());
                                }
                            }
                            if (dropTargetEvent.detail == DND.DROP_COPY) {
                                if (!attachmentSummary.getPageSummary().getId().equals(pageSummary.getData().getId())) {
                                    pageSummary.getDataManager().copyAttachment(attachmentSummary,
                                        pageSummary.getData().getId());
                                } else {
                                    pageSummary.getDataManager().copyAttachment(attachmentSummary,
                                        pageSummary.getData().getId(),
                                        "Copy of " + attachmentSummary.getData().getFileName());
                                }
                            }
                        } catch (XWikiEclipseException e) {
                            e.printStackTrace();
                            UIUtils.setStatusMessage("Internal Error", true);
                            return Status.CANCEL_STATUS;
                        }
                    }
                    if (o instanceof XWikiEclipsePageSummary) {
                        XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) o;
                        XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) target;
                        try {
                            if (dropTargetEvent.detail == DND.DROP_MOVE) {
                                if (!pageSummary.getData().getSpace().equals(spaceSummary.getData().getKey())) {
                                    spaceSummary.getDataManager().renamePage(pageSummary.getData().getId(),
                                        spaceSummary.getData().getKey(), pageSummary.getData().getId().split("\\.")[1]);
                                }
                            }
                            if (dropTargetEvent.detail == DND.DROP_COPY) {
                                if (!pageSummary.getData().getSpace().equals(spaceSummary.getData().getKey())) {
                                    spaceSummary.getDataManager().copyPage(
                                        pageSummary,
                                        spaceSummary.getData().getKey() + "."
                                            + pageSummary.getData().getId().split("\\.")[1]);
                                } else {
                                    spaceSummary.getDataManager().copyPage(
                                        pageSummary,
                                        spaceSummary.getData().getKey() + ".CopyOf"
                                            + pageSummary.getData().getId().split("\\.")[1]);
                                }
                            }
                        } catch (XWikiEclipseException e) {
                            e.printStackTrace();
                            UIUtils.setStatusMessage("Internal Error", true);
                            return Status.CANCEL_STATUS;
                        }
                    }
                }

            }

        }
        return Status.OK_STATUS;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int,
     * org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public IStatus validateDrop(Object target, int operation, TransferData transferType)
    {
        if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
            if (target instanceof XWikiEclipsePageSummary) {
                if (!((XWikiEclipsePageSummary) target).getDataManager().getSupportedFunctionalities().contains(
                    Functionality.EFFICIENT_RENAME)) {
                    UIUtils
                        .setStatusMessage(
                            "Drag/Drop not Supported since XWiki Server connected to does not support Copy/Move Functionality",
                            true);
                    return Status.CANCEL_STATUS;
                }
                ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection StructuredSelection = (IStructuredSelection) selection;
                    for (Iterator i = StructuredSelection.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (!(o instanceof XWikiEclipseAttachmentSummary)) {
                            UIUtils.setStatusMessage("Only Attachments can be dropped on pages", true);
                            return Status.CANCEL_STATUS;
                        }
                        // Is XWikiEclipseAttachmentSummary by now
                        if (!((XWikiEclipseAttachmentSummary) o).getDataManager().getName().equals(
                            ((XWikiEclipsePageSummary) target).getDataManager().getName())) {
                            UIUtils.setStatusMessage("Both Attachment and Page should belong to same Wiki", true);
                            return Status.CANCEL_STATUS;
                        }
                    }
                    return Status.OK_STATUS;
                }
            }
            if (target instanceof XWikiEclipseSpaceSummary) {
                if (!((XWikiEclipseSpaceSummary) target).getDataManager().getSupportedFunctionalities().contains(
                    Functionality.EFFICIENT_RENAME))
                    return Status.CANCEL_STATUS;
                ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection StructuredSelection = (IStructuredSelection) selection;
                    for (Iterator i = StructuredSelection.iterator(); i.hasNext();) {
                        Object o = i.next();
                        if (!(o instanceof XWikiEclipsePageSummary)) {
                            UIUtils.setStatusMessage("Only Pages can be dropped on spaces", true);
                            return Status.CANCEL_STATUS;
                        }
                        // Is XWikiEclipsePageSummary by now
                        if (!((XWikiEclipsePageSummary) o).getDataManager().getName().equals(
                            ((XWikiEclipseSpaceSummary) target).getDataManager().getName())) {
                            UIUtils.setStatusMessage("Both Page and Space should belong to same Wiki", true);
                            return Status.CANCEL_STATUS;
                        }
                    }
                    return Status.OK_STATUS;
                }
            }
        }
        return Status.CANCEL_STATUS;
    }
}
