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
package org.xwiki.eclipse.ui.workingsets;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class XWikiEclipseElementId implements IAdaptable, IPersistableElement
{
    private String xwikiEclipseId;

    public XWikiEclipseElementId(String xwikiEclipseId)
    {
        this.xwikiEclipseId = xwikiEclipseId;
    }

    public Object getAdapter(Class adapter)
    {
        if (adapter.equals(IPersistableElement.class)) {
            return this;
        }

        return null;
    }

    public String getFactoryId()
    {
        return "org.xwiki.eclipse.ui.workingsets.XWikiEclipseElementFactory";
    }

    public void saveState(IMemento memento)
    {
        memento.putString("xwikiEclipseID", xwikiEclipseId);
    }

    public String getXwikiEclipseId()
    {
        return xwikiEclipseId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((xwikiEclipseId == null) ? 0 : xwikiEclipseId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XWikiEclipseElementId other = (XWikiEclipseElementId) obj;
        if (xwikiEclipseId == null) {
            if (other.xwikiEclipseId != null)
                return false;
        } else if (!xwikiEclipseId.equals(other.xwikiEclipseId))
            return false;
        return true;
    }

}
