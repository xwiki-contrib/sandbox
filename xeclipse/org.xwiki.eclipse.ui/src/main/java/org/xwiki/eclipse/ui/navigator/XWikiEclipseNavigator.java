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
package org.xwiki.eclipse.ui.navigator;

import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.DataManagerRegistry;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.ui.editors.PageEditor;
import org.xwiki.eclipse.ui.editors.PageEditorInput;

public class XWikiEclipseNavigator extends CommonNavigator
{

    private IMemento memento;

    private String EXPANDED = "XWikiEclipseNavigator.Expanded";

    private String PAGE = "Page";

    private String SPACE = "Space";

    private String DM = "DataManager";

    private String pageId = "pageId";

    private String spaceId = "spaceId";

    private String dm = "dm";

    private String EDITOR = "Editor";

    private String EDITOR_ID = "PageEditor";

    private String SCROLL = "ScrollValue";

    public XWikiEclipseNavigator()
    {
        super();
    }

    @Override
    public void saveState(IMemento memento)
    {
        super.saveState(memento);
        saveEditorState(memento);
        saveTreeState(memento);
    }

    private void saveTreeState(IMemento memento)
    {
        // Saving expanded Elements of CNF viewer Tree
        Object[] obj = getCommonViewer().getExpandedElements();
        IMemento memento2 = memento.createChild(EXPANDED);
        if (obj.length < 1)
            return;
        for (Object o : obj) {
            if (o instanceof XWikiEclipsePageSummary) {
                XWikiEclipsePageSummary object = (XWikiEclipsePageSummary) o;
                IMemento temp = memento2.createChild(PAGE);
                temp.putString(pageId, object.getData().getId());
                temp.putString(spaceId, object.getData().getSpace());
                temp.putString(dm, object.getDataManager().getName());
            }
            if (o instanceof XWikiEclipseSpaceSummary) {
                XWikiEclipseSpaceSummary object = (XWikiEclipseSpaceSummary) o;
                IMemento temp = memento2.createChild(SPACE);
                temp.putString(spaceId, object.getData().getKey());
                temp.putString(dm, object.getDataManager().getName());
            }
            if (o instanceof DataManager) {
                DataManager object = (DataManager) o;
                IMemento temp = memento2.createChild(DM);
                temp.putString(dm, object.getName());
            }
        }
        // Saving scroll position
        memento.putInteger(SCROLL, getCommonViewer().getTree().getVerticalBar().getSelection());
    }

    private void saveEditorState(IMemento memento)
    {
        // Saving open Editors.
        IEditorPart[] openeditors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditors();
        IMemento mem_editors = memento.createChild(EDITOR);
        for (IEditorPart editor : openeditors) {
            if (editor.getEditorInput() instanceof PageEditorInput) {
                IMemento mem_editor = mem_editors.createChild(EDITOR_ID);
                mem_editor.putString(dm, ((PageEditorInput) editor.getEditorInput()).getPage().getDataManager()
                    .getName());
                mem_editor.putString(pageId, ((PageEditorInput) editor.getEditorInput()).getPage().getData().getId());
            }
        }
    }

    @Override
    public void createPartControl(Composite aParent)
    {
        super.createPartControl(aParent);
        restoreTreeState(memento);
        restoreEditorState(memento);
    }

    private void restoreTreeState(final IMemento memento2)
    {
        try {
            // restoring state of CNF Viewer Tree
            final Object[] e = getTreeSummariesToBeRestored(memento);
            final CommonViewer viewer = getCommonViewer();
            for (Object element : e) {
                // Data Managers
                if (element instanceof DataManager) {
                    viewer.setExpandedState(element, true);
                }
            }

            Job job = new Job("Restoring tree expansion state for space summary.")
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
                {
                    Display.getDefault().syncExec(new Runnable()
                    {

                        public void run()
                        {
                            for (Object element : e) {
                                if (element instanceof XWikiEclipseSpaceSummary) {
                                    XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) element;
                                    DataManager dataManager = spaceSummary.getDataManager();
                                    if (!viewer.getExpandedState(dataManager)) {
                                        System.out.println("space summary waiting...");
                                    }
                                    while (!viewer.getExpandedState(dataManager)) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    viewer.setExpandedState(spaceSummary, true);
                                }
                            }
                        }
                    });

                    Job job2 = new Job("Restoring tree expansion state for page summary.")
                    {
                        @Override
                        protected IStatus run(IProgressMonitor monitor)
                        {
                            Display.getDefault().asyncExec(new Runnable()
                            {
                                public void run()
                                {
                                    for (Object element : e) {
                                        if (element instanceof XWikiEclipsePageSummary) {
                                            XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) element;
                                            DataManager dataManager = pageSummary.getDataManager();
                                            XWikiEclipseSpaceSummary spaceSummary = null;
                                            try {
                                                spaceSummary =
                                                    dataManager.getSpaceSummary(pageSummary.getData().getSpace());
                                            } catch (Exception e) {
                                                System.out.println("Failed to get space summary: " + e);
                                            }
                                            if (!viewer.getExpandedState(spaceSummary)) {
                                                System.out.println("page summary waiting...");
                                            }
                                            while (!viewer.getExpandedState(spaceSummary)) {
                                                try {
                                                    Thread.sleep(100);
                                                } catch (InterruptedException e1) {
                                                    e1.printStackTrace();
                                                }
                                            }
                                            viewer.setExpandedState(pageSummary, true);
                                        }
                                    }

                                    try {
                                        viewer.getTree().getVerticalBar().setSelection(memento2.getInteger(SCROLL));
                                    } catch (NullPointerException e) {
                                        // Do nothing.
                                    }
                                }
                            });
                            return Status.OK_STATUS;
                        }
                    };
                    job2.schedule(1000);

                    return Status.OK_STATUS;
                }
            };
            job.schedule(1000);
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (XWikiEclipseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Exception occurs on the first run, or when DataManager is null.
            // e.printStackTrace();
        }
    }

    private void restoreEditorState(IMemento memento)
    {
        try {
            IMemento[] mementos = memento.getChildren(EDITOR);

            for (IMemento mem : mementos) {
                IMemento[] mem2 = mem.getChildren(EDITOR_ID);
                for (final IMemento mem3 : mem2) {
                    Job job_open_editor = new Job("Restoring editor...")
                    {
                        @Override
                        protected IStatus run(IProgressMonitor monitor)
                        {
                            Display.getDefault().asyncExec(new Runnable()
                            {
                                public void run()
                                {
                                    try {
                                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                            .openEditor(
                                                new PageEditorInput(DataManagerRegistry.getDefault()
                                                    .getDataManagerByName(mem3.getString(dm)).getPage(
                                                        mem3.getString(pageId)), false), PageEditor.ID);
                                    } catch (PartInitException e) {
                                        e.printStackTrace();
                                    } catch (XWikiEclipseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            return Status.OK_STATUS;
                        }
                    };
                    job_open_editor.schedule();
                }
            }
        } catch (NullPointerException e) {
            return;
        }
    }

    private Object[] getTreeSummariesToBeRestored(IMemento memento) throws CoreException, XWikiEclipseException,
        NullPointerException
    {
        IMemento[] mementos = memento.getChildren(EXPANDED);
        Vector<Object> elements = new Vector<Object>();
        for (IMemento mem : mementos) {
            IMemento[] mem2 = mem.getChildren(PAGE);
            for (IMemento mem3 : mem2) {
                elements.add(processChildren(mem3));
            }
            mem2 = mem.getChildren(DM);
            for (IMemento mem3 : mem2) {
                elements.add(processChildren(mem3));
            }
            mem2 = mem.getChildren(SPACE);
            for (IMemento mem3 : mem2) {
                elements.add(processChildren(mem3));
            }
        }
        return elements.toArray();
    }

    private Object processChildren(IMemento mem) throws XWikiEclipseException
    {
        if (mem.getType().equalsIgnoreCase(PAGE)) {
            DataManager dataManager = DataManagerRegistry.getDefault().getDataManagerByName(mem.getString(dm));
            XWikiEclipsePageSummary pageSummary = dataManager.getPageSummary(mem.getString(pageId));
            return pageSummary;
        }
        if (mem.getType().equalsIgnoreCase(SPACE)) {
            DataManager dataManager = DataManagerRegistry.getDefault().getDataManagerByName(mem.getString(dm));
            XWikiEclipseSpaceSummary spaceSummary = dataManager.getSpaceSummary(mem.getString(spaceId));
            return spaceSummary;
        }
        if (mem.getType().equalsIgnoreCase(DM)) {
            DataManager dataManager = DataManagerRegistry.getDefault().getDataManagerByName(mem.getString(dm));
            return dataManager;
        }
        return null;
    }

    @Override
    public void init(IViewSite aSite, IMemento aMemento) throws PartInitException
    {
        super.init(aSite, aMemento);
        memento = aMemento;
    }

    @Override
    protected CommonViewer createCommonViewer(Composite aParent)
    {
        CommonViewer aViewer =
            new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
        initListeners(aViewer);
        aViewer.getNavigatorContentService().restoreState(memento);
        return aViewer;
    }

}
