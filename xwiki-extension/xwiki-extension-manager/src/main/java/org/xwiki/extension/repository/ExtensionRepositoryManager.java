package org.xwiki.extension.repository;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;

@ComponentRole
public interface ExtensionRepositoryManager
{
    void addRepository(ExtensionRepositoryId repositoryId);

    void addRepository(ExtensionRepository repository);

    void removeRepository(ExtensionRepositoryId repositoryId);

    Extension resolve(ExtensionId artifactId);
}
