package org.xwiki.model;

import org.xwiki.model.reference.EntityReference;

public interface VersionManager
{
    Entity getEntity(EntityReference reference, Version version);
}
