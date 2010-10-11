package org.xwiki.rendering.xdomxml.internal.version10.renderer.parameter;

import org.xml.sax.ContentHandler;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;
import org.xwiki.rendering.xdomxml.internal.renderer.parameters.AbstractSerializer;

public class ResourceReferenceSerializer extends AbstractSerializer
{
    public void serialize(ResourceReference reference, ContentHandler contentHandler)
    {
        startElement("reference", EMPTY_ATTRIBUTES, contentHandler);

        serializeParameter("type", reference.getType().getScheme(), contentHandler);
        serializeParameter("reference", reference.getReference(), contentHandler);
        if (!reference.isTyped()) {
            serializeParameter("typed", reference.isTyped(), contentHandler);
        }
        if (reference.getParameters() != null && !reference.getParameters().isEmpty()) {
          serializeParameter(XDOMXMLConstants.ELEM_PARAMETERS, reference.getParameters(), contentHandler);
        }

        endElement("reference", contentHandler);
    }
}
