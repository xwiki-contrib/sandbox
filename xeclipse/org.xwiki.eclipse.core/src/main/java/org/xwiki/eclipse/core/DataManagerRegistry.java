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
package org.xwiki.eclipse.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.xwiki.eclipse.core.notifications.CoreEvent;
import org.xwiki.eclipse.core.notifications.NotificationManager;

public class DataManagerRegistry implements IResourceChangeListener
{
    private static DataManagerRegistry sharedInstance;

    private List<DataManager> dataManagers;

    private DataManagerRegistry()
    {
        dataManagers = new ArrayList<DataManager>();
    }

    public synchronized static DataManagerRegistry getDefault()
    {
        if (sharedInstance == null) {
            sharedInstance = new DataManagerRegistry();

            // Not as a Job, from this revision, for state persistence across sessions.
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            for (IProject project : projects) {
                if (project.isOpen()) {
                    try {
                        if (hasXWikiEclipseNature(project)) {
                            sharedInstance.register(new DataManager(project));
                        }
                    } catch (CoreException e) {
                        CoreLog.logError(String.format("Unable to read project %s's nature.", project.getName()), e);
                    }
                }
            }

            ResourcesPlugin.getWorkspace().addResourceChangeListener(sharedInstance, IResourceChangeEvent.POST_BUILD);
        }

        return sharedInstance;
    }

    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    public synchronized void register(DataManager dataManager)
    {
        dataManagers.add(dataManager);

        try {
            if (dataManager.isAutoConnect()) {
                try {
                    dataManager.connect();
                } catch (Exception e) {
                    // ignored. Because getDefault() not running as a job now, since this revision.
                }
            }
        } catch (Exception e) {
            CoreLog.logWarning(String.format("Cannot connect '%s'. Disabling auto connect option for the future.",
                dataManager.getName()));
        }

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.DATA_MANAGER_REGISTERED, this, dataManager);
    }

    public synchronized void unregister(DataManager dataManager)
    {
        dataManagers.remove(dataManager);
        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.DATA_MANAGER_UNREGISTERED, this, dataManager);
    }

    public void resourceChanged(IResourceChangeEvent event)
    {
        IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                delta.accept(new IResourceDeltaVisitor()
                {
                    public boolean visit(IResourceDelta delta) throws CoreException
                    {
                        if (delta.getFlags() == IResourceDelta.OPEN) {
                            IProject project = delta.getResource().getProject();

                            DataManager dataManager = findDataManagerByProject(project);

                            if (project.isOpen()) {
                                if (hasXWikiEclipseNature(project)) {
                                    if (dataManager == null) {
                                        register(new DataManager(project));
                                    }
                                }
                            } else {
                                if (dataManager != null) {
                                    unregister(dataManager);
                                }
                            }

                            return false;
                        }

                        if (delta.getKind() == IResourceDelta.REMOVED && (delta.getResource() instanceof IProject)) {
                            IProject project = (IProject) delta.getResource();
                            DataManager dataManager = findDataManagerByProject(project);
                            if (dataManager != null) {
                                unregister(dataManager);
                            }

                            return false;
                        }

                        return true;
                    }
                });

            } catch (CoreException e) {
                CoreLog.logError("Problem while visiting resources for changes", e);
            }
        }
    }

    public DataManager findDataManagerByProject(IProject project)
    {
        for (DataManager dataManager : dataManagers) {
            if (dataManager.getProject().equals(project)) {
                return dataManager;
            }
        }

        return null;
    }

    public DataManager getDataManagerByName(String name)
    {
        for (DataManager dataManager : dataManagers) {
            if (dataManager.getName().equalsIgnoreCase(name)) {
                return dataManager;
            }
        }

        return null;
    }

    public List<DataManager> getDataManagers()
    {
        return new ArrayList<DataManager>(dataManagers);
    }

    private static boolean hasXWikiEclipseNature(IProject project) throws CoreException
    {
        IProjectDescription projectDescription = project.getDescription();
        for (String nature : projectDescription.getNatureIds()) {
            if (nature.equals(XWikiEclipseNature.ID)) {
                return true;
            }
        }

        return false;
    }
}
