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
package org.xwiki.eclipse.ui.adapters;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.xwiki.eclipse.core.CoreLog;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.utils.UIUtils;

public class XWikiEclipseSpaceSummaryAdapter extends WorkbenchAdapter implements IDeferredWorkbenchAdapter
{
    @Override
    public Object[] getChildren(Object object)
    {
        if (object instanceof XWikiEclipseSpaceSummary) {
            final XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) object;

            try {
                List<XWikiEclipsePageSummary> result =
                    spaceSummary.getDataManager().getPages(spaceSummary.getData().getKey());
                return result.toArray();
            } catch (XWikiEclipseException e) {
                UIUtils
                    .showMessageDialog(
                        Display.getDefault().getActiveShell(),
                        SWT.ICON_ERROR,
                        "Error getting pages.",
                        "There was a communication error while getting pages. XWiki Eclipse is taking the connection offline in order to prevent further errors. Please check your remote XWiki status and then try to reconnect.");

                CoreLog.logError("Error getting pages", e);

                spaceSummary.getDataManager().disconnect();
                return NO_CHILDREN;
            }

        }

        return super.getChildren(object);
    }

    @Override
    public String getLabel(Object object)
    {
        if (object instanceof XWikiEclipseSpaceSummary) {
            XWikiEclipseSpaceSummary spaceSummary = (XWikiEclipseSpaceSummary) object;
            return spaceSummary.getData().getKey();
        }

        return super.getLabel(object);
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object)
    {
        return UIPlugin.getImageDescriptor(UIConstants.SPACE_ICON);
    }

    public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor)
    {
        collector.add(getChildren(object), monitor);
        collector.done();
    }

    public ISchedulingRule getRule(Object object)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isContainer()
    {
        // FIXME: haschildren() equivalent. create RPC methods. and implement. same for all other adapters.
        // But then, how do I get the Summary/object here?
        return true;
    }

}
