package org.xwiki.extension.xar;

public class LocalPage
{
    private String space;

    private String page;

    public LocalPage(String space, String page)
    {
        this.space = space;
        this.page = page;
    }

    public String getSpace()
    {
        return space;
    }

    public String getPage()
    {
        return page;
    }
}
