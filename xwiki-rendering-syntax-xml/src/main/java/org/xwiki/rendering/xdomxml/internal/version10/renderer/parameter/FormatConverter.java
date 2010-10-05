package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.rendering.listener.Format;

public class FormatConverter
{
    Map<String, Format> STRINGTOFORMAT = new HashMap<String, Format>()
    {
        {
            put("bold", Format.BOLD);
            put("italic", Format.ITALIC);
            put("underlined", Format.UNDERLINED);
            put("strikeout", Format.STRIKEDOUT);
            put("superscript", Format.SUPERSCRIPT);
            put("subscript", Format.SUBSCRIPT);
            put("monospace", Format.MONOSPACE);
            put("none", Format.NONE);
        }
    };

    Map<Format, String> FORMATTOSTRING = new HashMap<Format, String>()
    {
        {
            put(Format.BOLD, "bold");
            put(Format.ITALIC, "italic");
            put(Format.UNDERLINED, "underlined");
            put(Format.STRIKEDOUT, "strikeout");
            put(Format.SUPERSCRIPT, "superscript");
            put(Format.SUBSCRIPT, "subscript");
            put(Format.MONOSPACE, "monospace");
            put(Format.NONE, "none");
        }
    };

    public Format toFormat(String str)
    {
        return STRINGTOFORMAT.containsKey(str) ? STRINGTOFORMAT.get(str) : Format.NONE;
    }

    public String toString(Format format)
    {
        return FORMATTOSTRING.containsKey(format) ? FORMATTOSTRING.get(format) : FORMATTOSTRING.get(Format.NONE);
    }
}
