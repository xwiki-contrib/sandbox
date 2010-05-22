package org.xwiki.extension.repository;

import java.io.File;
import java.util.List;

public interface Artifact
{
    ArtifactId getId();

    ArtifactType getType();

    List<ArtifactId> getDependencies();

    void download(File file);
}
