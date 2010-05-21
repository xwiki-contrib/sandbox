package org.xwiki.extension.repository.internal.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.WagonManager;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.repository.Artifact;
import org.xwiki.extension.repository.ArtifactId;
import org.xwiki.extension.repository.Repository;
import org.xwiki.extension.repository.RepositoryId;

public class MavenRepository implements Repository
{
    private RepositoryId repositoryId;

    private PlexusContainer plexus;

    private ArtifactRepository repository;

    private RepositorySystem repositorySystem;

    public MavenRepository(RepositoryId repositoryId, ArtifactRepository repository, PlexusContainer plexus)
    {
        this.repositoryId = repositoryId;
        this.plexus = plexus;
        this.repository = repository;
    }

    public RepositoryId getId()
    {
        return this.repositoryId;
    }

    public Artifact findArtifact(ArtifactId actifactId)
    {
        Artifact artifact = null;

        // TODO: parse actifactId id to get group and artifact ids

        org.apache.maven.artifact.Artifact pomArtifact =
            this.repositorySystem.createProjectArtifact(groupId, artifactId, actifactId.getVersion());

        pomArtifact = this.repository.find(pomArtifact);

        // TODO: RepositorySystem#find does not really seems to try to find anything but just construct the the remote
        // path, need to be checked

        if (pomArtifact != null) {
            artifact = new MavenArtifact(pomArtifact, this);
        }

        return artifact;
    }
}
