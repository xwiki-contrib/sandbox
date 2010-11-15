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
package org.xwiki.model;

import org.xwiki.model.reference.EntityReference;

import java.util.List;

public interface Entity extends Persistable
{
    /**
     * UUID
     */
    String getIdentifier();
    
    EntityType getType();

    /**
     * Represents a link to another Entity (Example use cases: renames, aliases)
     *
     * @return the targeted entity or null if the current Entity isn't a link but an actual object
     */
    Entity getTarget();

    EntityReference getReference();

    <T> Entity getParent();

    List<Entity> getChildren(EntityType type);

    /**
     * @todo Should not be implemented with the old model
     */
    String getDescription();

    // Add:
    // - last modification author
    // - last modified date
    // - pretty name

    /**
     * @return the list of object definitions defined inside this document
     */
    List<ObjectDefinition> getObjectDefinitions();

    ObjectDefinition getObjectDefinition(String objectDefinitionName);

    ObjectDefinition addObjectDefinition(String objectDefinitionName);

    void removeObjectDefinition(String objectDefinitionName);

    boolean hasObjectDefinition(String objectDefinitionName);

    List<Object> getObjects();

    Object getObject(String objectName);

    Object addObject(String objectName);

    void removeObject(String objectName);

    boolean hasObject(String objectName);
}
