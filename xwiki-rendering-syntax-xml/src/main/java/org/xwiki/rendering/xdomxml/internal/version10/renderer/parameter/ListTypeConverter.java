package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.rendering.listener.ListType;

public class ListTypeConverter
{
    Map<String, ListType> STRINGTOLISTTYPE = new HashMap<String, ListType>()
    {
        {
            put("bulleted", ListType.BULLETED);
            put("numbered", ListType.NUMBERED);
        }
    };

    Map<ListType, String> LISTTYPETOSTRING = new HashMap<ListType, String>()
    {
        {
            put(ListType.BULLETED, "bulleted");
            put(ListType.NUMBERED, "numbered");
        }
    };

    public ListType toFormat(String str)
    {
        return STRINGTOLISTTYPE.containsKey(str) ? STRINGTOLISTTYPE.get(str) : ListType.BULLETED;
    }

    public String toString(ListType listType)
    {
        return LISTTYPETOSTRING.containsKey(listType) ? LISTTYPETOSTRING.get(listType) : LISTTYPETOSTRING
            .get(ListType.BULLETED);
    }
}
