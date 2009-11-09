package org.xwiki.store.jcr;

import javax.jcr.Repository;

public interface RepositoryProvider
{
    String ROLE = RepositoryProvider.class.getName();

    Repository getRepository() throws Exception;

    void shutdown();
}
