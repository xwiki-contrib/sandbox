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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;

public class ConnectionSettingsWizardPage extends WizardPage
{
    private NewConnectionWizardState newConnectionWizardState;

    private Text connectionNameText;

    private Text serverUrlText;

    private Text userNameText;

    private Text passwordText;

    protected ConnectionSettingsWizardPage(String pageName)
    {
        super(pageName);
        setTitle("XWiki connection settings");
        setImageDescriptor(UIPlugin.getImageDescriptor(UIConstants.CONNECTION_SETTINGS_BANNER));
    }

    public void createControl(Composite parent)
    {
        newConnectionWizardState = ((NewConnectionWizard) getWizard()).getNewConnectionWizardState();

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Connection name:");

        /* Connection name (this will be the corresponding project name in the workspace) */
        connectionNameText = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(connectionNameText);
        connectionNameText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newConnectionWizardState.setConnectionName(connectionNameText.getText().trim());
                getContainer().updateButtons();
            }
        });
        connectionNameText.setText("New connection");

        Group group = new Group(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).applyTo(group);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).span(2, 1).applyTo(group);
        group.setText("Connection settings");

        /* Server URL */
        label = new Label(group, SWT.NONE);
        label.setText("Server URL:");

        serverUrlText = new Text(group, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(serverUrlText);
        serverUrlText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newConnectionWizardState.setServerUrl(serverUrlText.getText().trim());
                getContainer().updateButtons();
            }
        });
        serverUrlText.setText("http://localhost:8080/xwiki/xmlrpc/confluence");

        /* Username */
        label = new Label(group, SWT.NONE);
        label.setText("Username:");

        userNameText = new Text(group, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(userNameText);
        userNameText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newConnectionWizardState.setUserName(userNameText.getText().trim());
                getContainer().updateButtons();
            }
        });

        /* Password */
        label = new Label(group, SWT.NONE);
        label.setText("Password:");

        passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(passwordText);
        passwordText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newConnectionWizardState.setPassword(passwordText.getText());
                getContainer().updateButtons();
            }
        });

        setControl(composite);
    }

    @Override
    public boolean isPageComplete()
    {
        String connectionName = connectionNameText.getText().trim();

        if (connectionName.length() == 0) {
            setErrorMessage("Connection name must be specified.");
            return false;
        }

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = workspaceRoot.getProject(connectionName);
        if (project.exists()) {
            setErrorMessage(String
                .format("Connection '%s' already exists. Please choose another name.", connectionName));
            return false;
        }

        if (serverUrlText.getText() == null || serverUrlText.getText().trim().length() == 0) {
            setErrorMessage("A server URL must be specified.");
            return false;
        }

        try {
            if (new URL(serverUrlText.getText().trim()).getHost().equals(""))
                throw new MalformedURLException("Invalid hostname.");
        } catch (MalformedURLException me) {
            setErrorMessage("The specified address is not a valid URL.");
            return false;
        }

        if (userNameText.getText() == null || userNameText.getText().trim().length() == 0) {
            setErrorMessage("User name must be specified.");
            return false;
        }

        if (passwordText.getText() == null || passwordText.getText().length() == 0) {
            setErrorMessage("User password must be specified.");
            return false;
        }

        /* Clear error messages if everything's fine. */
        setErrorMessage(null);

        return true;
    }
}
