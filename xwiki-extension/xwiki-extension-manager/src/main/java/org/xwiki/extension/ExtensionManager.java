package org.xwiki.extension;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface ExtensionManager
{
    List<Extension> getAvailableExtensions(int nb, int offset);

    List<Extension> getInstalledExtensions(int nb, int offset);

    int coundAvailableExtensions();

    int coundInstalledExtensions();

    void installExtension(ExtensionId extensionId);

    void uninstallExtension(ExtensionId extensionId);
}
