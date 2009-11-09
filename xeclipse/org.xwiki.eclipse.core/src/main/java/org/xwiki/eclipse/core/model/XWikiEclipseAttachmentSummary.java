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
package org.xwiki.eclipse.core.model;

import org.codehaus.swizzle.confluence.Attachment;
import org.eclipse.core.runtime.Assert;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.xmlrpc.model.XWikiPageSummary;

public class XWikiEclipseAttachmentSummary extends ModelObject
{
    private Attachment data;

    private XWikiPageSummary pageSummary;

    public XWikiEclipseAttachmentSummary(DataManager dataManager, Attachment data, XWikiPageSummary pageSummary)
    {
        super(dataManager);
        Assert.isNotNull(data);
        this.data = data;

        Assert.isNotNull(pageSummary);
        this.pageSummary = pageSummary;
    }

    public String getTitle()
    {
        return data.getTitle();
    }

    public String getSize()
    {
        return data.getFileSize();
    }

    public Attachment getData()
    {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXWikiEclipseId()
    {
        return String.format("xwikieclipse://%s/%s/summary", getDataManager().getName(), data.getId()); //$NON-NLS-1$
    }

    public XWikiPageSummary getPageSummary()
    {
        return pageSummary;
    }

}
