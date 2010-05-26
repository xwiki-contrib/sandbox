package org.xwiki.extension;

public enum ExtensionType
{
    /**
     * A "virtual" extension representing a top level application with a set of dependencies.
     */
    EMPTY(null),

    /**
     * A jar file.
     */
    JAR("jar"),

    /**
     * A package containing a set of wiki pages.
     */
    PAGES("xar"),

    SKIN("zip");

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
