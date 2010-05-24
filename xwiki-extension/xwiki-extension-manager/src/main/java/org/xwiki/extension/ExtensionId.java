package org.xwiki.extension;

public class ExtensionId
{
    private String name;

    private String version;

    public ExtensionId(String name, String version)
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
