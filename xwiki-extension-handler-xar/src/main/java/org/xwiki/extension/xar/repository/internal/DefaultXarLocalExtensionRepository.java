package org.xwiki.extension.xar.repository.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.xar.LocalPage;
import org.xwiki.extension.xar.XarLocalExtension;
import org.xwiki.extension.xar.internal.DefaultXarLocalExtension;
import org.xwiki.extension.xar.repository.XarLocalExtensionRepository;

public class DefaultXarLocalExtensionRepository implements XarLocalExtensionRepository, Initializable
{
    @Requirement
    private LocalExtensionRepository localExtensionRepository;

    private Map<ExtensionId, XarLocalExtension> extensions;

    // TODO: replace with a filesystem based index for scalability
    private Map<LocalPage, List<XarLocalExtension>> extensionsByPage;

    public DefaultXarLocalExtensionRepository(LocalExtensionRepository localExtensionRepository)
    {
        this.localExtensionRepository = localExtensionRepository;
    }

    public void initialize() throws InitializationException
    {
        for (LocalExtension localExtension : this.localExtensionRepository.getLocalExtensions()) {
            XarLocalExtension xarLocalExtension = new DefaultXarLocalExtension(localExtension, this);

            this.extensions.put(xarLocalExtension.getId(), xarLocalExtension);

            for (LocalPage localPage : xarLocalExtension.getPages()) {
                List<XarLocalExtension> pageExtensions = this.extensionsByPage.get(localPage);

                if (pageExtensions == null) {
                    pageExtensions = new ArrayList<XarLocalExtension>();
                    this.extensionsByPage.put(localPage, pageExtensions);
                }

                pageExtensions.add(xarLocalExtension);
            }
        }
    }

    // LocalExtensionRepository

    public ExtensionRepositoryId getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        return this.localExtensionRepository.resolve(extensionId);
    }

    public boolean exists(ExtensionId extensionId)
    {
        return this.localExtensionRepository.exists(extensionId);
    }

    public int countExtensions()
    {
        return this.localExtensionRepository.countExtensions();
    }

    public Collection< ? extends Extension> getExtensions(int nb, int offset)
    {
        return this.localExtensionRepository.getExtensions(nb, offset);
    }

    public Collection<LocalExtension> getLocalExtensions()
    {
        return this.localExtensionRepository.getLocalExtensions();
    }

    public LocalExtension getLocalExtension(String id)
    {
        return this.localExtensionRepository.getLocalExtension(id);
    }

    public LocalExtension installExtension(Extension extension, boolean dependency, String namespace)
        throws InstallException
    {
        return this.localExtensionRepository.installExtension(extension, dependency, namespace);
    }

    public void uninstallExtension(LocalExtension extension, String namespace) throws UninstallException
    {
        this.localExtensionRepository.uninstallExtension(extension, namespace);
    }

    public Collection<LocalExtension> getBackwardDependencies(String id, String namespace) throws ResolveException
    {
        return this.localExtensionRepository.getBackwardDependencies(id, namespace);
    }

    public Collection<LocalExtension> getBackwardDependencies(ExtensionId id) throws ResolveException
    {
        return this.localExtensionRepository.getBackwardDependencies(id);
    }

    // XarLocalExtensionRepository

    public Collection<XarLocalExtension> getExensionsByPage(LocalPage page)
    {
        return this.extensionsByPage.get(page);
    }

    public XarLocalExtension getXarExtension(LocalExtension extension, String namespace)
    {
        return this.extensions.get(extension);
    }
}
