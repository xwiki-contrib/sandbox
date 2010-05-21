package org.xwiki.extension.repository.internal.maven;

import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.WagonManager;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.repository.Repository;
import org.xwiki.extension.repository.RepositoryFactory;
import org.xwiki.extension.repository.RepositoryId;

public class MavenRepositoryFactory extends AbstractLogEnabled implements RepositoryFactory, Initializable
{
    /**
     * In-process maven runtime.
     */
    private MutablePlexusContainer plexus;

    private WagonManager wagonManager;

    private ArtifactRepositoryFactory artifactRepositoryFactory;

    private RepositorySystem repositorySystem;

    public void initialize() throws InitializationException
    {
        try {
            initializePlexus();
        } catch (PlexusContainerException e) {
            throw new InitializationException("Failed to initialize Maven", e);
        }

        try {
            this.wagonManager = this.plexus.lookup(WagonManager.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to lookup WagonManager", e);
        }

        try {
            this.artifactRepositoryFactory = this.plexus.lookup(ArtifactRepositoryFactory.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to lookup ArtifactRepositoryFactory", e);
        }

        try {
            this.repositorySystem = this.plexus.lookup(RepositorySystem.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to lookup RepositorySystem", e);
        }
    }

    private void initializePlexus() throws PlexusContainerException
    {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration mavenCoreCC =
            new DefaultContainerConfiguration().setClassWorld(
                new ClassWorld(mavenCoreRealmId, ClassWorld.class.getClassLoader())).setName("mavenCore");

        this.plexus = new DefaultPlexusContainer(mavenCoreCC);
        this.plexus.setLoggerManager(new XWikiLoggerManager(getLogger()));
    }

    public Repository createRepository(RepositoryId repositoryId)
    {
        return new MavenRepository(this.artifactRepositoryFactory.createArtifactRepository(repositoryId.getId(),
            repositoryId.getUrl().toExternalForm(), (ArtifactRepositoryLayout) null, new ArtifactRepositoryPolicy(),
            new ArtifactRepositoryPolicy()), this.wagonManager);
    }
}
