package org.xwiki.rendering.xdomxml.internal.parser.parameters;

import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface BlockParser extends ContentHandler
{
    String getVersion();

    void setVersion(String version);
}
