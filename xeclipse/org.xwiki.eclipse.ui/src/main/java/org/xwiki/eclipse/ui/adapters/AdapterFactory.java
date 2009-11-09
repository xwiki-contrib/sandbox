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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;

public class AdapterFactory implements IAdapterFactory
{
    private Class[] adapterList =
        new Class[] {IWorkbenchAdapter.class, IWorkbenchAdapter2.class, IDeferredWorkbenchAdapter.class};

    private DataManagerAdapter xwikiEclipseDataManagerAdapter = new DataManagerAdapter();

    private XWikiEclipseSpaceSummaryAdapter xwikiEclipseSpaceSummaryAdapter = new XWikiEclipseSpaceSummaryAdapter();

    private XWikiEclipsePageSummaryAdapter xwikiEclipsePageSummaryAdapter = new XWikiEclipsePageSummaryAdapter();

    private XWikiEclipseObjectSummaryAdapter xwikiEclipseObjectSummaryAdapter = new XWikiEclipseObjectSummaryAdapter();

    private XWikiEclipseAttachmentSummaryAdapter xwikiEclipseAttachmentSummaryAdapter =
        new XWikiEclipseAttachmentSummaryAdapter();

    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        /*
         * Adapters for DataManager
         */
        if ((adaptableObject instanceof DataManager) && adapterType.equals(IWorkbenchAdapter.class)) {
            return xwikiEclipseDataManagerAdapter;
        }

        if ((adaptableObject instanceof DataManager) && adapterType.equals(IWorkbenchAdapter2.class)) {
            return xwikiEclipseDataManagerAdapter;
        }

        if ((adaptableObject instanceof DataManager) && adapterType.equals(IDeferredWorkbenchAdapter.class)) {
            return xwikiEclipseDataManagerAdapter;
        }

        /*
         * Adapters for XWikiEclipseSpaceSummary
         */
        if ((adaptableObject instanceof XWikiEclipseSpaceSummary) && adapterType.equals(IWorkbenchAdapter.class)) {
            return xwikiEclipseSpaceSummaryAdapter;
        }

        if ((adaptableObject instanceof XWikiEclipseSpaceSummary)
            && adapterType.equals(IDeferredWorkbenchAdapter.class)) {
            return xwikiEclipseSpaceSummaryAdapter;
        }

        /*
         * Adapters for XWikiEclipsePageSummary
         */
        if ((adaptableObject instanceof XWikiEclipsePageSummary) && adapterType.equals(IWorkbenchAdapter.class)) {
            return xwikiEclipsePageSummaryAdapter;
        }

        if ((adaptableObject instanceof XWikiEclipsePageSummary) && adapterType.equals(IDeferredWorkbenchAdapter.class)) {
            return xwikiEclipsePageSummaryAdapter;
        }
        /*
         * Adapters for XWikiEclipseObjectSummary
         */
        if ((adaptableObject instanceof XWikiEclipseObjectSummary) && adapterType.equals(IWorkbenchAdapter.class)) {
            return xwikiEclipseObjectSummaryAdapter;
        }
        /*
         * Adapters for XWikiEclipseAttachmentSummary
         */
        if ((adaptableObject instanceof XWikiEclipseAttachmentSummary) && adapterType.equals(IWorkbenchAdapter.class)) {
            return xwikiEclipseAttachmentSummaryAdapter;
        }

        return null;
    }

    public Class[] getAdapterList()
    {
        return adapterList;
    }

}
