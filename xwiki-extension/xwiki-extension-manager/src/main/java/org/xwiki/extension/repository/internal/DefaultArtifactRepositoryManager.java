package org.xwiki.extension.repository.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;

@Component
public class DefaultArtifactRepositoryManager implements ExtensionRepositoryManager
{
    @Requirement
    private ComponentManager componentManager;

    Map<ExtensionRepositoryId, ExtensionRepository> repositories =
        new ConcurrentHashMap<ExtensionRepositoryId, ExtensionRepository>();

    public void addRepository(ExtensionRepositoryId repositoryId)
    {
        try {
            ExtensionRepositoryFactory repositoryFactory =
                this.componentManager.lookup(ExtensionRepositoryFactory.class, repositoryId.getType());

            addRepository(repositoryFactory.createRepository(repositoryId));
        } catch (ComponentLookupException e) {
            // TODO: throw exception
        }
    }

    public void addRepository(ExtensionRepository repository)
    {
        this.repositories.put(repository.getId(), repository);
    }

    public void removeRepository(ExtensionRepositoryId repositoryId)
    {
        this.repositories.remove(repositoryId);
    }

    public Extension resolve(ExtensionId artifactId)
    {
        Extension artifact = null;

        for (ExtensionRepository repository : this.repositories.values()) {
            artifact = repository.resolve(artifactId);

            if (artifact != null) {
                break;
            }
        }

        return artifact;
    }
}
