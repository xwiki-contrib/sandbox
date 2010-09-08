package org.xwiki.extension.test;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.repository.internal.DefaultCoreExtensionRepository;

@Component
public class ConfigurableDefaultCoreExtensionRepository extends DefaultCoreExtensionRepository
{
    public void addExtensions(CoreExtension extension)
    {
        this.extensions.put(extension.getName(), extension);
    }
}
