package org.xwiki.store.dao.internal;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.set.MapBackedSet;
import org.xwiki.store.dao.WikiDao;

public class MemoryWikiDao implements WikiDao
{
    @SuppressWarnings("unchecked")
    protected Set<String> wikis = MapBackedSet.decorate(new ConcurrentHashMap<String, Object>());

    public void delete(String entity)
    {
        wikis.remove(entity);
    }

    public boolean exist(String wiki)
    {
        return wikis.contains(wiki);
    }
    
    public void create(String wiki)
    {
        wikis.add(wiki);
    }
}
