/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension.repository.internal.maven;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.internal.maven.configuration.MavenConfiguration;

@Component("maven")
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

        // Get configured repositories
        for (Profile profile : this.mavenConfiguration.getSettings().getProfiles()) {
            for (Repository repository : profile.getRepositories()) {
                try {
                    repositories.add(createRepository(new ExtensionRepositoryId(repository.getId(), "maven", new URI(
                        repository.getUrl()))));
                } catch (Exception e) {
                    getLogger().error("Failed to create maven repository from maven settings", e);
                }
            }
        }

        return repositories;
    }

    public ExtensionRepository createRepository(ExtensionRepositoryId repositoryId) throws ExtensionRepositoryException
    {
        try {
            return new MavenExtensionRepository(repositoryId, this.artifactRepositoryFactory.createArtifactRepository(
                repositoryId.getId(), repositoryId.getURI().toString(), ArtifactRepositoryFactory.DEFAULT_LAYOUT_ID,
                new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy()), this.mavenConfiguration,
                this.mavenComponentManager);
        } catch (Exception e) {
            throw new ExtensionRepositoryException("Failed to create repository [" + repositoryId + "]", e);
        }
    }
}
