package org.xwiki.rendering.xdomxml.internal.renderer.parameters;

import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public abstract class AbstractSerializer
{
    public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

    public void serializeParameter(String name, Map<String, String> map, ContentHandler contentHandler)
    {
        startElement(name, EMPTY_ATTRIBUTES, contentHandler);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            serializeParameter(entry.getKey(), entry.getValue(), contentHandler);
        }
        endElement(name, contentHandler);
    }

    public void serializeParameter(String name, char value, ContentHandler contentHandler)
    {
        serializeParameter(name, String.valueOf(value), contentHandler);
    }
    
    public void serializeParameter(String name, int value, ContentHandler contentHandler)
    {
        serializeParameter(name, String.valueOf(value), contentHandler);
    }
    
    public void serializeParameter(String name, boolean value, ContentHandler contentHandler)
    {
        serializeParameter(name, String.valueOf(value), contentHandler);
    }

    public void serializeParameter(String name, String value, ContentHandler contentHandler)
    {
        String nodeName;
        Attributes attributes;

        if (isValidNodeName(name)) {
            nodeName = name;
            attributes = EMPTY_ATTRIBUTES;
        } else {
            nodeName = name;
            AttributesImpl attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute(null, null, "name", null, name);
            attributes = attributesImpl;
        }

        startElement(nodeName, attributes, contentHandler);
        characters(value, contentHandler);
        endElement(nodeName, contentHandler);
    }

    public boolean isValidNodeName(String name)
    {
        // TODO if invalid node name or "entry" return false
        return true;
    }

    public void startElement(String elementName, Attributes attributes, ContentHandler contentHandler)
    {
        try {
            contentHandler.startElement("", elementName, elementName, attributes);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }

    public void characters(String str, ContentHandler contentHandler)
    {
        try {
            contentHandler.characters(str.toCharArray(), 0, str.length());
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }

    public void endElement(String elementName, ContentHandler contentHandler)
    {
        try {
            contentHandler.endElement("", elementName, elementName);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to send sax event", e);
        }
    }
}
