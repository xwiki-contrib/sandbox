package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

@Component("macro_marker")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class MacroMarkerBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("id");
            add("content");
            add("inline");
        }
    };

    public MacroMarkerBlockParser(Listener listener)
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
        getListener().beginMacroMarker(getParameterAsString("id", "macro"), getCustomParameters(),
            getParameterAsString("content", null), getParameterAsBoolean("inline", false));
    }

    @Override
    protected void endBlock() throws SAXException
    {
        getListener().endMacroMarker(getParameterAsString("id", "macro"), getCustomParameters(),
            getParameterAsString("content", null), getParameterAsBoolean("inline", false));
    }
}
