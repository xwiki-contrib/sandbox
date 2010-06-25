package org.xwiki.model;

public interface Entity extends Persistable
{
    /**
     * UUID
     */
    String getIdentifier();
    
    EntityType getType();

    /**
     * All Entities have a name.
     */
    String getName();

    // Q: Do we need this? Should this be typed instead, ie for example Space getParent() in a Document Entity? 
    void setParent(Entity parentEntity);
    Entity getParent();
}
