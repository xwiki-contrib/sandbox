package org.xwiki.extension.repository.internal.maven.configuration;

import java.io.File;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.repository.internal.maven.MavenComponentManager;

@Component
public class DefaultMavenConfiguration extends AbstractLogEnabled implements MavenConfiguration, Initializable
{
    @Requirement
    private MavenComponentManager mavenComponentManager;

    private RepositorySystem repositorySystem;

    private SettingsBuilder settingsBuilder;

    public void initialize() throws InitializationException
    {
        try {
            this.repositorySystem = this.mavenComponentManager.getPlexus().lookup(RepositorySystem.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to lookup ArtifactRepositoryFactory", e);
        }
    }

    public ArtifactRepository getLocalRepository() throws InvalidRepositoryException
    {
        String localRepositoryPath = getSettings().getLocalRepository();
        if (localRepositoryPath != null) {
            return this.repositorySystem.createLocalRepository(new File(localRepositoryPath));
        }

        return this.repositorySystem.createLocalRepository(RepositorySystem.defaultUserLocalRepository);
    }

    public Settings getSettings()
    {
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setGlobalSettingsFile(new File(globalPath));
        request.setUserSettingsFile(new File(userPath));

        try {
            return this.settingsBuilder.build(request).getEffectiveSettings();
        } catch (SettingsBuildingException ex) {
            getLogger().error("Could not read settings.xml, assuming default values", ex);
            /*
             * NOTE: This method provides input for various other core functions, just bailing out would make m2e highly
             * unusuable. Instead, we fail gracefully and just ignore the broken settings, using defaults.
             */
            return new Settings();
        }
    }
}
