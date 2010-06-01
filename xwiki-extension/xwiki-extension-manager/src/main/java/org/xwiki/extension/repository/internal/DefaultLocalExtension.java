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
package org.xwiki.extension.repository.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionType;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.ExtensionRepository;

public class DefaultLocalExtension implements LocalExtension
{
    private File file;

    private boolean isDependency;

    private String name;

    private String version;

    private ExtensionType type;

    private String description;

    private String author;

    private String website;

    private List<ExtensionId> dependencies = new ArrayList<ExtensionId>();

    private DefaultLocalExtensionRepository repository;

    public DefaultLocalExtension(DefaultLocalExtensionRepository repository, String name, String version,
        ExtensionType type)
    {
        this.repository = repository;

        this.name = name;
        this.version = version;
        this.type = type;

        this.file = new File(repository.getRootFolder(), name + "-" + version + "." + type.getFileExtension());
    }

    public DefaultLocalExtension(DefaultLocalExtensionRepository repository, Extension extension)
    {
        this(repository, extension.getName(), extension.getVersion(), extension.getType());

        // TODO
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public void setDependency(boolean isDependency)
    {
        this.isDependency = isDependency;
    }

    // Extension

    public void download(File file)
    {
        // TODO: copy #getFile() into provided File
    }

    public String getName()
    {
        return this.name;
    }

    public String getVersion()
    {
        return this.version;
    }

    public ExtensionType getType()
    {
        return this.type;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public String getWebSite()
    {
        return this.website;
    }

    public List<ExtensionId> getDependencies()
    {
        return Collections.unmodifiableList(this.dependencies);
    }

    public ExtensionRepository getRepository()
    {
        return this.repository;
    }

    // LocalExtension

    public File getFile()
    {
        return file;
    }

    public boolean isDependency()
    {
        return isDependency;
    }
}
