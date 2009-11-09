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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

public class XWikiEclipseObjectSummaryActionProvider extends CommonActionProvider
{
    private Action open;

    public void init(final ICommonActionExtensionSite aSite)
    {
        open = new OpenXWikiModelObjectAction(aSite.getViewSite().getSelectionProvider());
    }

    public void fillContextMenu(IMenuManager menu)
    {
        super.fillContextMenu(menu);
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, open);
    }

    public void fillActionBars(IActionBars actionBars)
    {
        super.fillActionBars(actionBars);
        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, open);
    }
}
