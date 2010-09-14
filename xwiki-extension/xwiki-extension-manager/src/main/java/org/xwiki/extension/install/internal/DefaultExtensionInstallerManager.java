package org.xwiki.extension.install.internal;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.install.ExtensionInstaller;
import org.xwiki.extension.install.ExtensionInstallerException;
import org.xwiki.extension.install.ExtensionInstallerManager;

@Component
public class DefaultExtensionInstallerManager implements ExtensionInstallerManager
{
    @Requirement
    private ComponentManager componentManager;

    public void install(LocalExtension localExtension) throws ExtensionInstallerException
    {
        try {
            // Load extension
            ExtensionInstaller extensionInstaller =
                this.componentManager.lookup(ExtensionInstaller.class, localExtension.getType().toString()
                    .toLowerCase());

            extensionInstaller.install(localExtension);
        } catch (ComponentLookupException e) {
            throw new ExtensionInstallerException("Can't find any extension installer for the extension type ["
                + localExtension + "]");
        }
    }

}
