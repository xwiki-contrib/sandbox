package org.xwiki.extension.repository.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionType;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;

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

    private LocalExtensionRepository repository;

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
