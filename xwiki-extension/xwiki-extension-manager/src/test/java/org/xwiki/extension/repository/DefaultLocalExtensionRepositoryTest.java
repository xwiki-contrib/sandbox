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
package org.xwiki.extension.repository;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.internal.DefaultLocalExtensionRepository;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultLocalExtensionRepositoryTest extends AbstractComponentTestCase
{
    private DefaultLocalExtensionRepository localExtensionRepository;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        getConfigurationSource().setProperty("extension.localRepository",
            "target/DefaultLocalExtensionRepositoryTest/test-repository");

        File testDirectory = new File("target/DefaultLocalExtensionRepositoryTest");
        if (testDirectory.exists()) {
            FileUtils.deleteDirectory(testDirectory);
        }

        FileUtils.touch(new File(
            "target/DefaultLocalExtensionRepositoryTest/test-repository/existingextension-version.type"));

        this.localExtensionRepository =
            (DefaultLocalExtensionRepository) getComponentManager().lookup(LocalExtensionRepository.class);
    }

    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        ConfigurableDefaultCoreExtensionRepository.register(getComponentManager());
    }

    @Test
    public void testInit()
    {
        Assert.assertTrue(this.localExtensionRepository.countExtensions() > 0);
    }

    @Test
    public void testGetLocalExtension()
    {
        Assert.assertNull(this.localExtensionRepository.getLocalExtension("unexistingextension"));

        Extension extension = this.localExtensionRepository.getLocalExtension("existingextension");

        Assert.assertNotNull(extension);
        Assert.assertEquals("existingextension", extension.getId());
        Assert.assertEquals("version", extension.getVersion());
        Assert.assertEquals("type", extension.getType());
    }

    @Test
    public void testResolve() throws ResolveException
    {
        try {
            this.localExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        try {
            this.localExtensionRepository.resolve(new ExtensionId("existingextension", "wrongversion"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension = this.localExtensionRepository.resolve(new ExtensionId("existingextension", "version"));

        Assert.assertNotNull(extension);
        Assert.assertEquals("existingextension", extension.getId());
        Assert.assertEquals("version", extension.getVersion());
    }

    @Test
    public void testInstallExtension() throws ResolveException
    {
        // TODO
    }

    @Test
    public void testUninstallExtension() throws ResolveException
    {
        // TODO
    }
}
