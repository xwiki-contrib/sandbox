package org.xwiki.extension.repository;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface ExtensionRepositoryFactory
{
    List<ExtensionRepository> getDefaultExtensionRepositories();

    ExtensionRepository createRepository(ExtensionRepositoryId repositoryId);
}
