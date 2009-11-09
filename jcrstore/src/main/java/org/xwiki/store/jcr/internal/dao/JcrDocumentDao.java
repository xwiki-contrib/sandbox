package org.xwiki.store.jcr.internal.dao;

import javax.jcr.Node;
import javax.jcr.Session;

import org.jcrom.Jcrom;
import org.xwiki.store.dao.DocumentDao;
import org.xwiki.store.jcr.JcrTemplate;
import org.xwiki.store.jcr.JcromProvider;
import org.xwiki.store.jcr.JcrTemplate.JcrCallback;
import org.xwiki.store.jcr.internal.JcrUtil;
import org.xwiki.store.value.DocumentId;
import org.xwiki.store.value.DocumentValue;

public class JcrDocumentDao implements DocumentDao
{
    private JcrTemplate jcrTemplate;
    private JcromProvider jcromProvider;

    String getJcrPath(DocumentId docId, boolean withLanguage)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('/').append(docId.getSpace())
            .append('/').append(docId.getName());
        if (withLanguage && docId.getLanguage() != null) {
            sb.append('/').append(docId.getLanguage());
        }
        return sb.toString();
    }

    public void delete(final DocumentId id) throws Exception
    {
        getJcrTemplate().executeWrite(new JcrCallback<Void>() {
            public Void execute(Session session) throws Exception
            {
                session.getItem(getJcrPath(id, true)).remove();
                return null;
            }
        });
    }

    public DocumentValue load(final DocumentId id) throws Exception
    {
        return getJcrTemplate().executeRead(new JcrCallback<DocumentValue>() {
            public DocumentValue execute(Session session) throws Exception
            {
                Node node = (Node) session.getItem(getJcrPath(id, true));
                return getJcrom().fromNode(DocumentValue.class, node);
            }
        });
    }

    public void save(final DocumentValue entity) throws Exception
    {
        getJcrTemplate().executeWrite(new JcrCallback<Void>() {
            public Void execute(Session session) throws Exception
            {
                Node node = JcrUtil.createNodeHierarhy(session.getRootNode(), getJcrPath(entity.getId(), true));
                getJcrom().updateNode(node, entity);
                return null;
            }
        });
    }

    protected Jcrom getJcrom()
    {
        return jcromProvider.getJcrom();
    }

    protected JcrTemplate getJcrTemplate()
    {
        return jcrTemplate;
    }
}
