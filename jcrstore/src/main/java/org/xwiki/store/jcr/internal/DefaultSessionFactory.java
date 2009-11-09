package org.xwiki.store.jcr.internal;

import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.xwiki.store.jcr.RepositoryProvider;
import org.xwiki.store.jcr.SessionFactory;

public class DefaultSessionFactory implements SessionFactory
{
    protected RepositoryProvider repositoryProvider;

    protected String adminUsername = "admin";

    protected String adminPassword = "admin";

    protected Credentials adminCredentials = new SimpleCredentials(adminUsername, adminPassword.toCharArray());

    public Session getReadSession(String workspace) throws Exception
    {
        return repositoryProvider.getRepository().login(workspace);
    }

    public Session getWriteSession(String workspace) throws Exception
    {
        return repositoryProvider.getRepository().login(adminCredentials, workspace);
    }
}
