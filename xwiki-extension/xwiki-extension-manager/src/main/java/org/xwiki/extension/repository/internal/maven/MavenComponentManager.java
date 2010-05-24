package org.xwiki.extension.repository.internal.maven;

import org.codehaus.plexus.PlexusContainer;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface MavenComponentManager
{
    PlexusContainer getPlexus();
}
