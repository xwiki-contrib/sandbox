package org.xwiki.rendering.xdomxml.internal.parser;

import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.rendering.listener.Listener;

@ComponentRole
public interface BlockParser extends ContentHandler
{
    Listener getListener();

    void setListener(Listener listener);

    String getVersion();

    void setVersion(String version);
}
