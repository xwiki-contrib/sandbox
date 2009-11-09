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

import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.core.model.XWikiEclipsePageHistorySummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.editors.PageEditor;
import org.xwiki.eclipse.ui.editors.PageEditorInput;
import org.xwiki.eclipse.ui.utils.UIUtils;

public class XWikiEclipsePageSummaryActionProvider extends CommonActionProvider
{
    private Action open;

    private CommandContributionItem newObject;

    private CommandContributionItem renamePage;

    private CommandContributionItem newAttachment;

    private ISelectionProvider selectionProvider;

    public void init(final ICommonActionExtensionSite aSite)
    {
        selectionProvider = aSite.getViewSite().getSelectionProvider();

        open = new OpenXWikiModelObjectAction(selectionProvider);

        // CommandContributionItemParameter contributionItemParameter =
        // new CommandContributionItemParameter(PlatformUI.getWorkbench(),
        // UIConstants.NEW_OBJECT_COMMAND,
        // UIConstants.NEW_OBJECT_COMMAND,
        // 0);
        // newObject = new CommandContributionItem(contributionItemParameter);

        newObject =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.NEW_OBJECT_COMMAND, null, null,
                null, null, null, null, null, SWT.NONE);

        newAttachment =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.NEW_ATTACHMENT_COMMAND, null,
                null, null, null, null, null, null, SWT.NONE);

        renamePage =
            new CommandContributionItem(PlatformUI.getWorkbench(), null, UIConstants.RENAME_COMMAND, null, null,
                null, null, null, null, null, SWT.NONE);

    }

    public void fillContextMenu(IMenuManager menu)
    {
        super.fillContextMenu(menu);
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, open);

        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, getTranslationMenu());
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, getHistoryMenu());

        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, new Separator());
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, newObject);
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, newAttachment);
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, new Separator());
        menu.appendToGroup(ICommonMenuConstants.GROUP_ADDITIONS, renamePage);

    }

    private IMenuManager getHistoryMenu()
    {
        MenuManager menuManager = new MenuManager("Open version...");

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selectionProvider.getSelection());
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();

            if (selectedObject instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                try {
                    List<XWikiEclipsePageHistorySummary> pageHistory =
                        pageSummary.getDataManager().getPageHistory(pageSummary.getData().getId());
                    for (XWikiEclipsePageHistorySummary pageHistorySummary : pageHistory) {
                        menuManager.add(new OpenPageHistoryItemAction(pageHistorySummary));
                    }
                } catch (XWikiEclipseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return menuManager;
    }

    private IMenuManager getTranslationMenu()
    {
        MenuManager menuManager = new MenuManager("Open translation...");

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selectionProvider.getSelection());
        if (selectedObjects.size() == 1) {
            Object selectedObject = selectedObjects.iterator().next();

            if (selectedObject instanceof XWikiEclipsePageSummary) {
                final XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) selectedObject;

                if (!pageSummary.getDataManager().getSupportedFunctionalities().contains(Functionality.TRANSLATIONS)) {
                    return menuManager;
                }

                if (pageSummary.getData().getTranslations() != null) {
                    for (String language : pageSummary.getData().getTranslations()) {
                        menuManager.add(new OpenPageTranslationAction(pageSummary, language));
                    }

                    /* TODO: Checkout the server side. This gives non-deterministic results */
                    menuManager.add(new Separator());

                    menuManager.add(new Action("New translation...")
                    {
                        @Override
                        public void run()
                        {
                            InputDialog inputDialog =
                                new InputDialog(Display.getDefault().getActiveShell(), "Translation", "Translation",
                                    "", null);
                            inputDialog.open();

                            if (inputDialog.getReturnCode() == InputDialog.OK) {
                                if (!inputDialog.getValue().equals("")) {
                                    String[] components = pageSummary.getData().getId().split("\\.");

                                    try {
                                        XWikiEclipsePage page =
                                            pageSummary.getDataManager().createPage(components[0], components[1],
                                                pageSummary.getData().getTitle(), inputDialog.getValue(),
                                                "Write translation here");
                                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                            .openEditor(new PageEditorInput(page, false), PageEditor.ID);
                                    } catch (XWikiEclipseException e) {
                                        e.printStackTrace();
                                    } catch (PartInitException e) {

                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }

        return menuManager;
    }

    public void fillActionBars(IActionBars actionBars)
    {
        super.fillActionBars(actionBars);
        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, open);
    }
}
