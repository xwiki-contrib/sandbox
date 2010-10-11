package org.xwiki.rendering.xdomxml.internal.parser.parameters;

import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CustomParametersParser extends DefaultHandler
{
    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    private StringBuffer value = new StringBuffer();

    private int level = 0;

    private String currentEntry;

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    // ContentHandler

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        this.value.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.level > 0) {
            this.currentEntry = qName;
            String name = attributes.getValue("name");
            if (name != null) {
                this.currentEntry = name;
            }
        }

        ++this.level;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        --this.level;

        if (this.level > 0) {
            this.parameters.put(this.currentEntry, this.value.toString());
            this.value.setLength(0);
        }        
    }
}
