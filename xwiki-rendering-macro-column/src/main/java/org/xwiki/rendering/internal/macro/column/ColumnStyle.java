package org.xwiki.rendering.internal.macro.column;

import org.apache.commons.lang.StringUtils;

/**
 * Abstraction that encapsulate the different styles that can be given to a column
 * by the section/column macros and can generate the proper style code to be given
 * to the div enclosing the targeted column.
 */
public class ColumnStyle
{
    private static final String FLOAT_RULE = "float:left;";

    private String width;

    private String paddingRight;

    public String getStyleAsString()
    {
        String style = FLOAT_RULE;
        if (!StringUtils.isBlank(this.width)) {
            style += "width:" + this.width + ";";
        }
        if (!StringUtils.isBlank(this.paddingRight)) {
            style += "padding-right:" + this.paddingRight + ";";
        }
        return style;
    }

    public String getWidth()
    {
        return width;
    }

    public void setWidth(String width)
    {
        this.width = width;
    }

    public String getPaddingRight()
    {
        return paddingRight;
    }

    public void setPaddingRight(String paddingRight)
    {
        this.paddingRight = paddingRight;
    }

}
