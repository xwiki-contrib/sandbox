package org.xwiki.store.dao;

import java.util.Collection;

import org.xwiki.store.value.AttachmentId;
import org.xwiki.store.value.AttachmentValue;
import org.xwiki.store.value.DocumentId;

public interface AttachmentDao extends GenericDao<AttachmentId, AttachmentValue>
{
    Collection<AttachmentId> list(DocumentId docId);
}
