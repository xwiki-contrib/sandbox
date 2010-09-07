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
package org.xwiki.extension.install.internal.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xwiki.classloader.ExtendedURLClassLoader;
import org.xwiki.classloader.URIClassLoader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.install.ExtensionInstaller;
import org.xwiki.extension.install.ExtensionInstallerException;

@Component("jar")
public class JarExtensionInstaller extends AbstractLogEnabled implements ExtensionInstaller, Initializable
{
    private ExtendedURLClassLoader classLoader;

    private ComponentAnnotationLoader jarLoader;

    @Requirement
    private ComponentManager componentManager;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        this.jarLoader = new ComponentAnnotationLoader();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.install.ExtensionInstaller#install(org.xwiki.extension.LocalExtension)
     */
    public void install(LocalExtension localExtension) throws ExtensionInstallerException
    {
        // 1) load jar into classloader
        createOrExtendClassLoader(localExtension.getFile());

        // 2) load and register components
        loadComponents(localExtension.getFile());
    }

    private void createOrExtendClassLoader(File jarFile) throws ExtensionInstallerException
    {
        try {
            if (this.classLoader == null) {
                this.classLoader =
                    new URIClassLoader(new URI[] {new URI(jarFile.getAbsolutePath())}, getClass().getClassLoader());
            } else {
                this.classLoader.addURL(new URL(jarFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            throw new ExtensionInstallerException("Failed to load jar file", e);
        }
    }

    private void loadComponents(File jarFile) throws ExtensionInstallerException
    {
        try {
            List<String> componentClassNames = new ArrayList<String>();
            List<String> componentOverrideClassNames = new ArrayList<String>();

            getDeclaredComponents(jarFile, componentClassNames, componentOverrideClassNames);

            if (componentClassNames.isEmpty()) {
                getLogger().debug(jarFile + " does not contains any component");
                return;
            }

            this.jarLoader.initialize(this.componentManager, this.classLoader, componentClassNames,
                componentOverrideClassNames);
        } catch (Exception e) {
            throw new ExtensionInstallerException("Failed to load jar file components", e);
        }
    }

    private void getDeclaredComponents(File jarFile, List<String> componentClassNames,
        List<String> componentOverrideClassNames) throws IOException
    {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));

        try {
            for (ZipEntry entry = zis.getNextEntry(); entry != null && componentClassNames == null
                && componentOverrideClassNames == null; entry = zis.getNextEntry()) {
                if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_LIST)) {
                    componentClassNames = this.jarLoader.getDeclaredComponents(zis);
                } else if (entry.getName().equals(ComponentAnnotationLoader.COMPONENT_OVERRIDE_LIST)) {
                    componentOverrideClassNames = this.jarLoader.getDeclaredComponents(zis);
                }
            }
        } finally {
            zis.close();
        }
    }
}
