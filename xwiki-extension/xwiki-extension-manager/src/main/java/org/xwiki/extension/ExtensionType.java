package org.xwiki.extension;

public enum ExtensionType
{
    JAR("jar"),
    PAGES("xar");

    private String fileExtension;

    private ExtensionType(String fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension()
    {
        return this.fileExtension;
    }
}
