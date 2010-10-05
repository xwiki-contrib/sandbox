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
import org.xwiki.rendering.xdomxml.internal.version10.parser.parameter.LinkParser;

@Component("link")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class LinkBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("freestanding");
        }
    };

    private LinkParser linkParser = new LinkParser();

    public LinkBlockParser(Listener listener)
    {
        super(listener, NAMES);
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equals("link")) {
            setCurrentHandler(this.linkParser);
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
        getListener().beginLink(this.linkParser.getLink(), getParameterAsBoolean("freestanding", false),
            getCustomParameters());
    }

    @Override
    protected void endBlock() throws SAXException
    {
        getListener().endLink(this.linkParser.getLink(), getParameterAsBoolean("freestanding", false),
            getCustomParameters());
    }
}
