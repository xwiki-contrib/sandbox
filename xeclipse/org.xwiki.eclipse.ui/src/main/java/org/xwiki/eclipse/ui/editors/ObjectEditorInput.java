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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.xwiki.eclipse.core.model.XWikiEclipseObject;

public class ObjectEditorInput implements IEditorInput
{
    private XWikiEclipseObject object;

    public ObjectEditorInput(XWikiEclipseObject object)
    {
        this.object = object;
    }

    public boolean exists()
    {
        return false;
    }

    public ImageDescriptor getImageDescriptor()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName()
    {
        return object.getName();
    }

    public IPersistableElement getPersistable()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getToolTipText()
    {
        return getName();
    }

    public Object getAdapter(Class adapter)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public XWikiEclipseObject getObject()
    {
        return object;
    }

    @Override
    public int hashCode()
    {
        return object.hashCode();
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
        ObjectEditorInput other = (ObjectEditorInput) obj;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.getXWikiEclipseId().equals(other.object.getXWikiEclipseId()))
            return false;
        return true;
    }
}
