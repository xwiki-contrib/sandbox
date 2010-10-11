package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.Collections;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

@Component("id")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class IdBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = Collections.singleton("id");

    public IdBlockParser()
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
        getListener().onId(getParameterAsString("id", "id"));
    }
}
