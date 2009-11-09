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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.xmlrpc.model.XWikiClass;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiObjectSummary;
import org.xwiki.xmlrpc.model.XWikiPageSummary;

/**
 * A class representing an XWiki object.
 */
public class XWikiEclipseObject extends ModelObject
{
    private XWikiObject data;

    private XWikiClass xwikiClass;

    private XWikiPageSummary pageSummary;

    /**
     * Constructor
     * 
     * @param dataManager The data manager that returned this object.
     * @param data The actual XWiki object description.
     * @param xwikiClass The XWiki class for this object.
     */
    public XWikiEclipseObject(DataManager dataManager, XWikiObject data, XWikiClass xwikiClass,
        XWikiPageSummary pageSummary)
    {
        super(dataManager);

        Assert.isNotNull(data);
        this.data = data;

        Assert.isNotNull(xwikiClass);
        Assert.isLegal(data.getClassName().equals(xwikiClass.getId()));
        this.xwikiClass = xwikiClass;

        this.pageSummary = pageSummary;
    }

    public XWikiObject getData()
    {
        return data;
    }

    public XWikiClass getXWikiClass()
    {
        return xwikiClass;
    }

    /**
     * @return The list of properties available for this object.
     * @see XWikiEclipseObjectProperty
     */
    public List<XWikiEclipseObjectProperty> getProperties()
    {
        List<XWikiEclipseObjectProperty> result = new ArrayList<XWikiEclipseObjectProperty>();
        for (String propertyName : xwikiClass.getProperties()) {
            result.add(new XWikiEclipseObjectProperty(this, propertyName));
        }

        return result;
    }

    /**
     * @param propertyName
     * @return The information for a given property.
     */
    public XWikiEclipseObjectProperty getProperty(String propertyName)
    {
        return new XWikiEclipseObjectProperty(this, propertyName);
    }

    public String getName()
    {
        String name = data.getPrettyName();
        if (name == null) {
            if (data.getId() == -1) {
                name = String.format("%s[NEW]", data.getClassName());
            } else {
                name = String.format("%s[%d]", data.getClassName(), data.getId());
            }
        }

        return name;
    }

    public XWikiPageSummary getPageSummary()
    {
        return pageSummary;
    }

    public XWikiEclipseObjectSummary getSummary()
    {
        XWikiObjectSummary summary = new XWikiObjectSummary(data.toRawMap());
        return new XWikiEclipseObjectSummary(getDataManager(), summary, pageSummary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXWikiEclipseId()
    {
        return String
            .format(
                "xwikieclipse://%s/%s/%s/%d", getDataManager().getName(), data.getPageId(), data.getClassName(), data.getId()); //$NON-NLS-1$
    }

}
