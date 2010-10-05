package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import org.xml.sax.ContentHandler;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml.internal.renderer.parameters.AbstractSerializer;

public class LinkSerializer extends AbstractSerializer
{
    public void serialize(Link link, ContentHandler contentHandler)
    {
        startElement("link", EMPTY_ATTRIBUTES, contentHandler);

        serializeParameter("type", link.getType().getScheme(), contentHandler);
        serializeParameter("reference", link.getReference(), contentHandler);
        if (!link.isTyped()) {
            serializeParameter("typed", link.isTyped(), contentHandler);
        }
        serializeParameter(XDOMXMLConstants.ELEM_PARAMETERS, link.getParameters(), contentHandler);

        endElement("link", contentHandler);
    }
}
