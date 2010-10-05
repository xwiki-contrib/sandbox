package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.FormatConverter;

@Component("format")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class FormatBlockParser extends DefaultBlockParser
{
    private static final FormatConverter FORMATCONVERTER = new FormatConverter();

    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("format");
        }
    };

    public FormatBlockParser(Listener listener)
    {
        super(listener, NAMES);
    }

    @Override
    protected void beginBlock() throws SAXException
    {
        getListener()
            .beginFormat(FORMATCONVERTER.toFormat(getParameterAsString("format", null)), getCustomParameters());
    }

    @Override
    protected void endBlock() throws SAXException
    {
        getListener().endFormat(FORMATCONVERTER.toFormat(getParameterAsString("format", null)), getCustomParameters());
    }
}
