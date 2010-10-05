package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import org.xml.sax.ContentHandler;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.xdomxml.internal.renderer.parameters.AbstractSerializer;

public class ImageSerializer extends AbstractSerializer
{
    public void serialize(Image image, ContentHandler contentHandler)
    {
        startElement("image", EMPTY_ATTRIBUTES, contentHandler);

        // TODO

        endElement("image", contentHandler);
    }
}
