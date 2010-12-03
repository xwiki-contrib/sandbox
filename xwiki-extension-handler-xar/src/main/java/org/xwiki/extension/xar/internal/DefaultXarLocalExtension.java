package org.xwiki.extension.xar.internal;

import java.util.List;

import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.WrappingLocalException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.xar.LocalPage;
import org.xwiki.extension.xar.XarLocalExtension;
import org.xwiki.extension.xar.repository.XarLocalExtensionRepository;

public class DefaultXarLocalExtension extends WrappingLocalException implements XarLocalExtension
{
    private XarLocalExtensionRepository xarLocalRepository;

    private List<LocalPage> pages;

    public DefaultXarLocalExtension(LocalExtension localExtension, XarLocalExtensionRepository repository)
    {
        super(localExtension);

        this.xarLocalRepository = repository;
    }

    private void loadPages()
    {
        // TODO
    }

    // Extension

    public ExtensionRepository getRepository()
    {
        return this.xarLocalRepository;
    }

    // XarLocalExtension

    public List<LocalPage> getPages()
    {
        return this.pages;
    }
}
