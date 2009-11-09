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
package org.xwiki.eclipse.ui.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.model.ModelObject;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.workingsets.XWikiEclipseElementId;

public class UIUtils
{
    public static Set getSelectedObjectsFromSelection(Object selection)
    {
        Set result = new HashSet();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (!structuredSelection.isEmpty()) {
                Iterator iterator = structuredSelection.iterator();
                while (iterator.hasNext()) {
                    result.add(iterator.next());
                }
            }
        }

        return result;
    }

    public static Object getFirstSelectedObjectsFromSelection(Object selection)
    {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (!structuredSelection.isEmpty()) {
                return structuredSelection.getFirstElement();
            }
        }

        return null;
    }

    public static void runWithProgress(IRunnableWithProgress operation, Shell shell) throws InvocationTargetException,
        InterruptedException
    {
        runWithProgress(operation, shell, false);
    }

    public static void runWithProgress(IRunnableWithProgress operation, Shell shell, boolean cancelable)
        throws InvocationTargetException, InterruptedException
    {
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
        dialog.setCancelable(cancelable);
        dialog.run(true, false, operation);
    }

    public static void showMessageDialog(Shell shell, String title, String message)
    {
        showMessageDialog(shell, SWT.ICON_INFORMATION, title, message);
    }

    public static void showMessageDialog(Shell shell, int style, String title, String message)
    {
        MessageBox mb = new MessageBox(shell, style | SWT.APPLICATION_MODAL);
        mb.setText(title);
        mb.setMessage(message);
        mb.open();
    }

    public static boolean isXWikiEcipseIdInWorkingSet(String xwikiEclipseId, IWorkingSet workingSet)
    {
        if (workingSet == null) {
            return false;
        }

        for (Object object : workingSet.getElements()) {
            if (object instanceof XWikiEclipseElementId) {
                XWikiEclipseElementId element = (XWikiEclipseElementId) object;
                if (element.getXwikiEclipseId().equals(xwikiEclipseId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Object[] filterByWorkingSet(Object[] objects, IWorkingSet workingSet)
    {
        if (workingSet == null) {
            return objects;
        }

        Set result = new HashSet();
        for (Object object : objects) {
            if (isInWorkingSet(object, workingSet)) {
                result.add(object);
            }
        }

        return result.toArray();
    }

    public static boolean isInWorkingSet(Object object, IWorkingSet workingSet)
    {
        if (object instanceof DataManager) {
            DataManager dataManager = (DataManager) object;
            if (UIUtils.isXWikiEcipseIdInWorkingSet(dataManager.getXWikiEclipseId(), workingSet)) {
                return true;
            }
        }

        if (object instanceof ModelObject) {
            ModelObject modelObject = (ModelObject) object;
            if (UIUtils.isXWikiEcipseIdInWorkingSet(modelObject.getXWikiEclipseId(), workingSet)) {
                return true;
            }
        }

        return false;
    }

    public static void setStatusMessage(String message, boolean isErrorMessage)
    {
        if (isErrorMessage)
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(UIConstants.NAVIGATOR_VIEW_ID).getViewSite().getActionBars().getStatusLineManager()
                .setErrorMessage(message);
        if (!isErrorMessage)
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(UIConstants.NAVIGATOR_VIEW_ID).getViewSite().getActionBars().getStatusLineManager()
                .setMessage(message);
    }

    public static void setStatusMessage(String message, boolean isErrorMessage, Image image)
    {
        if (isErrorMessage)
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(UIConstants.NAVIGATOR_VIEW_ID).getViewSite().getActionBars().getStatusLineManager()
                .setErrorMessage(image, message);
        if (!isErrorMessage)
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(UIConstants.NAVIGATOR_VIEW_ID).getViewSite().getActionBars().getStatusLineManager()
                .setMessage(image, message);
    }

    public static void setStatusMessage(String message)
    {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(UIConstants.NAVIGATOR_VIEW_ID)
            .getViewSite().getActionBars().getStatusLineManager().setMessage(message);
    }

}
