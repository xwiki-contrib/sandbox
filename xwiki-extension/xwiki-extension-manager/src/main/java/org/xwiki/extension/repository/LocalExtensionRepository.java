package org.xwiki.extension.repository;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.LocalExtension;

@ComponentRole
public interface LocalExtensionRepository extends ExtensionRepository
{
    List<LocalExtension> getLocalExtensions(int nb, int offset);

    LocalExtension getLocalExtension(ExtensionId extensionId);

    void installExtension(Extension extension);

    void uninstallExtension(Extension extension);
}
