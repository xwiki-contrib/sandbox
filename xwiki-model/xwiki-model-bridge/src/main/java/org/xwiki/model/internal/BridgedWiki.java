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
 */
package org.xwiki.model.internal;

import java.util.List;
import java.util.Map;

import org.xwiki.model.*;
import org.xwiki.model.Object;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class BridgedWiki implements Wiki
{
    private XWikiContext xcontext;

    private WikiReference reference;

    public BridgedWiki(WikiReference reference, XWikiContext xcontext)
    {
        this.xcontext = xcontext;
    }

    public Space addSpace(String spaceName)
    {
        throw new ModelException("Not supported");
    }

    public Space getSpace(String spaceName)
    {
        throw new ModelException("Not supported");
    }

    public List<Space> getSpaces()
    {
        throw new ModelException("Not supported");
    }

    public boolean hasSpace(String spaceName)
    {
        throw new ModelException("Not supported");
    }

    public void removeSpace(String spaceName)
    {
        throw new ModelException("Not supported");
    }

    public XWiki getXWiki()
    {
        return this.xcontext.getWiki();
    }

    public org.xwiki.model.Object addObject(String objectName)
    {
        throw new ModelException("Not supported");
    }

    public ObjectDefinition addObjectDefinition(String objectDefinitionName)
    {
        throw new ModelException("Not supported");
    }

    public List<Entity> getChildren(EntityType type)
    {
        throw new ModelException("Not supported");
    }

    public String getDescription()
    {
        throw new ModelException("Not supported");
    }

    public String getIdentifier()
    {
        throw new ModelException("Not supported");
    }

    public Object getObject(String objectName)
    {
        throw new ModelException("Not supported");
    }

    public ObjectDefinition getObjectDefinition(String objectDefinitionName)
    {
        throw new ModelException("Not supported");
    }

    public List<ObjectDefinition> getObjectDefinitions()
    {
        throw new ModelException("Not supported");
    }

    public List<Object> getObjects()
    {
        throw new ModelException("Not supported");
    }

    public <T> Entity getParent()
    {
        throw new ModelException("Not supported");
    }

    public EntityReference getReference()
    {
        throw new ModelException("Not supported");
    }

    public Entity getTarget()
    {
        throw new ModelException("Not supported");
    }

    public EntityType getType()
    {
        throw new ModelException("Not supported");
    }

    public boolean hasObject(String objectName)
    {
        throw new ModelException("Not supported");
    }

    public boolean hasObjectDefinition(String objectDefinitionName)
    {
        throw new ModelException("Not supported");
    }

    public void removeObject(String objectName)
    {
        throw new ModelException("Not supported");
    }

    public void removeObjectDefinition(String objectDefinitionName)
    {
        throw new ModelException("Not supported");
    }

    public void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters)
    {
        throw new ModelException("Not supported");
    }

    public XWikiContext getXWikiContext()
    {
        return this.xcontext;
    }

    public WikiReference getWikiReferene()
    {
        return this.reference;
    }
}
