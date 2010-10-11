package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.HeaderLevelConverter;

@Component("header")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class HeaderBlockParser extends DefaultBlockParser
{
    private static final HeaderLevelConverter HEADERLEVELCONVERTER = new HeaderLevelConverter();

    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("level");
            add("id");
        }
    };

    public HeaderBlockParser()
    {
        super(NAMES);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser#beginBlock()
     */
    @Override
    protected void beginBlock() throws SAXException
    {
        getListener().beginHeader(HEADERLEVELCONVERTER.toFormat(getParameterAsString("level", null)),
            getParameterAsString("id", null), getCustomParameters());
    }

    @Override
    protected void endBlock() throws SAXException
    {
        getListener().endHeader(HEADERLEVELCONVERTER.toFormat(getParameterAsString("level", null)),
            getParameterAsString("id", null), getCustomParameters());
    }
}
