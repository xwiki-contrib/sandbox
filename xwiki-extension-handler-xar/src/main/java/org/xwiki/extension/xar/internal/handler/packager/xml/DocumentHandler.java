package org.xwiki.extension.xar.internal.handler.packager.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.manager.ComponentManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class DocumentHandler extends AbstractHandler
{
    private boolean fromDatabase = false;

    private boolean needSave = true;

    public DocumentHandler(ComponentManager componentManager)
    {
        super(componentManager, new XWikiDocument());
    }

    public XWikiDocument getDocument()
    {
        return (XWikiDocument) getCurrentBean();
    }

    private void saveDocument(String comment) throws SAXException
    {
        try {
            XWikiContext context = getXWikiContext();
            XWikiDocument document = getDocument();

            if (!this.fromDatabase) {
                XWikiDocument existingDocument =
                    getXWikiContext().getWiki().getDocument(document.getDocumentReference(), context);
                existingDocument = existingDocument.getTranslatedDocument(document.getLanguage(), context);

                if (!existingDocument.isNew()) {
                    document.setVersion(existingDocument.getVersion());
                }

                this.fromDatabase = true;
            }

            getXWikiContext().getWiki().saveDocument(document, comment, context);

            setCurrentBean(getXWikiContext().getWiki().getDocument(document.getDocumentReference(), context));
        } catch (Exception e) {
            throw new SAXException("Failed to save document", e);
        }

        this.needSave = false;
    }

    @Override
    protected void currentBeanModified()
    {
        this.needSave = true;
    }

    @Override
    public void startElementInternal(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
    {
        if (qName.equals("attachment")) {
            setCurrentHandler(new AttachmentHandler(getComponentManager()));
        } else if (qName.equals("object")) {
            setCurrentHandler(new ObjectHandler(getComponentManager()));
        } else if (qName.equals("class")) {
            setCurrentHandler(new ClassHandler(getComponentManager(), getDocument().getXClass()));
        } else {
            super.startElementInternal(uri, localName, qName, attributes);
        }
    }

    @Override
    public void endElementInternal(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals("attachment")) {
            if (!getDocument().getAttachmentList().isEmpty()) {
                saveDocument("Import: save first attachment");
            }

            AttachmentHandler handler = (AttachmentHandler) getCurrentHandler();

            getDocument().getAttachmentList().add(handler.getAttachment());
            
            // TODO: add attachment to document
            saveDocument("Import: add attachment");
        } else if (qName.equals("object")) {
            ObjectHandler handler = (ObjectHandler) getCurrentHandler();
            getDocument().addXObject(handler.getObject());

            this.needSave = true;
        } else if (qName.equals("class")) {
            this.needSave = true;
        } else {
            super.endElementInternal(uri, localName, qName);
        }
    }

    @Override
    protected void endHandlerElement(String uri, String localName, String qName) throws SAXException
    {
        if (this.needSave) {
            saveDocument(this.fromDatabase ? "Import: final save" : "Import");
        }
    }
}
