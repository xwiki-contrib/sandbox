package org.xwiki.extension.repository;

public class ArtifactId
{
    private String name;

    private String version;

    public ArtifactId(String name, String version)
    {
        this.name = name;
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }
}
