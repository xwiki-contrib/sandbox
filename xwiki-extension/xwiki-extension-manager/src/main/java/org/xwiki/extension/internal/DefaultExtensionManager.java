package org.xwiki.extension.internal;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;

@Component
public class DefaultExtensionManager implements ExtensionManager, Initializable
{
    @Requirement
    private ExtensionRepositoryManager repositoryManager;

    @Requirement(role = ExtensionRepositoryFactory.class)
    private List<ExtensionRepositoryFactory> extensionRepositoryFactory;

    @Requirement
    private LocalExtensionRepository localExtensionRepository;

    public void initialize() throws InitializationException
    {
        for (ExtensionRepositoryFactory repositoryFactory : this.extensionRepositoryFactory) {
            for (ExtensionRepository repository : repositoryFactory.getDefaultExtensionRepositories()) {
                this.repositoryManager.addRepository(repository);
            }
        }
    }

    public int coundAvailableExtensions()
    {
        // TODO
        return 0;
    }

    public int coundInstalledExtensions()
    {
        // TODO
        return 0;
    }

    public List<Extension> getAvailableExtensions(int nb, int offset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<Extension> getInstalledExtensions(int nb, int offset)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void installExtension(ExtensionId extensionId)
    {
        Extension extension = this.repositoryManager.resolve(extensionId);

        this.localExtensionRepository.installExtension(extension);
    }

    public void installExtension(Extension extension)
    {
        this.localExtensionRepository.installExtension(extension);
    }

    public void uninstallExtension(ExtensionId extensionId)
    {
        Extension extension = this.localExtensionRepository.resolve(extensionId);
    }

    public void uninstallExtension(Extension extension)
    {
        this.localExtensionRepository.uninstallExtension(extension);
    }

}
