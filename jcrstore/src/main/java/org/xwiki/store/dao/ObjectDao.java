package org.xwiki.store.dao;

import java.util.Collection;

import org.xwiki.store.value.DocumentId;
import org.xwiki.store.value.ObjectId;
import org.xwiki.store.value.ObjectValue;

public interface ObjectDao extends GenericDao<ObjectId, ObjectValue>
{
    Collection<ObjectId> list(DocumentId docId);
}
