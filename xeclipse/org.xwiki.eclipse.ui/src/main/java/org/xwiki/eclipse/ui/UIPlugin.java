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

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.Functionality;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

/**
 * The activator class controls the plug-in life cycle
 */
public class UIPlugin extends AbstractUIPlugin
{
    // The plug-in ID
    public static final String PLUGIN_ID = "org.xwiki.eclipse.ui";

    // The shared instance
    private static UIPlugin plugin;

    private HashMap<DataManager, List<XWikiEclipsePageSummary>> dataManagerToPageSummariesMap;

    /**
     * The constructor
     */
    public UIPlugin()
    {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        plugin = this;
        dataManagerToPageSummariesMap = new HashMap<DataManager, List<XWikiEclipsePageSummary>>();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static UIPlugin getDefault()
    {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path)
    {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public List<XWikiEclipsePageSummary> getAllPageSummariesForDataManager(final DataManager dataManager,
        final String linkPrefix)
    {

        if (dataManager.getSupportedFunctionalities().contains(Functionality.EFFICIENT_RETRIEVAL)) {
            List<XWikiEclipsePageSummary> pageSummaries;
            try {
                dataManagerToPageSummariesMap.clear();
                pageSummaries = dataManager.getAllPageIds(linkPrefix);
                dataManagerToPageSummariesMap.put(dataManager, pageSummaries);
                return dataManagerToPageSummariesMap.get(dataManager);
            } catch (XWikiEclipseException e) {
                e.printStackTrace();
            }
        }

        /* If we don't already have summaries, then fetch them from the data manager */
        if (dataManagerToPageSummariesMap.get(dataManager) == null) {
            SafeRunner.run(new XWikiEclipseSafeRunnable()
            {
                public void run() throws Exception
                {
                    List<XWikiEclipsePageSummary> pageSummaries = dataManager.getAllPageIds(linkPrefix);
                    dataManagerToPageSummariesMap.put(dataManager, pageSummaries);
                }
            });
        }

        return dataManagerToPageSummariesMap.get(dataManager);
    }
}
