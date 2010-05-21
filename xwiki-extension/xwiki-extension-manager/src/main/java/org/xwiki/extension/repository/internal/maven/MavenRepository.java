package org.xwiki.extension.repository.internal.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.legacy.WagonManager;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.repository.Artifact;
import org.xwiki.extension.repository.Repository;

public class MavenRepository implements Repository
{
    private WagonManager wagonManager;

    private ArtifactRepository repository;

    public MavenRepository(ArtifactRepository repository, WagonManager wagonManager)
    {
        this.repository = repository;
        this.wagonManager = wagonManager;
    }

    public List<Artifact> getArtefacts(int offset, int nb)
    {
        return null;
    }

    void getRemoteFile(Artifact artifact, File destination)
    {
        this.wagonManager.getRemoteFile(this.repository, destination, remotePath, downloadMonitor, checksumPolicy, false);
    }
}
