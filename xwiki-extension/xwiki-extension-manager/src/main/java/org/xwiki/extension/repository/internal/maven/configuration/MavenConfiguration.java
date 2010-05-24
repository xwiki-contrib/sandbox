package org.xwiki.extension.repository.internal.maven.configuration;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Settings;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MavenConfiguration
{
    ArtifactRepository getLocalRepository() throws InvalidRepositoryException;
    
    Settings getSettings();
}
