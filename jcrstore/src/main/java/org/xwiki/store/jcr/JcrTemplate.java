package org.xwiki.store.jcr;

import javax.jcr.Session;

public interface JcrTemplate
{
    String ROLE = JcrTemplate.class.getName();

    <T> T executeRead(JcrCallback<T> callback) throws Exception;

    <T> T executeWrite(JcrCallback<T> callback) throws Exception;

    public interface JcrCallback<T> {
        T execute(Session session) throws Exception;
    }
}
