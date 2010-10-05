package org.xwiki.rendering.xdomxml.internal.version10.parser.parameter;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

public class ImageParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("type");
            add("reference");
            add("typed");
        }
    };

    public Image image = new Image();

    public ImageParser()
    {
        super(null);
    }

    public Image getImage()
    {
        return this.image;
    }
    
    @Override
    protected void endBlock() throws SAXException
    {
        this.image.setType(new LinkType(getParameterAsString("type", "path")));
        this.image.setReference(getParameterAsString("reference", null));
        this.image.setTyped(getParameterAsBoolean("typed", true));
        this.image.setParameters(getCustomParameters());
    }
}
