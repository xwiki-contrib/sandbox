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
package org.xwiki.eclipse.ui.editors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.xwiki.eclipse.core.DataManagerRegistry;
import org.xwiki.eclipse.core.XWikiEclipseException;

public class PageEditorInputFactory implements IElementFactory
{

    private static final String ID_FACTORY = "org.xwiki.eclipse.ui.editors.PageEditorInputFactory";

    private static final String EDITOR = "PageEditor";

    public PageEditorInputFactory()
    {
    }

    private static PageEditorInput object = null;

    public IAdaptable createElement(IMemento memento)
    {
        final String page = memento.getString(EDITOR);
        if (page == null || page.equalsIgnoreCase("")) {
            return null;
        }
        if (page != null) {
            Job job1 = new Job("Restoring Editor History")
            {
                @Override
                protected IStatus run(IProgressMonitor monitor)
                {
                    Display.getDefault().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            try {
                                String pages[] = page.split("\\.");
                                PageEditorInput page_input = null;
                                while (page_input != null) {
                                    page_input =
                                        new PageEditorInput(DataManagerRegistry.getDefault().getDataManagerByName(
                                            pages[0]).getPage(pages[1] + "." + pages[2]), false);
                                }
                                object = page_input;
                            } catch (XWikiEclipseException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    return Status.OK_STATUS;
                }
            };
            job1.schedule(1000);
            return object;
        } else {
            return null;
        }
    }

    public static String getFactoryId()
    {
        return ID_FACTORY;
    }

    public static void saveState(IMemento memento, PageEditorInput input)
    {
        memento.putString(EDITOR, input.getPage().getDataManager().getName() + "."
            + input.getPage().getSummary().getData().getId());
    }
}
