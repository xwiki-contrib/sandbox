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
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.ui.utils.UIUtils;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class DataManagerConnectHandler extends AbstractHandler
{
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        Set selectedObjects = UIUtils.getSelectedObjectsFromSelection(selection);
        for (Object selectedObject : selectedObjects) {
            if (selectedObject instanceof DataManager) {
                final DataManager dataManager = (DataManager) selectedObject;

                try {
                    UIUtils.runWithProgress(new IRunnableWithProgress()
                    {
                        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                            InterruptedException
                        {
                            monitor.beginTask(String.format("Connecting %s", dataManager.getName()),
                                IProgressMonitor.UNKNOWN);
                            SafeRunner.run(new XWikiEclipseSafeRunnable()
                            {
                                public void run() throws Exception
                                {
                                    dataManager.connect();
                                }

                            });

                            monitor.done();
                        }

                    }, HandlerUtil.getActiveShell(event));
                } catch (Exception e) {
                    throw new ExecutionException("Unable to connect", e);
                }
            }
        }

        return null;
    }

}
