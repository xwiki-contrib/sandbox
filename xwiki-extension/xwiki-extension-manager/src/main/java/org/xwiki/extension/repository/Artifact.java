package org.xwiki.extension.repository;

public interface Artifact
{
    String getName();

    String getVersion();

    ArtifactType getType();
    
    Repository getRepository();
}
