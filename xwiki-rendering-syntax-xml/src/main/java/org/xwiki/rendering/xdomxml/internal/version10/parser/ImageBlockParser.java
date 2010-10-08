package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;
import org.xwiki.rendering.xdomxml.internal.version10.parser.parameter.ResourceReferenceParser;

@Component("image")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ImageBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("freestanding");
        }
    };

    private ResourceReferenceParser referenceParser = new ResourceReferenceParser();

    public ImageBlockParser(Listener listener)
    {
        super(listener, NAMES);
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equals("reference")) {
            setCurrentHandler(this.referenceParser);
        } else {
            super.startElementInternal(uri, localName, qName, attributes);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser#beginBlock()
     */
    @Override
    protected void beginBlock() throws SAXException
    {
        getListener().onImage(this.referenceParser.getResourceReference(),
            getParameterAsBoolean("freestanding", false), getCustomParameters());
    }
}
