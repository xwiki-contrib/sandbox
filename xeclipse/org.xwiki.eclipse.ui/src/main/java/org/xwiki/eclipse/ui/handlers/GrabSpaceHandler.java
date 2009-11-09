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
package org.xwiki.eclipse.ui.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.eclipse.core.CoreLog;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.ui.utils.UIUtils;

public class GrabSpaceHandler extends AbstractHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);
        for (Object object : selectedObjects) {
            if (object instanceof XWikiEclipseSpaceSummary) {
                final XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) object;

                try {
                    UIUtils.runWithProgress(new IRunnableWithProgress()
                    {

                        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                            InterruptedException
                        {
                            try {
                                List<XWikiEclipsePageSummary> pageSummaries =
                                    spaceSummary.getDataManager().getPages(spaceSummary.getData().getKey());

                                monitor.beginTask("Fetching pages", pageSummaries.size());

                                if (monitor.isCanceled()) {
                                    return;
                                }

                                for (XWikiEclipsePageSummary pageSummary : pageSummaries) {
                                    monitor.setTaskName(String.format("Fetching %s", pageSummary.getData().getId()));

                                    pageSummary.getDataManager().getPage(pageSummary.getData().getId());

                                    if (monitor.isCanceled()) {
                                        return;
                                    }

                                    monitor.worked(1);
                                }
                            } catch (XWikiEclipseException e) {
                                throw new InvocationTargetException(e);
                            } finally {
                                monitor.done();
                            }

                        }

                    }, HandlerUtil.getActiveShell(event), true);
                } catch (Exception e) {
                    CoreLog.logError("Error during space grabbing", e);
                }
            }
        }

        return null;
    }
}
