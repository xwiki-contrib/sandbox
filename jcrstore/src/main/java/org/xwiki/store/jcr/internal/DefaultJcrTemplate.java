package org.xwiki.store.jcr.internal;

import javax.jcr.Session;

import org.xwiki.store.jcr.JcrTemplate;
import org.xwiki.store.jcr.SessionFactory;

public class DefaultJcrTemplate implements JcrTemplate
{
    protected SessionFactory sessionFactory;

    public <T> T executeRead(JcrCallback<T> callback) throws Exception
    {
        Session session = sessionFactory.getReadSession(getCurrentWorkspace());
        try {
            return callback.execute(session);
        } finally {
            session.logout();
        }
    }

    public <T> T executeWrite(JcrCallback<T> callback) throws Exception
    {
        Session session = sessionFactory.getWriteSession(getCurrentWorkspace());
        try {
            T result = callback.execute(session);
            session.save();
            return result;
        } finally {
            session.logout();
        }
    }

    protected String getCurrentWorkspace()
    {
        //TODO:
        return null;
    }
}
