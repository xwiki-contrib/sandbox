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
package org.xwiki.eclipse.ui.dialogs;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.DataManagerRegistry;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class OpenPageDialog extends FilteredItemsSelectionDialog
{
    private Set<DataManager> targetDataManagers;

    private Map<DataManager, List<XWikiEclipsePageSummary>> dataManagerToPageSummariesMap;

    private static class OpenPageLabelProvider extends LabelProvider
    {
        WorkbenchLabelProvider workbenchLabelProvider;

        public OpenPageLabelProvider(WorkbenchLabelProvider workbenchLabelProvider)
        {
            this.workbenchLabelProvider = workbenchLabelProvider;
        }

        @Override
        public String getText(Object element)
        {
            if (element instanceof XWikiEclipsePageSummary) {
                XWikiEclipsePageSummary xwikiPage = (XWikiEclipsePageSummary) element;
                return String.format("%s (%s)", xwikiPage.getData().getId(), xwikiPage.getDataManager().getName());
            }

            return super.getText(element);
        }

        @Override
        public Image getImage(Object element)
        {
            return workbenchLabelProvider.getImage(element);
        }
    }

    public OpenPageDialog(Shell shell, DataManager dataManager)
    {
        super(shell);
        setTitle("Open XWiki page");
        OpenPageLabelProvider labelProvider = new OpenPageLabelProvider(new WorkbenchLabelProvider());
        setListLabelProvider(labelProvider);
        setDetailsLabelProvider(labelProvider);

        targetDataManagers = new HashSet<DataManager>();
        dataManagerToPageSummariesMap = new HashMap<DataManager, List<XWikiEclipsePageSummary>>();

        if (dataManager != null) {
            targetDataManagers.add(dataManager);
        } else {
            for (DataManager dm : DataManagerRegistry.getDefault().getDataManagers()) {
                targetDataManagers.add(dm);
            }
        }
    }

    @Override
    protected Control createExtendedContentArea(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("Select the connections to be searched for opening pages:");

        CheckboxTreeViewer dataManagerTreeViewers = new CheckboxTreeViewer(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(
            dataManagerTreeViewers.getControl());
        dataManagerTreeViewers.setContentProvider(new ITreeContentProvider()
        {

            public Object[] getChildren(Object parentElement)
            {
                // TODO Auto-generated method stub
                return null;
            }

            public Object getParent(Object element)
            {
                // TODO Auto-generated method stub
                return null;
            }

            public boolean hasChildren(Object element)
            {
                // TODO Auto-generated method stub
                return false;
            }

            public Object[] getElements(Object inputElement)
            {
                return DataManagerRegistry.getDefault().getDataManagers().toArray();
            }

            public void dispose()
            {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

        });
        dataManagerTreeViewers.setLabelProvider(new WorkbenchLabelProvider());
        dataManagerTreeViewers.setInput(this);

        for (DataManager dataManager : targetDataManagers) {
            dataManagerTreeViewers.setChecked(dataManager, true);
        }

        dataManagerTreeViewers.addCheckStateListener(new ICheckStateListener()
        {
            public void checkStateChanged(CheckStateChangedEvent event)
            {
                if (event.getChecked()) {
                    targetDataManagers.add((DataManager) event.getElement());
                } else {
                    targetDataManagers.remove(event.getElement());
                }
            }

        });

        return composite;
    }

    @Override
    protected ItemsFilter createFilter()
    {
        return new ItemsFilter()
        {
            @Override
            public boolean isConsistentItem(Object item)
            {
                return true;
            }

            @Override
            public boolean matchItem(Object item)
            {
                if (item instanceof XWikiEclipsePageSummary) {
                    XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) item;
                    return matches(pageSummary.getData().getTitle());
                }

                return false;
            }

        };
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
        final IProgressMonitor progressMonitor) throws CoreException
    {
        progressMonitor.beginTask("Searching...", targetDataManagers.size());

        for (DataManager dataManager : targetDataManagers) {
            final DataManager currentDataManager = (DataManager) dataManager;

            /* If we don't already have summaries, then fetch them from the data manager */
            if (dataManagerToPageSummariesMap.get(currentDataManager) == null) {
                SafeRunner.run(new XWikiEclipseSafeRunnable()
                {
                    public void run() throws Exception
                    {
                        List<XWikiEclipsePageSummary> pageSummaries = currentDataManager.getAllPageIds();
                        dataManagerToPageSummariesMap.put(currentDataManager, pageSummaries);
                    }
                });
            }

            if (progressMonitor.isCanceled()) {
                break;
            }

            for (XWikiEclipsePageSummary pageSummary : dataManagerToPageSummariesMap.get(currentDataManager)) {
                contentProvider.add(pageSummary, itemsFilter);
            }
        }

        progressMonitor.done();
    }

    @Override
    protected IDialogSettings getDialogSettings()
    {
        return UIPlugin.getDefault().getDialogSettings();
    }

    @Override
    public String getElementName(Object item)
    {
        return null;
    }

    @Override
    protected IStatus validateItem(Object item)
    {
        // TODO Auto-generated method stub
        return Status.OK_STATUS;
    }

    @Override
    protected Comparator getItemsComparator()
    {
        return new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                XWikiEclipsePageSummary page1 = (XWikiEclipsePageSummary) o1;
                XWikiEclipsePageSummary page2 = (XWikiEclipsePageSummary) o2;

                return page1.getData().getTitle().compareTo(page2.getData().getTitle());
            }
        };
    }
}
