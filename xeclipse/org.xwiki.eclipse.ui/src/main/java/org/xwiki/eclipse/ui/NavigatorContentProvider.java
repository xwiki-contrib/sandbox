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
package org.xwiki.eclipse.ui;

import java.util.List;

import org.codehaus.swizzle.confluence.SpaceSummary;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.xwiki.eclipse.core.CoreLog;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.DataManagerRegistry;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseObject;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.core.notifications.CoreEvent;
import org.xwiki.eclipse.core.notifications.ICoreEventListener;
import org.xwiki.eclipse.core.notifications.NotificationManager;
import org.xwiki.eclipse.ui.utils.LoadingJob;
import org.xwiki.eclipse.ui.utils.UIUtils;
import org.xwiki.eclipse.ui.utils.XWikiPendingUpdateAdapter;

public class NavigatorContentProvider extends BaseWorkbenchContentProvider implements ICoreEventListener
{
    private static final Object[] NO_OBJECTS = new Object[0];

    private AbstractTreeViewer viewer;

    private IWorkingSet workingSet;

    private DeferredTreeContentManager deferredTreeContentManager;

    public NavigatorContentProvider()
    {
        super();
        NotificationManager.getDefault().addListener(
            this,
            new CoreEvent.Type[] {CoreEvent.Type.DATA_MANAGER_REGISTERED, CoreEvent.Type.DATA_MANAGER_UNREGISTERED,
            CoreEvent.Type.DATA_MANAGER_CONNECTED, CoreEvent.Type.DATA_MANAGER_DISCONNECTED,
            CoreEvent.Type.PAGE_STORED, CoreEvent.Type.OBJECT_STORED, CoreEvent.Type.PAGE_REMOVED,
            CoreEvent.Type.OBJECT_REMOVED, CoreEvent.Type.REFRESH, CoreEvent.Type.PAGE_RENAMED,
            CoreEvent.Type.SPACE_REMOVED, CoreEvent.Type.ATTACHMENT_REMOVED, CoreEvent.Type.ATTACHMENT_STORED,
            CoreEvent.Type.ATTACHMENT_RENAMED, CoreEvent.Type.ATTACHMENT_COPIED, CoreEvent.Type.PAGE_COPIED,
            CoreEvent.Type.SPACE_ADDED});

        workingSet = null;
    }

    @Override
    public void dispose()
    {
        NotificationManager.getDefault().removeListener(this);
        super.dispose();
    }

    @Override
    public Object[] getChildren(Object element)
    {
        /* If our parent is a project then return the data manager associated to that project */
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            DataManager dataManager = DataManagerRegistry.getDefault().findDataManagerByProject(project);
            if (dataManager != null) {
                return new Object[] {dataManager};
            } else {
                return NO_OBJECTS;
            }
        }
        if (element instanceof XWikiEclipseSpaceSummary) {
            final XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) element;
            if (!spaceSummary.getDataManager().getSupportedFunctionalities().contains(Functionality.LAZY_RETRIEVAL))
                return deferredTreeContentManager.getChildren(element);
            int numberOfPages = spaceSummary.getDataManager().getNumberOfPages(spaceSummary.getData().getKey());
            if (numberOfPages < 100)
                return deferredTreeContentManager.getChildren(element);
            XWikiPendingUpdateAdapter placeHolder = new XWikiPendingUpdateAdapter();
            new LoadingJob(viewer, placeHolder, spaceSummary, numberOfPages).schedule();
            return new Object[] {placeHolder};
        }
        if (element instanceof DataManager) {
            final DataManager dataManager = (DataManager) element;
            if (!dataManager.getSupportedFunctionalities().contains(Functionality.LAZY_RETRIEVAL))
                return deferredTreeContentManager.getChildren(element);
            int numberOfSpaces = dataManager.getNumberOfSpaces();
            if (numberOfSpaces < 100)
                return deferredTreeContentManager.getChildren(element);
            XWikiPendingUpdateAdapter placeHolder = new XWikiPendingUpdateAdapter();
            new LoadingJob(viewer, placeHolder, dataManager, numberOfSpaces).schedule();
            return new Object[] {placeHolder};
        }
        return deferredTreeContentManager.getChildren(element);
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return deferredTreeContentManager.mayHaveChildren(element);
    }

    @Override
    public Object[] getElements(Object element)
    {
        Object[] result = DataManagerRegistry.getDefault().getDataManagers().toArray();
        return UIUtils.filterByWorkingSet(result, workingSet);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        this.viewer = (AbstractTreeViewer) viewer;

        if (newInput instanceof IWorkingSet) {
            workingSet = (IWorkingSet) newInput;
        } else {
            workingSet = null;
        }

        deferredTreeContentManager = new WorkingSetDeferredTreeContentManager(this.viewer, workingSet);

        super.inputChanged(viewer, oldInput, newInput);
    }

    public void handleCoreEvent(final CoreEvent event)
    {
        switch (event.getType()) {
            case DATA_MANAGER_REGISTERED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        /*
                         * FIXME: Find a way to add new DataManagers to the viewer to avoid flicker and loss of expanded
                         * state caused by refresh(). Tried: viewer.add(dataManager.getProject().getParent(),
                         * dataManager) but the data manager that was added could not be expanded. No arrow appeared
                         * next to it and isExpandable(dataManager) returns false. The arrow would appear only after
                         * issuing refresh(), but that destroys the expanded state of the viewer.
                         */
                        viewer.refresh();
                    }
                });
                break;

            case ATTACHMENT_REMOVED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseAttachmentSummary attachmentSummary =
                            (XWikiEclipseAttachmentSummary) event.getData();
                        viewer.remove(attachmentSummary);
                    }
                });
                break;

            case SPACE_ADDED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) event.getData();
                        viewer.add(spaceSummary.getDataManager(), spaceSummary);
                    }
                });
                break;

            case ATTACHMENT_RENAMED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipsePageSummary oldpageSummary = ((XWikiEclipsePageSummary[]) event.getData())[0];
                        XWikiEclipsePageSummary newPageSummary = ((XWikiEclipsePageSummary[]) event.getData())[1];
                        // FIXME: A better way possibly is to use viewer.add() as in PAGE_RENAMED below. This would stop
                        // the refreshing.
                        viewer.refresh(oldpageSummary);
                        viewer.refresh(newPageSummary);
                    }
                });
                break;

            case ATTACHMENT_COPIED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipsePageSummary pageSummary = (XWikiEclipsePageSummary) event.getData();
                        viewer.refresh(pageSummary);
                    }
                });
                break;

            case PAGE_COPIED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) event.getData();
                        viewer.refresh(spaceSummary);
                    }
                });
                break;

            case ATTACHMENT_STORED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseAttachmentSummary attachmentSummary =
                            (XWikiEclipseAttachmentSummary) event.getData();
                        XWikiEclipsePageSummary pageSummary =
                            new XWikiEclipsePageSummary(attachmentSummary.getDataManager(), attachmentSummary
                                .getPageSummary());
                        viewer.refresh(pageSummary);
                    }

                });
                break;

            case DATA_MANAGER_UNREGISTERED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        DataManager dataManager = (DataManager) event.getData();
                        viewer.remove(dataManager);
                    }
                });
                break;

            case DATA_MANAGER_CONNECTED:
            case DATA_MANAGER_DISCONNECTED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        DataManager dataManager = (DataManager) event.getSource();
                        viewer.refresh(dataManager);
                    }
                });
                break;

            case PAGE_STORED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipsePage page = (XWikiEclipsePage) event.getData();

                        // Check if this is a newly created page.
                        if (page.getData().getVersion() == 1) {
                            // Make sure the new page/space get drawn.
                            SpaceSummary spaceSummary = new SpaceSummary();
                            spaceSummary.setKey(page.getData().getSpace());
                            spaceSummary.setName(page.getData().getSpace());
                            XWikiEclipseSpaceSummary space =
                                new XWikiEclipseSpaceSummary(page.getDataManager(), spaceSummary);

                            // If the space did not previously exist, draw it.
                            if (viewer.testFindItem(space) == null)
                                viewer.add(page.getDataManager(), space);

                            viewer.add(space, page.getSummary());
                            viewer.expandToLevel(page.getSummary(), 0);
                        } else {
                            viewer.refresh(page.getSummary());
                        }
                    }
                });
                break;

            case PAGE_RENAMED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipsePage oldPage = ((XWikiEclipsePage[]) event.getData())[0];
                        XWikiEclipsePage newPage = ((XWikiEclipsePage[]) event.getData())[1];

                        SpaceSummary spaceSummary = new SpaceSummary();
                        spaceSummary.setKey(newPage.getData().getSpace());
                        spaceSummary.setName(newPage.getData().getSpace());
                        XWikiEclipseSpaceSummary space =
                            new XWikiEclipseSpaceSummary(newPage.getDataManager(), spaceSummary);

                        viewer.add(newPage.getDataManager(), space);
                        viewer.add(space, newPage.getSummary());
                        viewer.remove(oldPage.getSummary());
                    }
                });
                break;

            case PAGE_REMOVED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipsePage page = (XWikiEclipsePage) event.getData();
                        String spaceKey = page.getData().getSpace();

                        List<XWikiEclipsePageSummary> pages = null;
                        try {
                            pages = page.getDataManager().getPages(spaceKey);
                        } catch (XWikiEclipseException e) {
                            CoreLog.logError("Unable to get space pages: " + e.getMessage());
                        }

                        if (pages != null && pages.size() == 0) {
                            // The space is left with no pages so it has to be removed.
                            SpaceSummary spaceSummary = new SpaceSummary();
                            spaceSummary.setKey(spaceKey);
                            spaceSummary.setName(spaceKey);

                            XWikiEclipseSpaceSummary space =
                                new XWikiEclipseSpaceSummary(page.getDataManager(), spaceSummary);
                            viewer.remove(space);
                        } else {
                            viewer.remove(page.getSummary());
                        }
                    }
                });
                break;

            case OBJECT_STORED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseObject object = (XWikiEclipseObject) event.getData();
                        XWikiEclipsePageSummary pageSumary =
                            new XWikiEclipsePageSummary(object.getDataManager(), object.getPageSummary());
                        /*
                         * FIXME: For lack of a way of knowing whether the object has just been created or modified, I
                         * chose to refresh all the objects in the page. Best way: like the PAGE_STORED event handling,
                         * only that, in that case, there was a way of knowing if the page was just created and that
                         * there were visual inconsistencies. Maybe a new OBJECT_CREATED event? This could be an elegant
                         * solution for the PAGE_STORED too, by introducing a PAGE_CREATED event.
                         */
                        viewer.refresh(pageSumary);
                    }

                });
                break;

            case OBJECT_REMOVED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseObject object = (XWikiEclipseObject) event.getData();
                        viewer.remove(object);
                    }

                });
                break;

            case SPACE_REMOVED:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        XWikiEclipseSpaceSummary space = (XWikiEclipseSpaceSummary) event.getData();
                        viewer.remove(space);
                    }

                });
                break;

            case REFRESH:
                Display.getDefault().syncExec(new Runnable()
                {
                    public void run()
                    {
                        /*
                         * FIXME: This should work but it doesn't. Can't get the viewer's expanded elements to restore
                         * after a refresh. Tried many things, none seem to work. Any attempt at restoring the expanded
                         * state fails, although the viewer's data classes all have equals and hashCode methods
                         * overridden in their superclass.
                         */
                        Object[] expandedElements = viewer.getVisibleExpandedElements();
                        viewer.refresh(event.getData());
                        viewer.setExpandedElements(expandedElements);
                    }
                });
                break;
        }
    }

}
