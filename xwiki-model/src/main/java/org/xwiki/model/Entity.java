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
     * Represents a link to another Entity.
     *
     * @return the targetted entity or null if the current Entity isn't a link but an actual object  
     */
    Entity getTarget();

    EntityReference getReference();

    Entity getParent();

    List<Entity> getChildren();

    /**
     * @return the space's description
     * @todo Should not be implemented with the old model
     */
    String getDescription();

    // Add:
    // - author
    // - creation date
    // - last modified date
}
