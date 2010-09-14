package org.xwiki.extension.install;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.extension.LocalExtension;

@ComponentRole
public interface ExtensionInstallerManager
{
    void install(LocalExtension localExtension) throws ExtensionInstallerException;
}
