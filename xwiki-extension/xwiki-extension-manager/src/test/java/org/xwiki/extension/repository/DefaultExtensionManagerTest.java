package org.xwiki.extension.repository;

import java.net.URI;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.internal.DefaultCoreExtension;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultExtensionManagerTest extends AbstractComponentTestCase
{
    private ExtensionRepositoryManager repositoryManager;

    private ExtensionManager extensionManager;

    private ExtensionId rubyArtifactId;

    private ConfigurableDefaultCoreExtensionRepository coreExtensionRepository;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        getConfigurationSource().setProperty("extension.aether.localRepository",
            "target/DefaultExtensionManagerTest/test-aether-repository");
        getConfigurationSource().setProperty("extension.localRepository",
            "target/DefaultExtensionManagerTest/test-repository");

        this.repositoryManager = getComponentManager().lookup(ExtensionRepositoryManager.class);

        this.repositoryManager.addRepository(new ExtensionRepositoryId("xwiki-releases", "maven", new URI(
            "http://maven.xwiki.org/releases/")));
        this.repositoryManager.addRepository(new ExtensionRepositoryId("central", "maven", new URI(
            "http://repo1.maven.org/maven2/")));

        this.rubyArtifactId = new ExtensionId("org.xwiki.platform:xwiki-core-rendering-macro-ruby", "2.4");

        this.extensionManager = getComponentManager().lookup(ExtensionManager.class);
        this.coreExtensionRepository =
            (ConfigurableDefaultCoreExtensionRepository) getComponentManager().lookup(CoreExtensionRepository.class);
    }

    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        DefaultComponentDescriptor<CoreExtensionRepository> componentDescriptor =
            new DefaultComponentDescriptor<CoreExtensionRepository>();
        componentDescriptor.setImplementation(ConfigurableDefaultCoreExtensionRepository.class);
        componentDescriptor.setRole(CoreExtensionRepository.class);

        getComponentManager().registerComponent(componentDescriptor);
    }

    @Test
    public void testInstallExtension() throws ComponentLookupException, Exception
    {
        // way too big for a unit test so lets skip it
        this.coreExtensionRepository.addExtensions(new DefaultCoreExtension("org.jruby:jruby", "1.5"));
        
        // emulate environment
        registerMockComponent(DocumentAccessBridge.class);
        registerMockComponent(AttachmentReferenceResolver.class, "current");

        // actual test
        LocalExtension localExtension = this.extensionManager.installExtension(this.rubyArtifactId);

        Assert.assertNotNull(localExtension);
        Assert.assertNotNull(localExtension.getFile());
        Assert.assertTrue(localExtension.getFile().exists());

        Macro rubyMacro = getComponentManager().lookup(Macro.class, "ruby");

        Assert.assertNotNull(rubyMacro);
    }
}
