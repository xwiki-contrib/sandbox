package org.xwiki.extension.repository.internal.aether.configuration;

import java.io.File;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Settings;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface AetherConfiguration
{
    File getLocalRepository();

    ArtifactRepository getLocalArtifactRepository() throws InvalidRepositoryException;

    Settings getSettings();
}
