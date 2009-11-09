package org.xwiki.store.dao.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.store.dao.ObjectDao;
import org.xwiki.store.value.DocumentId;
import org.xwiki.store.value.ObjectId;
import org.xwiki.store.value.ObjectValue;

public class MemoryObjectDao implements ObjectDao
{
    Map<DocumentId, Set<ObjectId>> objectIds = new ConcurrentHashMap<DocumentId, Set<ObjectId>>();
    Map<ObjectId, ObjectValue> objects = new ConcurrentHashMap<ObjectId, ObjectValue>();

    public Collection<ObjectId> list(DocumentId docId)
    {
        Collection<ObjectId> res = objectIds.get(docId);
        if (res == null) {
            res = Collections.emptyList();
        } else {
            res = Collections.unmodifiableCollection(res);
        }
        return res;
    }

    public void delete(ObjectId id)
    {
        objects.remove(id);
        Set<ObjectId> objs = objectIds.get(id);
        if (objs != null) {
            objs.remove(id);
        }
    }

    public ObjectValue load(ObjectId id)
    {
        return objects.get(id);
    }

    public void save(ObjectValue entity)
    {
        ObjectId oId = entity.getId();
        objects.put(oId, entity);
        if (objectIds.get(oId.getDocumentId()) == null) {
            objectIds.put(oId.getDocumentId(), new HashSet<ObjectId>());
        }
        objectIds.get(oId).add(oId);
    }
}
