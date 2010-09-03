package org.xwiki.extension.repository.internal.maven.configuration;

import java.io.File;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Settings;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MavenConfiguration
{
    File getLocalRepository();

    ArtifactRepository getLocalArtifactRepository() throws InvalidRepositoryException;

    Settings getSettings();
}
