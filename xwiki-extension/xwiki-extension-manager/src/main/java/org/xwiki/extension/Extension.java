package org.xwiki.extension;

import java.io.File;
import java.util.List;

import org.xwiki.extension.repository.ExtensionRepository;

public interface Extension
{
    String getName();

    String getVersion();

    ExtensionType getType();

    String getDescription();

    String getWebSite();

    String getAuthor();
    
    /**
     * TODO: introduce ArtifactDependency when we will need version range, for now {@link ExtensionId#getVersion()} is
     * the minimum version (maven-like rule)
     */
    List<ExtensionId> getDependencies();
    
    void download(File file);
    
    ExtensionRepository getRepository();
}
