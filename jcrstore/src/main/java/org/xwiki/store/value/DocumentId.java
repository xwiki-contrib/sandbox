package org.xwiki.store.value;

/**
 * immutable
 */
public class DocumentId extends AbstractId
{
    private String wiki;
    private String space;
    private String name;
    private String language;

    public DocumentId(String wiki, String space, String name, String language)
    {
        this.wiki = wiki;
        this.space = space;
        this.name = name;
        this.language = language;
    }

    public String getWiki()
    {
        return wiki;
    }

    public String getSpace()
    {
        return space;
    }

    public String getName()
    {
        return name;
    }

    public String getLanguage()
    {
        return language;
    }
}
