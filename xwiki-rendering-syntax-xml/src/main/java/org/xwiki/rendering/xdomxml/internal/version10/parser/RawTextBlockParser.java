package org.xwiki.rendering.xdomxml.internal.version10.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.xdomxml.internal.parser.DefaultBlockParser;

@Component("raw_text")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class RawTextBlockParser extends DefaultBlockParser
{
    private static final Set<String> NAMES = new HashSet<String>()
    {
        {
            add("content");
            add("syntax");
        }
    };

    @Requirement
    private SyntaxFactory syntaxFactory;

    public RawTextBlockParser(Listener listener)
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
        try {
            getListener().onRawText(getParameterAsString("content", ""),
                this.syntaxFactory.createSyntaxFromIdString(getParameterAsString("syntax", null)));
        } catch (ParseException e) {
            throw new SAXException("Failed to parse [syntax] parameter in rw block", e);
        }
    }
}
