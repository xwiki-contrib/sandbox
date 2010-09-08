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
package org.xwiki.extension.internal;

import java.io.File;
import java.io.IOException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.extension.ExtensionManagerConfiguration;

@Component
public class DefaultExtensionManagerConfiguration implements ExtensionManagerConfiguration
{
    @Requirement("xwikiproperties")
    private ConfigurationSource configurationSource;

    // Cache

    private File localRepository;

    public File getLocalRepository() throws IOException
    {
        if (this.localRepository == null) {
            String localRepositoryPath = this.configurationSource.getProperty("extension.localRepository");

            if (localRepositoryPath == null) {
                this.localRepository =
                    new File(File.createTempFile("extension", ".xml").getParentFile(), "repository/");
            } else {
                this.localRepository = new File(localRepositoryPath);
            }
        }

        return this.localRepository;
    }
}
