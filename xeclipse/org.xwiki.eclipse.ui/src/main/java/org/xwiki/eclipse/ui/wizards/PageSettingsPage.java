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

public class PageSettingsPage extends WizardPage
{
    private NewPageWizardState newPageWizardState;

    private Text spaceText;

    private Text nameText;

    private Text titleText;

    public PageSettingsPage(String pageName)
    {
        super(pageName);
        setTitle("New page");
        setImageDescriptor(UIPlugin.getImageDescriptor(UIConstants.PAGE_SETTINGS_BANNER));
    }

    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        newPageWizardState = ((NewPageWizard) getWizard()).getNewPageWizardState();

        Group group = new Group(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).applyTo(group);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);
        group.setText("Page settings");

        /* Page space */
        Label label = new Label(group, SWT.NONE);
        label.setText("Space:");

        spaceText = new Text(group, SWT.BORDER);
        if (newPageWizardState.getSpace() != null) {
            spaceText.setText(newPageWizardState.getSpace());
        }
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(spaceText);
        spaceText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newPageWizardState.setSpace(spaceText.getText());
                getContainer().updateButtons();
            }
        });

        /* Page name */
        label = new Label(group, SWT.NONE);
        label.setText("Name:");

        nameText = new Text(group, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(nameText);
        nameText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newPageWizardState.setName(nameText.getText());
                getContainer().updateButtons();
            }
        });

        /* Page title */
        label = new Label(group, SWT.NONE);
        label.setText("Title:");

        titleText = new Text(group, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(titleText);
        titleText.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                newPageWizardState.setTitle(titleText.getText());
                getContainer().updateButtons();
            }
        });

        setControl(composite);
    }

    @Override
    public boolean isPageComplete()
    {
        String spaceTextString = spaceText.getText().trim();
        if (spaceTextString.length() == 0) {
            setErrorMessage("Space must be specified.");
            return false;
        }

        if (spaceTextString.contains(":") || spaceTextString.contains("?")) {
            setErrorMessage("Invalid characters in space name.");
            return false;
        }

        String nameTextString = nameText.getText().trim();
        if (nameText.getText().length() == 0) {
            setErrorMessage("Name must be specified.");
            return false;
        }

        if (nameTextString.contains(":") || nameTextString.contains("?") || nameTextString.contains(".")) {
            setErrorMessage("Invalid characters in page name.");
            return false;
        }

        String pageId = spaceTextString + "." + nameTextString;
        boolean exists = ((NewPageWizard) getWizard()).getDataManager().exists(pageId);
        if (exists) {
            setErrorMessage("That page already exists.");
            return false;
        }

        setErrorMessage(null);
        return true;
    }

}
