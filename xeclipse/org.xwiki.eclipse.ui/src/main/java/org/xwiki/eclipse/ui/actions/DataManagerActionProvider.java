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
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.xwiki.eclipse.ui.UIConstants;

public class DataManagerActionProvider extends CommonActionProvider
{
    private CommandContributionItem connect;

    private CommandContributionItem disconnect;

    private CommandContributionItem newPage;

    private CommandContributionItem newSpace;

    public void init(final ICommonActionExtensionSite aSite)
    {
        /* This is for Eclipse 3.4 */
        // CommandContributionItemParameter contributionItemParameter =
        // new CommandContributionItemParameter(PlatformUI.getWorkbench(),
        // UIConstants.DATA_MANAGER_CONNECT_COMMAND,
        // UIConstants.DATA_MANAGER_CONNECT_COMMAND,
        // 0);
        // connect = new CommandContributionItem(contributionItemParameter);
        connect =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.DATA_MANAGER_CONNECT_COMMAND,
                null, null, null, null, null, null, null, SWT.NONE);

        /* This is for Eclipse 3.4 */
        // contributionItemParameter =
        // new CommandContributionItemParameter(PlatformUI.getWorkbench(),
        // UIConstants.DATA_MANAGER_DISCONNECT_COMMAND,
        // UIConstants.DATA_MANAGER_DISCONNECT_COMMAND,
        // 0);
        // disconnect = new CommandContributionItem(contributionItemParameter);
        disconnect =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.DATA_MANAGER_DISCONNECT_COMMAND,
                null, null, null, null, null, null, null, SWT.NONE);

        /* This is for Eclipse 3.4 */
        // contributionItemParameter =
        // new CommandContributionItemParameter(PlatformUI.getWorkbench(),
        // UIConstants.NEW_PAGE_COMMAND,
        // UIConstants.NEW_PAGE_COMMAND,
        // 0);
        // newPage = new CommandContributionItem(contributionItemParameter);
        newPage =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.NEW_PAGE_COMMAND, null, null,
                null, null, null, null, null, SWT.NONE);

        newSpace =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.NEW_SPACE_COMMAND, null, null,
                null, null, null, null, null, SWT.NONE);

    }

    public void fillContextMenu(IMenuManager menu)
    {
        super.fillContextMenu(menu);
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, connect);
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, disconnect);
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, new Separator());
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, newSpace);
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, newPage);
    }
}
