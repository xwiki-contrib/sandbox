package org.xwiki.rendering.xdomxml.internal.version10.parser.parameter;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

public class ResourceReferenceParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("type");
            add("reference");
            add("typed");
        }
    };

    public ResourceReference reference;

    public ResourceReferenceParser()
    {
        super(NAMES);
    }

    public ResourceReference getResourceReference()
    {
        return this.reference;
    }

    @Override
    protected void endBlock() throws SAXException
    {
        this.reference =
            new ResourceReference(getParameterAsString("reference", null), new ResourceType(getParameterAsString(
                "type", "path")));
        this.reference.setTyped(getParameterAsBoolean("typed", true));
        this.reference.setParameters(getCustomParameters());
    }
}
