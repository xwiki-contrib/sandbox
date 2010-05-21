package org.xwiki.extension.repository;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

@Component
public class DefaultRepositoryManager implements RepositoryManager
{
    @Requirement
    private ComponentManager componentManager;

    Map<RepositoryId, Repository> repositories = new HashMap<RepositoryId, Repository>();

    public void addRepository(RepositoryId repositoryId)
    {
        try {
            RepositoryFactory repositoryFactory =
                this.componentManager.lookup(RepositoryFactory.class, repositoryId.getType());

            this.repositories.put(repositoryId, repositoryFactory.createRepository(repositoryId));
        } catch (ComponentLookupException e) {
            // TODO: throw exception
        }
    }

    public void removeRepository(RepositoryId repositoryId)
    {
        this.repositories.remove(repositoryId);
    }

    public Artifact findArtifact(ArtifactId actifactId)
    {
        Artifact artifact = null;

        for (Repository repository : this.repositories.values()) {
            artifact = repository.findArtifact(actifactId);

            if (artifact != null) {
                break;
            }
        }

        return artifact;
    }
}
