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
import org.xwiki.eclipse.core.model.XWikiEclipseObject;
import org.xwiki.eclipse.ui.editors.ObjectEditor;
import org.xwiki.eclipse.ui.editors.ObjectEditorInput;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class NewObjectWizard extends Wizard implements INewWizard
{
    private NewObjectWizardState newObjectWizardState;

    private DataManager dataManager;

    public NewObjectWizard(DataManager dataManager, String pageId)
    {
        super();
        newObjectWizardState = new NewObjectWizardState();
        newObjectWizardState.setPageId(pageId);
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
                        monitor.beginTask("Creating object...", IProgressMonitor.UNKNOWN);
                        final XWikiEclipseObject object =
                            dataManager.createObject(newObjectWizardState.getPageId(), newObjectWizardState
                                .getClassName());

                        Display.getDefault().syncExec(new Runnable()
                        {
                            public void run()
                            {
                                SafeRunner.run(new XWikiEclipseSafeRunnable()
                                {
                                    public void run() throws Exception
                                    {
                                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                            .openEditor(new ObjectEditorInput(object), ObjectEditor.ID);
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
        addPage(new ObjectSettingsPage("Object settings", dataManager));
    }

    public NewObjectWizardState getNewObjectWizardState()
    {
        return newObjectWizardState;
    }

    @Override
    public boolean canFinish()
    {
        if (!super.canFinish()) {
            return false;
        }

        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        // Do nothing.
    }
}
