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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.XWikiEclipseNature;
import org.xwiki.eclipse.ui.perspectives.XWikiPerspectiveFactory;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;

public class NewConnectionWizard extends Wizard implements INewWizard, IExecutableExtension
{
    /*
     * The Wizard's ID
     */
    public final static String WIZARD_ID = "org.xwiki.eclipse.ui.wizards.NewConnection";

    /*
     * The ConfigurationElement required to activate the XWiki Eclipse perspective after finishing creating the new
     * connection.
     */
    private IConfigurationElement config;

    private NewConnectionWizardState newConnectionWizardState;

    public NewConnectionWizard()
    {
        super();
        newConnectionWizardState = new NewConnectionWizardState();
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages()
    {
        addPage(new ConnectionSettingsWizardPage("Connection settings"));
    }

    @Override
    public boolean canFinish()
    {
        if (newConnectionWizardState.getConnectionName() == null
            || newConnectionWizardState.getConnectionName().length() == 0) {
            return false;
        }

        if (newConnectionWizardState.getServerUrl() == null || newConnectionWizardState.getServerUrl().length() == 0) {
            return false;
        }

        if (newConnectionWizardState.getUserName() == null || newConnectionWizardState.getUserName().length() == 0) {
            return false;
        }

        if (newConnectionWizardState.getPassword() == null || newConnectionWizardState.getPassword().length() == 0) {
            return false;
        }

        return super.canFinish();
    }

    @Override
    public boolean performFinish()
    {
        WizardPage currentPage = (WizardPage) getContainer().getCurrentPage();

        try {
            getContainer().run(true, false, new IRunnableWithProgress()
            {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try {
                        monitor.beginTask("Setting up connection", IProgressMonitor.UNKNOWN);

                        /* Try to login with the specified username + password */
                        XWikiXmlRpcClient client = new XWikiXmlRpcClient(newConnectionWizardState.getServerUrl());
                        client.login(newConnectionWizardState.getUserName(), newConnectionWizardState.getPassword());
                        client.logout();

                    } catch (Exception e) {
                        throw new InvocationTargetException(e, e.getMessage());
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (Exception e) {
            currentPage.setErrorMessage(String.format(
                "Error connecting to remote XWiki: '%s'. Please check your settings.", e.getMessage()));
            return false;
        }

        /* Create a workbench project containing connection data */
        try {
            getContainer().run(true, false, new IRunnableWithProgress()
            {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try {
                        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                        IProject project = workspaceRoot.getProject(newConnectionWizardState.getConnectionName());
                        if (project.exists()) {
                            return;
                        }

                        project.create(null);
                        project.open(null);

                        project.setPersistentProperty(DataManager.ENDPOINT, newConnectionWizardState.getServerUrl());
                        project.setPersistentProperty(DataManager.USERNAME, newConnectionWizardState.getUserName());
                        project.setPersistentProperty(DataManager.PASSWORD, newConnectionWizardState.getPassword());
                        project.setPersistentProperty(DataManager.AUTO_CONNECT, "true");

                        IProjectDescription description = project.getDescription();
                        description.setNatureIds(new String[] {XWikiEclipseNature.ID});
                        project.setDescription(description, null);

                        ResourcesPlugin.getWorkspace().save(true, new NullProgressMonitor());
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            currentPage.setErrorMessage(String.format("Error creating project data: '%s'.", e.getMessage()));
            return false;

        }

        if (!PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId().equals(
            XWikiPerspectiveFactory.PERSPECTIVE_ID)) {
            // Ask the user to switch to XWiki Eclipse perspective.
            BasicNewProjectResourceWizard.updatePerspective(config);
        }

        return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        // Empty method.
    }

    public NewConnectionWizardState getNewConnectionWizardState()
    {
        return newConnectionWizardState;
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
        throws CoreException
    {
        // Store the ConfigurationElement to use later when calling updatePerspective().
        this.config = config;
    }

}
