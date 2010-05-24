package org.xwiki.extension.repository.internal.maven;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

@Component
public class DefaultMavenComponentManager extends AbstractLogEnabled implements MavenComponentManager, Initializable
{
    /**
     * In-process maven runtime.
     */
    private MutablePlexusContainer plexusContainer;

    public void initialize() throws InitializationException
    {
        try {
            initializePlexus();
        } catch (PlexusContainerException e) {
            throw new InitializationException("Failed to initialize Maven", e);
        }
    }

    private void initializePlexus() throws PlexusContainerException
    {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration mavenCoreCC =
            new DefaultContainerConfiguration().setClassWorld(
                new ClassWorld(mavenCoreRealmId, ClassWorld.class.getClassLoader())).setName("mavenCore");

        this.plexusContainer = new DefaultPlexusContainer(mavenCoreCC);
        this.plexusContainer.setLoggerManager(new XWikiLoggerManager(getLogger()));
    }

    public PlexusContainer getPlexus()
    {
        return plexusContainer;
    }
}
