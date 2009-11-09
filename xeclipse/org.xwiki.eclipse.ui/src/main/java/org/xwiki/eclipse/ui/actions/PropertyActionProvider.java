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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

public class PropertyActionProvider extends CommonActionProvider
{
    private PropertyDialogAction propertiesAction;

    private ISelectionProvider selectionProvider;

    public void init(final ICommonActionExtensionSite aSite)
    {
        selectionProvider = aSite.getViewSite().getSelectionProvider();

        propertiesAction = new PropertyDialogAction(new IShellProvider()
        {
            public Shell getShell()
            {
                return aSite.getViewSite().getShell();
            }
        }, selectionProvider);

        propertiesAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.PROPERTIES);
    }

    public void fillContextMenu(IMenuManager menu)
    {
        super.fillContextMenu(menu);
        menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES, propertiesAction);
    }

    public void fillActionBars(IActionBars actionBars)
    {
        super.fillActionBars(actionBars);
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertiesAction);
    }

    public void setContext(ActionContext context)
    {
        super.setContext(context);
        propertiesAction.selectionChanged(selectionProvider.getSelection());
    }
}
