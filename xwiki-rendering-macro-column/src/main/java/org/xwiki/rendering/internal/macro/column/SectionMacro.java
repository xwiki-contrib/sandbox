package org.xwiki.rendering.internal.macro.column;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.column.SectionMacroParameters;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

@Component(SectionMacro.MACRO_NAME)
public class SectionMacro extends AbstractMacro<SectionMacroParameters>
{
    private static final double TOTAL_WIDTH = 99.9;

    private static final double COLUMN_RIGHT_PADDING_RATE = 1.5;

    private static final String STYLE_TEXT_ALIGN_JUSTIFY = "text-align:justify;";

    private static final String STYLE_CLEAR_BOTH = "clear:both";

    private static final String PARAMETER_STYLE = "style";

    private static final String COLUMN_RIGHT_PADDING_STYLE = COLUMN_RIGHT_PADDING_RATE + "%";

    private static final String DESCRIPTION = "A macro to enclose columned text";

    public static final String MACRO_NAME = "section";

    @Requirement
    private ComponentManager componentManager;

    public SectionMacro()
    {
        super("Section", DESCRIPTION, SectionMacroParameters.class);
    }

    /**
     * {@inheritDoc}
     */
    public List<Block> execute(SectionMacroParameters parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
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

        List<MacroBlock> potentialColumns = parsedDom.getChildrenByType(MacroBlock.class, false);

        int count = this.countColumns(potentialColumns);

        if (count == 0) {
            throw new MacroExecutionException("Section macro expect at least one column macro as first-level children");
        }

        double computedColumnWidth = ((TOTAL_WIDTH - COLUMN_RIGHT_PADDING_RATE * (count - 1)) / count);

        // Make the actual columns injecting divs around column macros
        this.makeColumns(potentialColumns, computedColumnWidth);

        // finally, clear the floats introduced by the columns
        Map<String, String> clearFloatsParams = new HashMap<String, String>();
        clearFloatsParams.put(PARAMETER_STYLE, STYLE_CLEAR_BOTH);

        parsedDom.addChild(new GroupBlock(clearFloatsParams));

        Map<String, String> sectionParameters = new HashMap<String, String>();
        if (parameters.isJustify()) {
            sectionParameters.put(PARAMETER_STYLE, STYLE_TEXT_ALIGN_JUSTIFY);
        }
        
        Block sectionRoot = new GroupBlock(sectionParameters);
        sectionRoot.addChildren(parsedDom.getChildren());

        return Collections.singletonList(sectionRoot);
    }

    /**
     * Count the number of columns in the given list of blocks.
     */
    private int countColumns(List<MacroBlock> blocks)
    {
        int result = 0;
        for (MacroBlock maybeColumn : blocks) {
            if (maybeColumn.getId().equals(ColumnMacro.MACRO_NAME)) {
                result++;
            }
        }
        return result;
    }

    /**
     * Transform the passed blocks to make them columns, that is insert them in an enclosing "div" (or group)
     * with the appropriate styles.
     * 
     * @param blocks the blocks to transform. Presumably all column macro blocks.
     * @param columnWidth the relative width to give to each column.
     */
    private void makeColumns(List<MacroBlock> blocks, double columnWidth)
    {
        Iterator<MacroBlock> it = blocks.iterator();
        while (it.hasNext()) {
            MacroBlock probablyColumn = it.next();
            if (probablyColumn.getId().equals(ColumnMacro.MACRO_NAME)) {
                ColumnStyle style = new ColumnStyle();
                style.setWidth(columnWidth + "%");
                if (it.hasNext()) {
                    style.setPaddingRight(COLUMN_RIGHT_PADDING_STYLE);
                }
                Map<String, String> params = Collections.singletonMap(PARAMETER_STYLE, style.getStyleAsString());
                Block colParent = new GroupBlock(new HashMap<String, String>(params));
                colParent.addChild(probablyColumn.clone());
                probablyColumn.getParent().replaceChild(colParent, probablyColumn);
            }
            // FIXME Should we cry and throw an exception if ever we meet something else than a column macro right under
            // a section macro ?
        }

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
