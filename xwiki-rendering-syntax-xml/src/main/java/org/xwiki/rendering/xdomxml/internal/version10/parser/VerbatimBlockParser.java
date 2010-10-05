package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

@Component("verbatim")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class VerbatimBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("count");
            add("inline");
        }
    };

    public VerbatimBlockParser(Listener listener)
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
        getListener().onVerbatim(getParameterAsString("content", ""), getParameterAsBoolean("inline", false),
            getCustomParameters());
    }
}
