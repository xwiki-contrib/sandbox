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
package org.xwiki.eclipse.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.views.PagePreview;
import org.xwiki.eclipse.ui.wizards.NewConnectionWizard;

/**
 * Provides a new perspective designed for using XEclipse containing the XWiki Navigator, Outline and Page Preview
 * views.
 * 
 * @author Eduard Moraru
 */
public class XWikiPerspectiveFactory implements IPerspectiveFactory
{
    /*
     * The perspective's ID.
     */
    public final static String PERSPECTIVE_ID = UIPlugin.PLUGIN_ID + ".perspectives.XWikiPerspective";

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout)
    {
        defineActions(layout);
        defineLayout(layout);
    }

    /*
     * Defines perspective specific actions providing shortcuts for New Wizard, Show Views and Open Perspective
     * operations.
     */
    private void defineActions(IPageLayout layout)
    {
        // Add "new wizards".
        layout.addNewWizardShortcut(NewConnectionWizard.WIZARD_ID);

        // Add "show views".
        layout.addShowViewShortcut(UIConstants.NAVIGATOR_VIEW_ID);
        layout.addShowViewShortcut(PagePreview.VIEW_ID);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);

        // Add "open perspective". Looks nicer with something there.
        for (IPerspectiveDescriptor descriptor : PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives()) {
            layout.addPerspectiveShortcut(descriptor.getId());
        }
    }

    /*
     * Specifies what views are included in this perspective and their layout.
     */
    private void defineLayout(IPageLayout layout)
    {
        IFolderLayout leftFolder = layout.createFolder("leftFolder", IPageLayout.LEFT, 0.20f, layout.getEditorArea());

        leftFolder.addView(UIConstants.NAVIGATOR_VIEW_ID);

        IFolderLayout bottomFolder =
            layout.createFolder("bottomFolder", IPageLayout.BOTTOM, 0.60f, layout.getEditorArea());
        bottomFolder.addView(PagePreview.VIEW_ID);
        bottomFolder.addView("org.eclipse.pde.runtime.LogView");

        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.80f, layout.getEditorArea());
    }

}
