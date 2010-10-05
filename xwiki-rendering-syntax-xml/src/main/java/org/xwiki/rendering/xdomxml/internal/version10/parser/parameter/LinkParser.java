package org.xwiki.rendering.xdomxml.internal.version10.parser.parameter;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

public class LinkParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("type");
            add("reference");
            add("typed");
        }
    };

    public Link link = new Link();

    public LinkParser()
    {
        super(null);
    }

    public Link getLink()
    {
        return this.link;
    }
    
    @Override
    protected void endBlock() throws SAXException
    {
        this.link.setType(new LinkType(getParameterAsString("type", "path")));
        this.link.setReference(getParameterAsString("reference", null));
        this.link.setTyped(getParameterAsBoolean("typed", true));
        this.link.setParameters(getCustomParameters());
    }
}
