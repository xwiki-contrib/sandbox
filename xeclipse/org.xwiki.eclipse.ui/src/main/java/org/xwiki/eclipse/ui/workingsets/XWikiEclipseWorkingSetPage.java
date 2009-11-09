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
package org.xwiki.eclipse.ui.workingsets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.model.ModelObject;
import org.xwiki.eclipse.ui.NavigatorContentProvider;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.utils.UIUtils;

public class XWikiEclipseWorkingSetPage extends WizardPage implements IWorkingSetPage
{
    private IWorkingSet workingSet;

    private WorkingSetCheckboxTreeViewer treeViewer;

    private Text workingSetName;

    private boolean isPageComplete;

    class WorkingSetCheckboxTreeViewer extends CheckboxTreeViewer
    {
        private IWorkingSet workingSet;

        public WorkingSetCheckboxTreeViewer(Composite parent, int style, IWorkingSet workingset)
        {
            super(parent, style);
            this.workingSet = workingset;
        }

        @Override
        public void add(Object parentElementOrTreePath, Object[] childElements)
        {
            TreeItem item;

            super.add(parentElementOrTreePath, childElements);

            for (Object c : childElements) {
                item = (TreeItem) findItem(c);
                setChecked(item, c);
            }
        }

        protected Item newItem(Widget parent, int flags, int ix)
        {
            if (parent instanceof TreeItem) {
                setChecked((TreeItem) parent, parent.getData());
            }

            return super.newItem(parent, flags, ix);
        }

        private void setChecked(TreeItem item, Object o)
        {
            if (o instanceof DataManager) {
                DataManager dataManager = (DataManager) o;
                item.setChecked(UIUtils.isXWikiEcipseIdInWorkingSet(dataManager.getXWikiEclipseId(), workingSet));
            }

            if (o instanceof ModelObject) {
                ModelObject modelObject = (ModelObject) o;
                item.setChecked(UIUtils.isXWikiEcipseIdInWorkingSet(modelObject.getXWikiEclipseId(), workingSet));
            }
        }

        public void setWorkingSet(IWorkingSet workingSet)
        {
            this.workingSet = workingSet;
        }
    }

    public XWikiEclipseWorkingSetPage()
    {
        super("XWiki Eclipse Working Set", "XWiki Eclipse Working Set", UIPlugin
            .getImageDescriptor(UIConstants.CONNECTION_SETTINGS_BANNER));
    }

    public void finish()
    {
        Set<Object> elements = new HashSet<Object>();
        for (Object object : treeViewer.getCheckedElements()) {
            if (object instanceof DataManager) {
                DataManager dataManager = (DataManager) object;
                elements.add(dataManager.getProject());
                elements.add(new XWikiEclipseElementId(((DataManager) object).getXWikiEclipseId()));
            }

            if (object instanceof ModelObject) {
                elements.add(new XWikiEclipseElementId(((ModelObject) object).getXWikiEclipseId()));
            }
        }

        if (workingSet == null) {
            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            workingSet =
                workingSetManager.createWorkingSet(workingSetName.getText(), elements.toArray(new IAdaptable[0]));
        } else {
            workingSet.setElements(elements.toArray(new IAdaptable[0]));
        }

    }

    public IWorkingSet getSelection()
    {
        return workingSet;
    }

    public void setSelection(IWorkingSet workingSet)
    {
        this.workingSet = workingSet;
    }

    public void createControl(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Working set name:");

        workingSetName = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(workingSetName);
        workingSetName.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                validateInput();
            }

        });
        if (workingSet != null) {
            workingSetName.setText(workingSet.getName());
        }

        label = new Label(composite, SWT.NONE);
        label.setText("Elements:");

        treeViewer = new WorkingSetCheckboxTreeViewer(composite, SWT.BORDER, workingSet);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(treeViewer.getControl());
        treeViewer.setContentProvider(new NavigatorContentProvider());
        treeViewer.setLabelProvider(new WorkbenchLabelProvider());
        treeViewer.setInput(this);
        treeViewer.getTree().addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                if (event.detail == SWT.CHECK) {
                    TreeItem item = (TreeItem) event.item;
                    boolean checked = item.getChecked();
                    checkItems(item, checked);
                    checkPath(item.getParentItem(), checked, false);
                }
            }
        });

        label = new Label(composite, SWT.BORDER | SWT.WRAP);
        label
            .setText("To select all pages in a space, first expand the space node and then click on the checkbox next to it.");

        setControl(composite);

        validateInput();
    }

    @Override
    public boolean isPageComplete()
    {
        return isPageComplete;
    }

    private void checkPath(TreeItem item, boolean checked, boolean grayed)
    {
        if (item == null) {
            return;
        }

        if (grayed) {
            checked = true;
        } else {
            int index = 0;
            TreeItem[] items = item.getItems();
            while (index < items.length) {
                TreeItem child = items[index];
                if (child.getGrayed() || checked != child.getChecked()) {
                    checked = grayed = true;
                    break;
                }
                index++;
            }
        }

        item.setChecked(checked);
        item.setGrayed(grayed);
        checkPath(item.getParentItem(), checked, grayed);
    }

    private void checkItems(TreeItem item, boolean checked)
    {
        item.setGrayed(false);
        item.setChecked(checked);
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; i++) {
            checkItems(items[i], checked);
        }
    }

    public void setPageComplete(boolean complete)
    {
        isPageComplete = complete;
        if (isCurrentPage()) {
            getContainer().updateButtons();
        }
    }

    private void validateInput()
    {
        if (workingSetName != null) {
            setPageComplete(workingSetName.getText().length() > 0);
        }
    }

}
