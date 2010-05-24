package org.xwiki.extension.repository;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultRepositoryManagerTest extends AbstractComponentTestCase
{
    private ExtensionRepositoryManager repositoryManager;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.repositoryManager = getComponentManager().lookup(ExtensionRepositoryManager.class);
    }

    @Test
    public void testFindArtifact() throws InterruptedException
    {
        ExtensionId artifactId = new ExtensionId("org.xwiki.platform:xwiki-core-rendering-macro-ruby", "2.4-SNAPSHOT");

        Extension artifact = this.repositoryManager.resolve(artifactId);

        Assert.assertNotNull(artifact);
    }
}
