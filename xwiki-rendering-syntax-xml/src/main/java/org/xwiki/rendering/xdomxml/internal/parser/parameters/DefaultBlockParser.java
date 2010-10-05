package org.xwiki.rendering.xdomxml.internal.parser.parameters;

import java.util.Collections;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.Listener;
import org.xwiki.rendering.listener.chaining.EventType;
import org.xwiki.rendering.xdomxml.internal.XDOMXMLConstants;

@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultBlockParser extends AbstractBlockParser
{
    private Map<String, String> customParameters = Collections.emptyMap();

    public DefaultBlockParser(Listener listener)
    {
        super(listener);
    }

    public Map<String, String> getCustomParameters()
    {
        return this.customParameters;
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equals(XDOMXMLConstants.ELEM_PARAMETERS)) {
            // Start parsing custom parameters
            setCurrentHandler(new CustomParametersParser());
        }
    }

    @Override
    protected void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals(XDOMXMLConstants.ELEM_PARAMETERS)) {
            // Custom parameters has been parsed
            CustomParametersParser parametersParser = (CustomParametersParser) getCurrentHandler();
            this.customParameters = parametersParser.getParameters();
        }
    }

    @Override
    protected void beginBlock() throws SAXException
    {
        String name = getBlockName().toUpperCase();

        try {
            EventType onEventType = EventType.valueOf("ON_" + name);

            onEventType.fireEvent(getListener(), new Object[] {this.customParameters});
        } catch (IllegalArgumentException e) {
            // It's a container

            try {
                EventType beginEventType = EventType.valueOf("BEGIN_" + name);

                beginEventType.fireEvent(getListener(), new Object[] {this.customParameters});
            } catch (IllegalArgumentException e2) {
                throw new SAXException("Unknow block [" + name + "]", e2);
            }
        }
    }

    @Override
    protected void endBlock() throws SAXException
    {
        String name = getBlockName().toUpperCase();

        try {
            EventType onEventType = EventType.valueOf("ON_" + name);

            onEventType.fireEvent(getListener(), new Object[] {this.customParameters});
        } catch (IllegalArgumentException e) {
            // It's a container

            try {
                EventType endEventType = EventType.valueOf("END_" + name);

                endEventType.fireEvent(getListener(), new Object[] {this.customParameters});
            } catch (IllegalArgumentException e2) {
                throw new SAXException("Unknow block [" + name + "]", e2);
            }
        }
    }
}
