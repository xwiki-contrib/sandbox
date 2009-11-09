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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.utils.UIUtils;
import org.xwiki.eclipse.ui.wizards.NewObjectWizard;

public class NewObjectHandler extends AbstractHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();
            if (selectedObject instanceof XWikiEclipsePageSummary) {
                XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                if (!pageSummary.getDataManager().getSupportedFunctionalities().contains(Functionality.OBJECTS)) {
                    UIUtils.showMessageDialog(Display.getDefault().getActiveShell(), "Objects not supported",
                        "This data manager is connected to an XWiki that does not support object management.");

                    return null;
                }

                NewObjectWizard wizard =
                    new NewObjectWizard(pageSummary.getDataManager(), pageSummary.getData().getId());

                WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), wizard);
                dialog.create();
                dialog.open();
            }
        }

        return null;
    }

}
