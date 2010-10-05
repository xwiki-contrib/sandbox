package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.rendering.listener.HeaderLevel;

public class HeaderLevelConverter
{
    Map<String, HeaderLevel> STRINGTOHEADERLEVEL = new HashMap<String, HeaderLevel>()
    {
        {
            put("1", HeaderLevel.LEVEL1);
            put("2", HeaderLevel.LEVEL2);
            put("3", HeaderLevel.LEVEL3);
            put("4", HeaderLevel.LEVEL4);
            put("5", HeaderLevel.LEVEL5);
            put("6", HeaderLevel.LEVEL6);
        }
    };

    Map<HeaderLevel, String> HEADERLEVELTOSTRING = new HashMap<HeaderLevel, String>()
    {
        {
            put(HeaderLevel.LEVEL1, "1");
            put(HeaderLevel.LEVEL2, "2");
            put(HeaderLevel.LEVEL3, "3");
            put(HeaderLevel.LEVEL4, "4");
            put(HeaderLevel.LEVEL5, "5");
            put(HeaderLevel.LEVEL6, "6");
        }
    };

    public HeaderLevel toFormat(String str)
    {
        return STRINGTOHEADERLEVEL.containsKey(str) ? STRINGTOHEADERLEVEL.get(str) : HeaderLevel.LEVEL1;
    }

    public String toString(HeaderLevel headerLevel)
    {
        return HEADERLEVELTOSTRING.containsKey(headerLevel) ? HEADERLEVELTOSTRING.get(headerLevel)
            : HEADERLEVELTOSTRING.get(HeaderLevel.LEVEL1);
    }
}
