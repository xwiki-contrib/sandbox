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
package org.xwiki.eclipse.ui.properties;

import java.util.Formatter;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class DataManagerPropertiesPage extends PropertyPage
{
    private DataManager dataManager;

    private Text endpointText;

    private Text userNameText;

    private Text passwordText;

    private Button autoConnect;

    public DataManagerPropertiesPage()
    {
        super();
    }

    protected Control createContents(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        SafeRunner.run(new XWikiEclipseSafeRunnable()
        {
            public void run() throws Exception
            {
                dataManager = (DataManager) getElement().getAdapter(DataManager.class);

                Label label = new Label(composite, SWT.BORDER);
                label.setText("Name:");
                label = new Label(composite, SWT.BORDER);
                label.setText(dataManager.getName());

                label = new Label(composite, SWT.BORDER);
                label.setText("Status:");
                label = new Label(composite, SWT.BORDER);
                if (dataManager.isConnected()) {
                    Formatter f = new Formatter();
                    for (Functionality functionality : dataManager.getSupportedFunctionalities()) {
                        f.format("%s ", functionality);
                    }
                    label.setText(String.format("Connected. Support for: %s", f.toString()));
                } else {
                    label.setText("Not connected");
                }

                label = new Label(composite, SWT.BORDER);
                label.setText("Endpoint:");
                endpointText = new Text(composite, SWT.BORDER);
                GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(endpointText);
                String endpoint = dataManager.getEndpoint();
                endpointText.setText(endpoint != null ? endpoint : "No endpoint defined");

                label = new Label(composite, SWT.BORDER);
                label.setText("User name:");
                userNameText = new Text(composite, SWT.BORDER);
                GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(userNameText);
                String userName = dataManager.getUserName();
                userNameText.setText(userName != null ? userName : "No username defined");

                label = new Label(composite, SWT.BORDER);
                label.setText("Password:");
                passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
                GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(passwordText);
                String password = dataManager.getPassword();
                passwordText.setText(password != null ? password : "");

                label = new Label(composite, SWT.NONE);
                autoConnect = new Button(composite, SWT.CHECK);
                autoConnect.setText("Auto connect");
                GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(autoConnect);
                autoConnect.setSelection(dataManager.isAutoConnect());

            }

        });

        return composite;
    }

    protected void performDefaults()
    {
        SafeRunner.run(new XWikiEclipseSafeRunnable()
        {
            public void run() throws Exception
            {
                String endpoint = dataManager.getEndpoint();
                endpointText.setText(endpoint != null ? endpoint : "No endpoint defined");
                String userName = dataManager.getUserName();
                userNameText.setText(userName != null ? userName : "No username defined");
                String password = dataManager.getPassword();
                passwordText.setText(password != null ? password : "");
                autoConnect.setSelection(dataManager.isAutoConnect());
            }
        });
    }

    public boolean performOk()
    {
        SafeRunner.run(new XWikiEclipseSafeRunnable()
        {
            public void run() throws Exception
            {
                dataManager.setEndpoint(endpointText.getText());
                dataManager.setUserName(userNameText.getText());
                dataManager.setPassword(passwordText.getText());
                dataManager.setAutoConnect(autoConnect.getSelection());
            }
        });

        return true;
    }

}
