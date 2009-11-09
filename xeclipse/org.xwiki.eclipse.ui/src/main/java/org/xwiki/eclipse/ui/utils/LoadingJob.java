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
package org.xwiki.eclipse.ui.utils;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.progress.UIJob;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;

public class LoadingJob extends Job
{

    private AbstractTreeViewer viewer;

    private XWikiPendingUpdateAdapter placeHolder;

    private XWikiEclipseSpaceSummary spaceSummary;

    private DataManager dataManager;

    private int number;

    public LoadingJob(AbstractTreeViewer viewer, XWikiPendingUpdateAdapter placeHolder,
        XWikiEclipseSpaceSummary spaceSummary, int numberOfSpaces)
    {
        super(spaceSummary.getData().getKey() + " Loading..");
        this.viewer = viewer;
        this.placeHolder = placeHolder;
        this.spaceSummary = spaceSummary;
        this.number = numberOfSpaces;
    }

    public LoadingJob(AbstractTreeViewer viewer, XWikiPendingUpdateAdapter placeHolder, DataManager dataManager,
        int numberOfSpaces)
    {
        super(dataManager.getName() + " Loading..");
        this.viewer = viewer;
        this.placeHolder = placeHolder;
        this.dataManager = dataManager;
        this.number = numberOfSpaces;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        // UIJob is same as Job with Display.asyncExec.
        Job job_viewer_refresh = new UIJob("Refreshing viewer every 200 msec.")
        {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor)
            {
                if (!placeHolder.isDisposed()) {
                    viewer.refresh(placeHolder, true);
                    schedule(200);
                }
                return Status.OK_STATUS;
            }
        };
        job_viewer_refresh.schedule();

        try {
            if (spaceSummary != null) {
                Job add_to_tree = new UIJob("Adding to tree lazily..")
                {

                    int i = 0;

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor)
                    {
                        try {
                            List<XWikiEclipsePageSummary> result =
                                spaceSummary.getDataManager().getPages(spaceSummary.getData().getKey(), i,
                                    (i + 20) < number ? 20 : number - i);
                            // FIXME: 20 is just intuition. Any calculation needed here?
                            i += 20;
                            viewer.add(spaceSummary, result.toArray());
                            if ((i + 20) < number)
                                schedule(150);
                            else
                                placeHolder.dispose();
                        } catch (XWikiEclipseException e) {
                            e.printStackTrace();
                        }
                        return Status.OK_STATUS;
                    }

                };

                add_to_tree.schedule();
            }
            if (dataManager != null) {
                Job add_to_tree = new UIJob("Adding to tree lazily..")
                {

                    int i = 0;

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor)
                    {
                        try {
                            List<XWikiEclipseSpaceSummary> result =
                                dataManager.getSpaces(i, (i + 20) < number ? 20 : number - i);
                            // FIXME: 20 is just intuition. Any calculation needed here?
                            i += 20;
                            viewer.add(dataManager, result.toArray());
                            if ((i + 20) < number)
                                schedule(150);
                            else
                                placeHolder.dispose();
                        } catch (XWikiEclipseException e) {
                            e.printStackTrace();
                        }
                        return Status.OK_STATUS;
                    }

                };

                add_to_tree.schedule();
            }

        } finally {
            Job remove_placeholder_job = new UIJob("Removing placeholder lazily.")
            {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor)
                {
                    if (!placeHolder.isDisposed()) {
                        schedule(200);
                    }
                    if (placeHolder.isDisposed()) {
                        viewer.remove(placeHolder);
                    }

                    return Status.OK_STATUS;
                }

            };
            remove_placeholder_job.schedule(200);
        }

        return Status.OK_STATUS;
    }
}
