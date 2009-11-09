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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import org.codehaus.swizzle.confluence.Attachment;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.utils.CoreUtils;
import org.xwiki.eclipse.ui.utils.UIUtils;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class NewAttachmentHandler extends AbstractHandler
{

    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();
            if (selectedObject instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                FileDialog dialog =
                    new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SINGLE);
                dialog.setText("Choose File");
                dialog.open();
                final String name = dialog.getFileName();
                String filterpath = dialog.getFilterPath() + "/";
                try {
                    final byte[] data = CoreUtils.getBytesFromFile(new File(filterpath + name));

                    SafeRunner.run(new XWikiEclipseSafeRunnable()
                    {
                        public void run() throws Exception
                        {

                            HashMap map = new HashMap(10);
                            map.put("pageId", pageSummary.getData().getId());
                            map.put("fileName", name);
                            map.put("comment", ""); // FIXME: Wizard later to support comments, detectContentType.
                            final Attachment attachment = new Attachment(map);
                            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress()
                            {
                                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                                    InterruptedException
                                {

                                    try {
                                        // FIXME: for Large Attachments, xmlrpc addAttachment() takes 4 times the
                                        // memory. hence InvocationError
                                        Attachment returned =
                                            pageSummary.getDataManager().addAttachment(attachment, data, pageSummary);
                                    } catch (XWikiEclipseException e) {
                                        e.printStackTrace();
                                    }
                                };
                            });
                        }
                    });
                } catch (IOException e) {
                    UIUtils.showMessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), e
                        .getLocalizedMessage(), "There was a problem in reading/accessing the file.");
                } catch (Exception e) {
                    e.printStackTrace();
                    UIUtils.showMessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "Internal error.", "Please file a bug report. http://jira.xwiki.org");
                }

            }
        }

        return null;
    }
}
