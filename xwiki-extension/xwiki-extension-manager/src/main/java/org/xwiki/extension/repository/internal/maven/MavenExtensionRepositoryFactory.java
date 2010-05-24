package org.xwiki.extension.repository.internal.maven;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.internal.maven.configuration.MavenConfiguration;

@ComponentRole
public class MavenExtensionRepositoryFactory extends AbstractLogEnabled implements ExtensionRepositoryFactory,
    Initializable
{
    @Requirement
    private MavenComponentManager mavenComponentManager;

    @Requirement
    private MavenConfiguration mavenConfiguration;

    private org.apache.maven.artifact.repository.ArtifactRepositoryFactory artifactRepositoryFactory;

    public void initialize() throws InitializationException
    {
        try {
            this.artifactRepositoryFactory =
                this.mavenComponentManager.getPlexus().lookup(
                    org.apache.maven.artifact.repository.ArtifactRepositoryFactory.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to lookup ArtifactRepositoryFactory", e);
        }
    }

    public List<ExtensionRepository> getDefaultExtensionRepositories()
    {
        List<ExtensionRepository> repositories = new ArrayList<ExtensionRepository>();

        // TODO: get maven default repositories

        // Get configured repositories
        for (Profile profile : this.mavenConfiguration.getSettings().getProfiles()) {
            for (Repository repository : profile.getRepositories()) {
                try {
                    repositories.add(createRepository(new ExtensionRepositoryId(repository.getId(), "maven", new URI(
                        repository.getUrl()))));
                } catch (URISyntaxException e) {
                    getLogger().error("Failed to create maven repository from maven settings", e);
                }
            }
        }

        return repositories;
    }

    public ExtensionRepository createRepository(ExtensionRepositoryId repositoryId)
    {
        return new MavenExtensionRepository(repositoryId, this.artifactRepositoryFactory.createArtifactRepository(
            repositoryId.getId(), repositoryId.getURI().toString(), (ArtifactRepositoryLayout) null,
            new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy()), this.mavenComponentManager);
    }
}
