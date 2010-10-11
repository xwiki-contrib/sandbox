package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

@Component("macro")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MacroBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("id");
            add("content");
            add("inline");
        }
    };

    public MacroBlockParser()
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
        getListener().onMacro(getParameterAsString("id", "macro"), getCustomParameters(),
            getParameterAsString("content", null), getParameterAsBoolean("inline", false));
    }
}
