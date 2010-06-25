package org.xwiki.model.internal;

import org.xwiki.model.Entity;
import org.xwiki.model.Server;
import org.xwiki.model.Wiki;
import org.xwiki.model.reference.EntityReference;

import java.util.List;
import java.util.Map;

public class BridgedServer implements Server
{
    public Wiki addWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public Entity getEntity(EntityReference reference)
    {
        throw new RuntimeException("Not supported");
    }

    public Wiki getWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public List<Wiki> getWikis()
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasEntity(EntityReference reference)
    {
        throw new RuntimeException("Not supported");
    }

    public boolean hasWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeEntity(EntityReference reference)
    {
        throw new RuntimeException("Not supported");
    }

    public void removeWiki(String wikiName)
    {
        throw new RuntimeException("Not supported");
    }

    public void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters)
    {
        throw new RuntimeException("Not supported");
    }
}
