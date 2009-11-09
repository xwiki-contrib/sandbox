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

import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.model.XWikiEclipseClassSummary;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnableWithResult;

public class ObjectSettingsPage extends WizardPage
{
    private NewObjectWizardState newObjectWizardState;

    private DataManager dataManager;

    private Combo combo;

    public ObjectSettingsPage(String pageName, DataManager dataManager)
    {
        super(pageName);
        setTitle("New object");
        setImageDescriptor(UIPlugin.getImageDescriptor(UIConstants.OBJECT_SETTINGS_BANNER));

        this.dataManager = dataManager;
    }

    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        newObjectWizardState = ((NewObjectWizard) getWizard()).getNewObjectWizardState();

        Group group = new Group(composite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(5, 5).applyTo(group);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);
        group.setText("Object settings");

        /* Page space */
        Label label = new Label(group, SWT.NONE);
        label.setText("Page:");

        label = new Label(group, SWT.NONE);
        label.setText(newObjectWizardState.getPageId());

        final XWikiEclipseSafeRunnableWithResult<List<XWikiEclipseClassSummary>> runnable =
            new XWikiEclipseSafeRunnableWithResult<List<XWikiEclipseClassSummary>>()
            {
                public void run() throws Exception
                {
                    setResult(dataManager.getClasses());
                }
            };
        SafeRunner.run(runnable);

        label = new Label(group, SWT.NONE);
        label.setText("Class:");

        combo = new Combo(group, SWT.READ_ONLY);
        for (XWikiEclipseClassSummary classSummary : runnable.getResult()) {
            combo.add(classSummary.getData().getId());
        }
        combo.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void widgetSelected(SelectionEvent e)
            {
                newObjectWizardState.setClassName(combo.getItem(combo.getSelectionIndex()));
                getContainer().updateButtons();

            }

        });

        setControl(composite);
    }

    @Override
    public boolean isPageComplete()
    {
        if (combo.getSelectionIndex() == -1) {
            setErrorMessage("A class must be selected");
            return false;
        }

        setErrorMessage(null);
        return true;
    }

}
