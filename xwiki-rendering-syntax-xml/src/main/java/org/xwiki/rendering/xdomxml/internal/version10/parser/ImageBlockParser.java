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
import org.xwiki.rendering.xdomxml.internal.version10.parser.parameter.ImageParser;

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

    private ImageParser imageParser = new ImageParser();

    public ImageBlockParser(Listener listener)
    {
        super(listener, NAMES);
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equals("image")) {
            setCurrentHandler(this.imageParser);
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
        getListener().onImage(this.imageParser.getImage(), getParameterAsBoolean("freestanding", false),
            getCustomParameters());
    }
}
