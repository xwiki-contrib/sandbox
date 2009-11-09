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
package org.xwiki.eclipse.ui.actions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import org.codehaus.swizzle.confluence.Attachment;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.xwiki.eclipse.core.CoreLog;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseObject;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.editors.ObjectEditor;
import org.xwiki.eclipse.ui.editors.ObjectEditorInput;
import org.xwiki.eclipse.ui.editors.PageEditor;
import org.xwiki.eclipse.ui.editors.PageEditorInput;
import org.xwiki.eclipse.ui.utils.UIUtils;

/*
 * This is defined as a standard action and not with the command framework because the common
 * navigator does not export a command with the ICommonActionConstants.OPEN id. So in order to make
 * double click work we need to do things in this way.
 */
public class OpenXWikiModelObjectAction extends Action
{
    private ISelectionProvider selectionProvider;

    public OpenXWikiModelObjectAction(ISelectionProvider selectionProvider)
    {
        super("Open...");
        this.selectionProvider = selectionProvider;
    }

    public static void run(Object[] selectedObjects)
    {
        for (Object object : selectedObjects) {
            if (object instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) object;

                try {
                    XWikiEclipsePage page = pageSummary.getDataManager().getPage(pageSummary.getData().getId());

                    if (page == null) {
                        UIUtils
                            .showMessageDialog(
                                Display.getDefault().getActiveShell(),
                                "Page not avaliable",
                                "The page is not currently available. This might happen if the page has been removed remotely or if the page is not locally available.");

                        return;
                    }

                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new PageEditorInput(page, false), PageEditor.ID);
                } catch (XWikiEclipseException e) {
                    UIUtils
                        .showMessageDialog(
                            Display.getDefault().getActiveShell(),
                            SWT.ICON_ERROR,
                            "Error opening page.",
                            "There was a communication error while opening the page. XWiki Eclipse is taking the connection offline in order to prevent further errors. Please check your remote XWiki status and then try to reconnect.");

                    CoreLog.logError("Error opening page", e);

                    pageSummary.getDataManager().disconnect();
                } catch (PartInitException e) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), "Error opening editor",
                        "There was an error while opening the editor.");
                }
            }

            if (object instanceof XWikiEclipseObjectSummary) {
                final XWikiEclipseObjectSummary objectSummary = (XWikiEclipseObjectSummary) object;

                try {
                    XWikiEclipseObject xwikiObject =
                        objectSummary.getDataManager().getObject(objectSummary.getData().getPageId(),
                            objectSummary.getData().getClassName(), objectSummary.getData().getId());

                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                        new ObjectEditorInput(xwikiObject), ObjectEditor.ID);
                } catch (XWikiEclipseException e) {
                    UIUtils
                        .showMessageDialog(
                            Display.getDefault().getActiveShell(),
                            SWT.ICON_ERROR,
                            "Error getting the object.",
                            "There was a communication error while getting the object. XWiki Eclipse is taking the connection offline in order to prevent further errors. Please check your remote XWiki status and then try to reconnect.");

                    CoreLog.logError("Error getting object", e);

                    objectSummary.getDataManager().disconnect();
                } catch (PartInitException e) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), "Error opening editor",
                        "There was an error while opening the editor.");
                }

            }

            if (object instanceof XWikiEclipseAttachmentSummary) {
                final XWikiEclipseAttachmentSummary attachmentSummary = (XWikiEclipseAttachmentSummary) object;

                try {
                    final Attachment attachment = attachmentSummary.getData();
                    final String url = attachment.getUrl();

                    String tmpDir = File.createTempFile("xeclipse", "").getParent();
                    final File file = new File(tmpDir + "/" + attachment.getFileName());
                    if (file.exists()) {
                        file.delete();
                    }
                    PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress()
                    {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                            InterruptedException
                        {
                            try {
                                URL attachmentURL = new URL(url);
                                URLConnection attachmentConnection = attachmentURL.openConnection();
                                BufferedInputStream bis =
                                    new BufferedInputStream(attachmentConnection.getInputStream());
                                file.createNewFile();
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                                byte[] bytes = new byte[1024];
                                int count = 0;
                                while ((count = bis.read(bytes)) != -1) {
                                    bos.write(bytes, 0, count);
                                }
                                bis.close();
                                bos.close();
                            } catch (FileNotFoundException e) {
                                UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), SWT.ICON_ERROR,
                                    "File Error.",
                                    "There was an error with Opening/Creating a temporary file on your local system.");
                            } catch (MalformedURLException e) {
                                UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), SWT.ICON_ERROR,
                                    "URL Error.", "There was error with URL/Locating URL of the Attachment file.");
                            } catch (IOException e) {
                                UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), SWT.ICON_ERROR,
                                    "File Error.",
                                    "There was an error with Opening/Creating a temporary file on your local system.");
                            }

                        };
                    });
                    // FIXME: Not sure of LocalXWikiStorage Structure. Will have to integrate.
                    IProject project = attachmentSummary.getDataManager().getProject();
                    IPath location = new Path(file.getPath());
                    IFile iFile = project.getFile(location.lastSegment());
                    if (!iFile.exists()) {
                        iFile.createLink(location, IResource.NONE, null);
                    }
                    FileEditorInput fileEditorInput = new FileEditorInput(iFile);

                    if (PlatformUI.getWorkbench().getEditorRegistry().isSystemInPlaceEditorAvailable(file.getName())) {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                            fileEditorInput, IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);

                    } else if (PlatformUI.getWorkbench().getEditorRegistry().isSystemExternalEditorAvailable(
                        iFile.getName())) {

                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(
                            fileEditorInput, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
                    } else {
                        System.out.println("No viewer available for this file type.  You can download it instead.");
                    }

                } catch (IOException e) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), SWT.ICON_ERROR, "File Error.",
                        "There was an error with Opening/Creating a temporary file on your local system.");
                } catch (CoreException e) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), SWT.ICON_ERROR,
                        "Error Opening Editor.", "There was an error in opening the External/Internal Editor.");
                } catch (Exception e) {
                    e.printStackTrace();
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), SWT.ICON_ERROR, "Internal Error.",
                        "Please file a bug report. http://jira.xwiki.org");
                }

            }

        }
    }

    @Override
    public void run()
    {
        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selectionProvider.getSelection());
        run(selectedObjects.toArray());
    }

    public static void run(ISelection selection)
    {
        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);
        run(selectedObjects.toArray());
    }
}
