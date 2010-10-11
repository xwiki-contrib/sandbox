package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;
import org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter.ListTypeConverter;

@Component("list")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ListBlockParser extends DefaultBlockParser
{
    private static final ListTypeConverter LISTTYPECONVERTER = new ListTypeConverter();

    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("type");
        }
    };

    public ListBlockParser()
    {
        super(NAMES);
    }

    @Override
    protected void beginBlock() throws SAXException
    {
        getListener()
            .beginList(LISTTYPECONVERTER.toFormat(getParameterAsString("type", null)), getCustomParameters());
    }

    @Override
    protected void endBlock() throws SAXException
    {
        getListener().endList(LISTTYPECONVERTER.toFormat(getParameterAsString("type", null)), getCustomParameters());
    }
}
