package org.xwiki.store.dao.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.store.dao.DocumentDao;
import org.xwiki.store.value.DocumentId;
import org.xwiki.store.value.DocumentValue;

public class MemoryDocumentDao implements DocumentDao
{
    protected Map<DocumentId, DocumentValue> documents = new ConcurrentHashMap<DocumentId, DocumentValue>();

    public void delete(DocumentId id)
    {
        documents.remove(id);
    }

    public DocumentValue load(DocumentId id)
    {
        return documents.get(id);
    }

    public void save(DocumentValue entity)
    {
        documents.put(entity.getId(), entity);
    }
}
