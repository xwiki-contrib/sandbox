package org.xwiki.store.jcr;

import javax.jcr.Session;

public interface SessionFactory
{
    String ROLE = SessionFactory.class.getName(); 

    Session getReadSession(String workspace) throws Exception;

    Session getWriteSession(String workspace) throws Exception;
}
