package org.xwiki.extension.repository;

import java.io.File;

public interface LocalRepository extends Repository
{
    File getFile(ArtifactId artifactId);
}
