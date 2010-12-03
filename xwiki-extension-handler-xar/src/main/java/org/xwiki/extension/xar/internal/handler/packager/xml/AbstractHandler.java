package org.xwiki.extension.xar.internal.handler.packager.xml;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWikiContext;

public class AbstractHandler extends DefaultHandler
{
    @Requirement
    private ComponentManager componentManager;

    private Object currentBean;

    private ContentHandler currentHandler;

    private int currentHandlerLevel;

    private int depth = 0;
    
    protected StringBuffer value;

    public AbstractHandler(ComponentManager componentManager, Object currentBean)
    {
        this.componentManager = componentManager;
        this.currentBean = currentBean;
    }

    protected ComponentManager getComponentManager()
    {
        return componentManager;
    }

    protected Object getCurrentBean()
    {
        return currentBean;
    }
    
    protected void setCurrentBean(Object currentBean)
    {
        this.currentBean = currentBean;
    }

    // ContentHandler

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (this.currentHandler == null) {
            if (this.depth == 0) {
                startHandlerElement(uri, localName, qName, attributes);
            } else {
                startElementInternal(uri, localName, qName, attributes);
            }
        }

        if (this.currentHandler != null) {
            this.currentHandler.startElement(uri, localName, qName, attributes);
        }

        ++this.depth;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (this.currentHandler != null) {
            this.currentHandler.characters(ch, start, length);
        } else {
            charactersInternal(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        --this.depth;

        if (this.currentHandler != null) {
            this.currentHandler.endElement(uri, localName, qName);

            if (this.depth == this.currentHandlerLevel) {
                endElementInternal(uri, localName, qName);
                this.currentHandler = null;
            }
        } else {
            if (this.depth == 0) {
                endHandlerElement(uri, localName, qName);
            } else {
                endElementInternal(uri, localName, qName);
            }
        }
    }

    // to override

    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (this.currentBean != null) {
            if (value == null) {
                this.value = new StringBuffer();
            } else {
                this.value.setLength(0);
            }
        }
    }

    protected void charactersInternal(char[] ch, int start, int length) throws SAXException
    {
        if (this.currentBean != null) {
            this.value.append(ch, start, length);
        }
    }

    protected void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        if (this.currentBean != null) {
            Method setter;
            try {
                setter = this.currentBean.getClass().getMethod("set" + StringUtils.capitalize(qName), String.class);
                setter.invoke(this.currentBean, this.value);
                currentBeanModified();
            } catch (Exception e) {
                // TODO: LOG warn "Unknown element [" + qName + "]"
            }
        }
    }

    protected void currentBeanModified()
    {
        // no op
    }

    protected void startHandlerElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        // no op
    }

    protected void endHandlerElement(String uri, String localName, String qName) throws SAXException
    {
        // no op
    }

    // tools

    protected void setCurrentHandler(ContentHandler currentHandler)
    {
        this.currentHandler = currentHandler;
        this.currentHandlerLevel = this.depth;
    }

    public ContentHandler getCurrentHandler()
    {
        return this.currentHandler;
    }

    protected ExecutionContext getExecutionContext() throws ComponentLookupException
    {
        return this.componentManager.lookup(Execution.class).getContext();
    }

    protected XWikiContext getXWikiContext() throws ComponentLookupException
    {
        return (XWikiContext) getExecutionContext().getProperty("xwikicontext");
    }
}
