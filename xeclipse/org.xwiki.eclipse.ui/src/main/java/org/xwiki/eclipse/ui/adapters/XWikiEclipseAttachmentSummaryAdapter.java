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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.utils.CoreUtils;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;

public class XWikiEclipseAttachmentSummaryAdapter extends WorkbenchAdapter
{
    @Override
    public String getLabel(Object object)
    {
        if (object instanceof XWikiEclipseAttachmentSummary) {
            XWikiEclipseAttachmentSummary attachmentSummary = (XWikiEclipseAttachmentSummary) object;

            return attachmentSummary.getTitle() + " ["
                + CoreUtils.prettySize(Integer.parseInt(attachmentSummary.getSize())) + "]";
        }

        return super.getLabel(object);
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object)
    {
        if (object instanceof XWikiEclipseAttachmentSummary) {
            return UIPlugin.getImageDescriptor(UIConstants.ATTACHMENT_ICON);
        }
        return null;
    }
}
