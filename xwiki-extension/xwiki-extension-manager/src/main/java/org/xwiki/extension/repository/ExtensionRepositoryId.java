package org.xwiki.extension.repository;

import java.net.URI;

public class ExtensionRepositoryId
{
    private String id;

    private String type;

    private URI uri;

    public ExtensionRepositoryId(String id, String type, URI uri)
    {
        this.id = id;
        this.type = type;
        this.uri = uri;
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public URI getURI()
    {
        return uri;
    }
}
