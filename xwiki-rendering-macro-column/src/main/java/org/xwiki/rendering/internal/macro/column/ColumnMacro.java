package org.xwiki.rendering.internal.macro.column;

import java.io.StringReader;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.column.ColumnMacroParameters;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

@Component(ColumnMacro.MACRO_NAME)
public class ColumnMacro<P extends ColumnMacroParameters> extends AbstractMacro<P>
{

    @Requirement
    private ComponentManager componentManager;

    /** The name of this macro. */
    public static final String MACRO_NAME = "column";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "the content to put in the column";

    /** The description of the macro. */
    private static final String DESCRIPTION = "declares a column in a columned section";

    public ColumnMacro()
    {
        super("Column", DESCRIPTION, ColumnMacroParameters.class);
    }

    /**
     * {@inheritDoc}
     */
    public List<Block> execute(P parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {

        List<Block> result;
        XDOM parsedDom;

        // get a parser for the desired syntax identifier
        Parser parser = getSyntaxParser(context.getSyntax().toIdString());

        try {
            // parse the content of the wiki macro that has been injected by the
            // component manager
            // the content of the macro call itself is ignored.
            parsedDom = parser.parse(new StringReader(content));
        } catch (ParseException e) {
            throw new MacroExecutionException("Failed to parse content [" + content + "] with Syntax parser ["
                + parser.getSyntax() + "]", e);
        }

        result = parsedDom.getChildren();

        return result;
    }

    /**
     * Get the parser of the desired wiki syntax.
     * 
     * @param context the context of the macro transformation.
     * @return the parser of the current wiki syntax.
     * @throws MacroExecutionException Failed to find source parser.
     */
    protected Parser getSyntaxParser(String syntaxId) throws MacroExecutionException
    {
        try {
            return (Parser) this.componentManager.lookup(Parser.class, syntaxId);
        } catch (ComponentLookupException e) {
            throw new MacroExecutionException("Failed to find source parser", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsInlineMode()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.rendering.macro.Macro#getPriority()
     */
    @Override
    public int getPriority()
    {
        return 750;
    }

}
