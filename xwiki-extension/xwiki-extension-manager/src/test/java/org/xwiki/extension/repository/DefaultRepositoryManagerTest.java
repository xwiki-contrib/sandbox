package org.xwiki.extension.repository;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionType;
import org.xwiki.extension.ResolveException;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultRepositoryManagerTest extends AbstractComponentTestCase
{
    private ExtensionRepositoryManager repositoryManager;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.repositoryManager = getComponentManager().lookup(ExtensionRepositoryManager.class);
        
        this.repositoryManager.addRepository(new ExtensionRepositoryId("xwiki-releases", "maven", new URI("http://maven.xwiki.org/releases/")));
    }

    @Test
    public void testResolve() throws ResolveException
    {
        ExtensionId artifactId = new ExtensionId("org.xwiki.platform:xwiki-core-rendering-macro-ruby", "2.3.1");

        Extension artifact = this.repositoryManager.resolve(artifactId);

        Assert.assertNotNull(artifact);
        Assert.assertEquals("org.xwiki.platform:xwiki-core-rendering-macro-ruby", artifact.getName());
        Assert.assertEquals("2.3.1", artifact.getVersion());
        Assert.assertEquals(ExtensionType.JAR, artifact.getType());
        Assert.assertEquals(new ExtensionId("org.jruby:jruby", "1.3.0"), artifact.getDependencies().get(1));
    }
}
