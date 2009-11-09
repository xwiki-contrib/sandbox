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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.xwiki.eclipse.ui.utils.UIUtils;

public class WorkingSetDeferredTreeContentManager extends DeferredTreeContentManager
{
    private IWorkingSet workingSet;

    private static class WorkingSetElementCollectorFilter implements IElementCollector
    {
        private IWorkingSet workingSet;

        private IElementCollector elementCollector;

        public WorkingSetElementCollectorFilter(IWorkingSet workingSet, IElementCollector elementCollector)
        {
            this.workingSet = workingSet;
            this.elementCollector = elementCollector;
        }

        public void add(Object element, IProgressMonitor monitor)
        {
            if (workingSet != null) {
                if (UIUtils.isInWorkingSet(element, workingSet)) {
                    elementCollector.add(element, monitor);
                }
            } else {
                elementCollector.add(element, monitor);
            }

        }

        public void add(Object[] elements, IProgressMonitor monitor)
        {
            Object[] filteredObjects = null;

            if (workingSet != null) {
                filteredObjects = UIUtils.filterByWorkingSet(elements, workingSet);
            } else {
                filteredObjects = elements;
            }

            elementCollector.add(filteredObjects, monitor);
        }

        public void done()
        {
            elementCollector.done();
        }

    }

    public WorkingSetDeferredTreeContentManager(AbstractTreeViewer viewer, IWorkingSet workingSet)
    {
        // Eclipse 3.3.2 has no DeferredTreeContentManager(AbstractTreeViewer viewer) constructor.
        // only DeferredTreeContentManager(ITreeContentProvider provider, AbstractTreeViewer viewer)
        super(null, viewer);
        this.workingSet = workingSet;
    }

    @Override
    protected IElementCollector createElementCollector(Object parent, PendingUpdateAdapter placeholder)
    {
        IElementCollector elementCollector = super.createElementCollector(parent, placeholder);
        return new WorkingSetElementCollectorFilter(workingSet, elementCollector);
    }

}
