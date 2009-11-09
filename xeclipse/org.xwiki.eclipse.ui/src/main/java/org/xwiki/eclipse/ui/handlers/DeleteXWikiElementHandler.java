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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.DataManagerRegistry;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.ui.dialogs.SelectionDialog;
import org.xwiki.eclipse.ui.utils.UIUtils;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class DeleteXWikiElementHandler extends AbstractHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        final Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);

        SelectionDialog selectionDialog =
            new SelectionDialog(HandlerUtil.getActiveShell(event), "Delete objects",
                "Review the objects to be deleted", selectedObjects);
        int result = selectionDialog.open();
        if (result == IDialogConstants.CANCEL_ID) {
            return null;
        }

        final Set<Object> objectsToBeRemoved = selectionDialog.getSelectedObjects();

        IRunnableWithProgress progess = new IRunnableWithProgress()
        {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                monitor.beginTask("Delete Task", selectedObjects.size());
                for (Object selectedObject : selectedObjects) {
                    if (objectsToBeRemoved.contains(selectedObject)) {
                        if (selectedObject instanceof XWikiEclipsePageSummary) {
                            final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                            SafeRunner.run(new XWikiEclipseSafeRunnable()
                            {
                                public void run() throws Exception
                                {
                                    pageSummary.getDataManager().removePage(pageSummary.getData().getId());
                                }
                            });

                        }

                        if (selectedObject instanceof XWikiEclipseObjectSummary) {
                            final XWikiEclipseObjectSummary objectSummary = (XWikiEclipseObjectSummary) selectedObject;

                            SafeRunner.run(new XWikiEclipseSafeRunnable()
                            {
                                public void run() throws Exception
                                {
                                    objectSummary.getDataManager().removeObject(objectSummary.getData().getPageId(),
                                        objectSummary.getData().getClassName(), objectSummary.getData().getId());
                                }
                            });

                        }

                        if (selectedObject instanceof DataManager) {
                            final DataManager dataManager = (DataManager) selectedObject;

                            SafeRunner.run(new XWikiEclipseSafeRunnable()
                            {
                                public void run() throws Exception
                                {
                                    DataManagerRegistry.getDefault().unregister(dataManager);
                                    dataManager.getProject().delete(true, null);
                                    ResourcesPlugin.getWorkspace().save(true, new NullProgressMonitor());
                                }
                            });

                        }

                        if (selectedObject instanceof XWikiEclipseSpaceSummary) {
                            final XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) selectedObject;

                            SafeRunner.run(new XWikiEclipseSafeRunnable()
                            {
                                public void run() throws Exception
                                {
                                    spaceSummary.getDataManager().removeSpace(spaceSummary.getData().getKey());
                                }
                            });
                        }

                        if (selectedObject instanceof XWikiEclipseAttachmentSummary) {
                            final XWikiEclipseAttachmentSummary attachmentSummary =
                                (XWikiEclipseAttachmentSummary) selectedObject;

                            SafeRunner.run(new XWikiEclipseSafeRunnable()
                            {
                                public void run() throws Exception
                                {
                                    attachmentSummary.getDataManager().removeAttachment(attachmentSummary);
                                }
                            });
                        }
                    }
                    monitor.worked(1);
                }
                monitor.done();
            }
        };

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(progess);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
