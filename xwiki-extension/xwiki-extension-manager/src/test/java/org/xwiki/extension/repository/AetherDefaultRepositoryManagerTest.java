package org.xwiki.extension.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionType;
import org.xwiki.extension.ResolveException;
import org.xwiki.test.AbstractComponentTestCase;

public class AetherDefaultRepositoryManagerTest extends AbstractComponentTestCase
{
    private ExtensionRepositoryManager repositoryManager;

    private ExtensionId rubyArtifactId;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.repositoryManager = getComponentManager().lookup(ExtensionRepositoryManager.class);

        this.repositoryManager.addRepository(new ExtensionRepositoryId("xwiki-releases", "maven", new URI(
            "http://maven.xwiki.org/releases/")));

        this.rubyArtifactId = new ExtensionId("org.xwiki.platform:xwiki-core-rendering-macro-ruby", "2.3.1");
    }

    @Test
    public void testResolve() throws ResolveException
    {
        Extension artifact = this.repositoryManager.resolve(this.rubyArtifactId);

        Assert.assertNotNull(artifact);
        Assert.assertEquals("org.xwiki.platform:xwiki-core-rendering-macro-ruby", artifact.getName());
        Assert.assertEquals("2.3.1", artifact.getVersion());
        Assert.assertEquals(ExtensionType.JAR, artifact.getType());

        ExtensionDependency dependency = artifact.getDependencies().get(1);
        Assert.assertEquals("org.jruby:jruby", dependency.getName());
        Assert.assertEquals("1.3.0", dependency.getVersion());
    }

    @Test
    public void testDownload() throws ExtensionException, IOException
    {
        Extension artifact = this.repositoryManager.resolve(this.rubyArtifactId);

        File file = new File("target/downloaded/rubymacro.jar");

        if (file.exists()) {
            file.delete();
        }

        artifact.download(file);

        Assert.assertTrue("File has not been downloaded", file.exists());

        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

        boolean found = false;

        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            if (entry.getName().equals("org/xwiki/rendering/internal/macro/ruby/RubyMacro.class")) {
                found = true;
                break;
            }
        }

        if (!found) {
            Assert.fail("Does not seems to be the right file");
        }

        zis.close();
    }
}
