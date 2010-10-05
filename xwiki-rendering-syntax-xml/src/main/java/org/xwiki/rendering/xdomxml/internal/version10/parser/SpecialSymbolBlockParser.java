package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.Collections;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

@Component("special_symbol")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class SpecialSymbolBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = Collections.singleton("symbol");

    public SpecialSymbolBlockParser(Listener listener)
    {
        super(listener, NAMES);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser#beginBlock()
     */
    @Override
    protected void beginBlock() throws SAXException
    {
        getListener().onSpecialSymbol(getParameterAsChar("symbol", (char) 0));
    }
}
