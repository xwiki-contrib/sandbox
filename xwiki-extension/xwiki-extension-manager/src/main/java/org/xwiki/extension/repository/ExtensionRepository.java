package org.xwiki.extension.repository;

import java.util.List;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;

public interface ExtensionRepository
{
    ExtensionRepositoryId getId();

    Extension resolve(ExtensionId extensionId);
    
    List<Extension> getExtensions(int nb, int offset);
}
