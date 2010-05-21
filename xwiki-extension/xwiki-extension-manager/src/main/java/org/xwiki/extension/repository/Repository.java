package org.xwiki.extension.repository;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface Repository
{
    Artifact findArtifact(ArtifactId actifactId);
}
