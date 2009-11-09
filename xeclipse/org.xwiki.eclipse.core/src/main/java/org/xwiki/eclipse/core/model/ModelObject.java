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

import org.eclipse.core.runtime.Assert;
import org.xwiki.eclipse.core.DataManager;

/**
 * Base class for all the objects that are used to model XWiki elements in XWiki Eclipse. This class is used basically
 * to associate the elements returned by XWiki to the data manager that retrieved them.
 * 
 * @see DataManager
 */
public abstract class ModelObject
{
    /**
     * The data manager that retrieved this object.
     */
    private DataManager dataManager;

    /**
     * Constructor.
     * 
     * @param dataManager The data manager that generated this object.
     */
    public ModelObject(DataManager dataManager)
    {
        Assert.isNotNull(dataManager);
        this.dataManager = dataManager;
    }

    /**
     * @return The data manager associated to this object.
     */
    public DataManager getDataManager()
    {
        return dataManager;
    }

    /**
     * This method is used to associate an unique identifier that will be used for comparisons. Using identifiers from
     * the XWiki elements is not enough when, for example, a page with the same id exists in two different remote
     * XWikis. With this method subclasses may provide additional information to build an unique identifier that can be
     * used to differentiate the objects. Typically the data manager id will be part of this identifier.
     * 
     * @return An unique identifier for this object.
     */
    public abstract String getXWikiEclipseId();

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getXWikiEclipseId() == null) ? 0 : getXWikiEclipseId().hashCode());
        return result;
    }

    /*
     * Comparisons for object equality are done by using the XWiki Eclipse id provided by the getXWikiEclipseId()
     * method. (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelObject other = (ModelObject) obj;
        if (getXWikiEclipseId() == null) {
            if (other.getXWikiEclipseId() != null)
                return false;
        } else if (!getXWikiEclipseId().equals(other.getXWikiEclipseId()))
            return false;
        return true;
    }
}
