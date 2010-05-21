package org.xwiki.extension.repository;

import java.net.URL;

public class RepositoryId
{
    private String id;

    private String type;

    private URL url;

    public RepositoryId(String id, String type, URL url)
    {
        this.id = id;
        this.type = type;
        this.url = url;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public URL getUrl()
    {
        return url;
    }
}
