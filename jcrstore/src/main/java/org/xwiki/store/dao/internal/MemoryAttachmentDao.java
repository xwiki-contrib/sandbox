package org.xwiki.store.dao.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.store.dao.AttachmentDao;
import org.xwiki.store.value.AttachmentId;
import org.xwiki.store.value.AttachmentValue;
import org.xwiki.store.value.DocumentId;

public class MemoryAttachmentDao implements AttachmentDao
{
    protected Map<DocumentId, Set<AttachmentId>> attachmentIds = new ConcurrentHashMap<DocumentId, Set<AttachmentId>>();
    protected Map<AttachmentId, AttachmentValue> attachments = new ConcurrentHashMap<AttachmentId, AttachmentValue>(); 

    public void delete(AttachmentId id)
    {
        attachments.remove(id);
        Set<AttachmentId> atts = attachmentIds.get(id.getDocumentId());
        if (atts != null) {
            atts.remove(id);
        }
    }

    public AttachmentValue load(AttachmentId id)
    {
        return attachments.get(id);
    }

    public void save(AttachmentValue entity)
    {
        AttachmentId aId = entity.getId();
        attachments.put(aId, entity);
        if (attachmentIds.get(aId.getDocumentId()) == null) {
            attachmentIds.put(aId.getDocumentId(), new HashSet<AttachmentId>());
        }
        attachmentIds.get(aId.getDocumentId()).add(aId);
    }

    public Collection<AttachmentId> list(DocumentId docId)
    {
        Collection<AttachmentId> res = attachmentIds.get(docId);
        if (res != null) {
            res = Collections.unmodifiableCollection(res);
        } else {
            res = Collections.emptyList();
        }
        return res;
    }
}
