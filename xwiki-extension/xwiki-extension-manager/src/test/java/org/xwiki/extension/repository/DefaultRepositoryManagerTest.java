package org.xwiki.extension.repository;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultRepositoryManagerTest extends AbstractComponentTestCase
{
    private RepositoryManager repositoryManager;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.repositoryManager = getComponentManager().lookup(RepositoryManager.class);
    }

    @Test
    public void testFindArtifact() throws InterruptedException
    {
        ArtifactId artifactId = new ArtifactId("org.xwiki.platform:xwiki-core-rendering-macro-ruby", "2.4-SNAPSHOT");

        Artifact artifact = this.repositoryManager.findArtifact(artifactId);

        Assert.assertNotNull(artifact);
    }
}
