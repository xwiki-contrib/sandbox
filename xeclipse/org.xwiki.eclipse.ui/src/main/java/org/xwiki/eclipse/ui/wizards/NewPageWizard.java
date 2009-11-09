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
package org.xwiki.eclipse.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.ui.editors.PageEditor;
import org.xwiki.eclipse.ui.editors.PageEditorInput;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class NewPageWizard extends Wizard implements INewWizard
{
    private NewPageWizardState newPageWizardState;

    private DataManager dataManager;

    public NewPageWizard(DataManager dataManager, String spaceKey)
    {
        super();
        newPageWizardState = new NewPageWizardState();
        newPageWizardState.setSpace(spaceKey);
        this.dataManager = dataManager;
        setNeedsProgressMonitor(true);
    }

    @Override
    public boolean performFinish()
    {
        try {
            getContainer().run(true, false, new IRunnableWithProgress()
            {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try {
                        monitor.beginTask("Creating page...", IProgressMonitor.UNKNOWN);
                        final XWikiEclipsePage page =
                            dataManager.createPage(newPageWizardState.getSpace(), newPageWizardState.getName(),
                                newPageWizardState.getTitle(), "Write here content");

                        Display.getDefault().syncExec(new Runnable()
                        {
                            public void run()
                            {
                                SafeRunner.run(new XWikiEclipseSafeRunnable()
                                {
                                    public void run() throws Exception
                                    {
                                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                            .openEditor(new PageEditorInput(page, false), PageEditor.ID);
                                    }
                                });
                            }
                        });

                        monitor.done();
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            WizardPage currentPage = (WizardPage) getContainer().getCurrentPage();
            currentPage.setErrorMessage("Error creating remote page.");

            return false;
        }

        return true;
    }

    @Override
    public void addPages()
    {
        addPage(new PageSettingsPage("Page settings"));
    }

    public NewPageWizardState getNewPageWizardState()
    {
        return newPageWizardState;
    }

    @Override
    public boolean canFinish()
    {
        if (!super.canFinish()) {
            return false;
        }

        if (newPageWizardState.getSpace() == null || newPageWizardState.getSpace().length() == 0) {
            return false;
        }

        if (newPageWizardState.getName() == null || newPageWizardState.getName().length() == 0) {
            return false;
        }

        if (newPageWizardState.getTitle() == null || newPageWizardState.getTitle().length() == 0) {
            return false;
        }

        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        // Do nothing.
    }

    public DataManager getDataManager()
    {
        return dataManager;
    }
}
