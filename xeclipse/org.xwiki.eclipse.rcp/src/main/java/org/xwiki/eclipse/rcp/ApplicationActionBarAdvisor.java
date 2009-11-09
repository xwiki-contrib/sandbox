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
package org.xwiki.eclipse.rcp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.views.IViewDescriptor;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
    private List<Action> showViewActions;

    private IWorkbenchAction preferencesAction;

    class ShowViewAction extends Action
    {
        public final String ID = "org.xwiki.xeclipse.actions.ShowView";

        private IWorkbenchWindow window;

        private IViewDescriptor viewDescriptor;

        public ShowViewAction(IWorkbenchWindow window, IViewDescriptor viewDescriptor)
        {
            this.window = window;
            this.viewDescriptor = viewDescriptor;

            setId(ID);
            setText(viewDescriptor.getLabel());
            setImageDescriptor(viewDescriptor.getImageDescriptor());
        }

        @Override
        public void run()
        {
            try {
                window.getActivePage().showView(viewDescriptor.getId());
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }

    }

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
    {
        super(configurer);
    }

    protected void makeActions(IWorkbenchWindow window)
    {
        IWorkbenchAction newAction = ActionFactory.NEW.create(window);
        register(newAction);

        IWorkbenchAction saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        IWorkbenchAction cutAction = ActionFactory.CUT.create(window);
        register(cutAction);

        IWorkbenchAction copyAction = ActionFactory.COPY.create(window);
        register(copyAction);

        IWorkbenchAction pasteAction = ActionFactory.PASTE.create(window);
        register(pasteAction);

        IWorkbenchAction exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        IWorkbenchAction showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(window);
        register(showViewMenuAction);

        preferencesAction = ActionFactory.PREFERENCES.create(window);
        register(preferencesAction);

        showViewActions = new ArrayList<Action>();

        for (IViewDescriptor v : window.getWorkbench().getViewRegistry().getViews()) {
            String[] categoryPath = v.getCategoryPath();

            boolean addView = false;
            if (categoryPath != null) {
                for (String category : categoryPath) {
                    if (category.equalsIgnoreCase("org.xwiki.eclipse")) {
                        addView = true;
                        break;
                    }
                }
            }

            if (v.getId().equals("org.eclipse.pde.runtime.LogView")) {
                addView = true;
            }

            if (v.getId().equals("org.eclipse.ui.views.ContentOutline")) {
                addView = true;
            }

            if (addView) {
                ShowViewAction action = new ShowViewAction(window, v);
                showViewActions.add(action);
                register(action);
            }
        }

        IWorkbenchAction deleteAction = ActionFactory.DELETE.create(window);
        register(deleteAction);
    }

    protected void fillMenuBar(IMenuManager menuBar)
    {
        MenuManager fileMenu = new MenuManager("File", "org.xwiki.xeclipse.menu.File");
        menuBar.add(fileMenu);

        MenuManager editMenu = new MenuManager("Edit", "org.xwiki.xeclipse.menu.Edit");
        menuBar.add(editMenu);

        MenuManager windowMenu = new MenuManager("Window", "org.xwiki.xeclipse.menu.Window");

        MenuManager showViewMenu = new MenuManager("Show view", "org.xwiki.xeclipse.menu.ShowView");

        for (Action action : showViewActions) {
            showViewMenu.add(action);
        }

        windowMenu.add(showViewMenu);
        windowMenu.add(preferencesAction);

        menuBar.add(windowMenu);

        MenuManager helpMenu = new MenuManager("Help", "org.xwiki.xeclipse.menu.Help");
        menuBar.add(helpMenu);

    }

}
