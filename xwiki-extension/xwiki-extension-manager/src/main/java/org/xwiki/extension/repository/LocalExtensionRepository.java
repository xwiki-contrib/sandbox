package org.xwiki.extension.repository;

import java.io.File;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.extension.Extension;

@ComponentRole
public interface LocalExtensionRepository extends ExtensionRepository
{
    File getFile(Extension extension);

    void installExtension(Extension extension);

    void uninstallExtension(Extension extension);
}
