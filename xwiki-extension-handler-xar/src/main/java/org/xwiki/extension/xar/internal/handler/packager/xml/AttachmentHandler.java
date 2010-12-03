package org.xwiki.extension.xar.internal.handler.packager.xml;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.tika.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.manager.ComponentManager;

import com.xpn.xwiki.doc.XWikiAttachment;

public class AttachmentHandler extends AbstractHandler
{
    public AttachmentHandler(ComponentManager componentManager)
    {
        super(componentManager, new XWikiAttachment());
    }

    public XWikiAttachment getAttachment()
    {
        return (XWikiAttachment) getCurrentBean();
    }

    @Override
    protected void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equals("content")) {

        } else if (qName.equals("versions")) {
            this.value = null;
        } else {
            super.startElementInternal(uri, localName, qName, attributes);
        }
    }

    @Override
    protected void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals("content")) {
            try {
                Base64InputStream b64is = new Base64InputStream(IOUtils.toInputStream(this.value));
                getAttachment().setContent(b64is);
            } catch (IOException e) {
                // TODO: log error
            }
        } else {
            super.endElementInternal(uri, localName, qName);
        }
    }
}
