package org.xwiki.store.jcr.internal;

import javax.jcr.Repository;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.store.jcr.RepositoryProvider;

/**
 * Not worked yet. Configuration missing.
 */
public class ExoStandaloneRepositoryProvider implements RepositoryProvider, Initializable
{
    protected ManageableRepository repository;

    public Repository getRepository() throws Exception
    {
        return repository;
    }

    public void shutdown()
    {
    }

    public void initialize() throws InitializationException
    {
        try {
            RepositoryService repositoryService = (RepositoryService) StandaloneContainer.getInstance(getClass().getClassLoader()).getComponentInstanceOfType(RepositoryService.class);
            repository = repositoryService.getDefaultRepository();
        } catch (Exception e) {
            throw new InitializationException("Can't initialize exo container", e);
        }
    }
}
