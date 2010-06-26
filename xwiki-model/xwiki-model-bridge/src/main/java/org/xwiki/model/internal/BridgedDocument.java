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

import org.xwiki.model.*;
import org.xwiki.model.Object;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.syntax.Syntax;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BridgedDocument implements Document
{
    private com.xpn.xwiki.api.Document document;

    public BridgedDocument(com.xpn.xwiki.api.Document document)
    {
        this.document = document;
    }

    public Attachment addAttachment(String attachmentName)
    {
        throw new RuntimeException("Not supported");
    }

    public org.xwiki.model.Object addObject(String objectName)
    {
        throw new RuntimeException("Not supported");
    }

    public ObjectDefinition addObjectDefinition(String objectDefinitionName)
    {
        throw new RuntimeException("Not supported");
    }

    public Attachment getAttachment(String attachmentName)
    {
        throw new RuntimeException("Not supported");
    }

    public List<Attachment> getAttachments()
    {
        throw new RuntimeException("Not supported");
    }

    public String getContent()
    {
        throw new RuntimeException("Not supported");
    }

    public Locale getLocale()
    {
        throw new RuntimeException("Not supported");
    }

    public Object getObject(String objectName)
    {
        throw new RuntimeException("Not supported");
    }

    public ObjectDefinition getObjectDefinition(String objectDefinitionName)
    {
        throw new RuntimeException("Not supported");
    }

    public List<ObjectDefinition> getObjectDefinitions()
    {
        throw new RuntimeException("Not supported");
    }

    public List<Object> getObjects()
    {
        throw new RuntimeException("Not supported");
    }

    public Syntax getSyntax()
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasAttachment(String attachmentName)
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasObject(String objectName)
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasObjectDefinition(String objectDefinitionName)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeAttachment(String attachmentName)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeObject(String objectName)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeObjectDefinition(String objectDefinitionName)
    {
        throw new RuntimeException("Not supported");
    }

    public void setContent(String content)
    {
        throw new RuntimeException("Not supported");
    }

    public void setSyntax(Syntax syntax)
    {
        throw new RuntimeException("Not supported");
    }

    public List<Entity> getChildren()
    {
        throw new RuntimeException("Not supported");
    }

    public String getDescription()
    {
        throw new RuntimeException("Not supported");
    }

    public String getIdentifier()
    {
        throw new RuntimeException("Not supported");
    }

    public Entity getParent()
    {
        throw new RuntimeException("Not supported");
    }

    public EntityReference getReference()
    {
        throw new RuntimeException("Not supported");
    }

    public Entity getTarget()
    {
        throw new RuntimeException("Not supported");
    }

    public EntityType getType()
    {
        throw new RuntimeException("Not supported");
    }

    public void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters)
    {
        throw new RuntimeException("Not supported");
    }
}
