package org.xwiki.extension.repository;

public interface RepositoryFactory
{
    Repository createRepository(RepositoryId repositoryId);
}
