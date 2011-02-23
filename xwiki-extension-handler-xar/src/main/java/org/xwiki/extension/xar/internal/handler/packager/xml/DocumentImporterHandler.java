/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension.xar.internal.handler.packager.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class DocumentImporterHandler extends AbstractHandler
{
    private boolean fromDatabase = false;

    private boolean needSave = true;

    public DocumentImporterHandler(ComponentManager componentManager)
    {
        super(componentManager);

        try {
            setCurrentBean(new XWikiDocument(new DocumentReference(getXWikiContext().getDatabase(), "XWiki", "Page")));
        } catch (ComponentLookupException e) {
            setCurrentBean(new XWikiDocument());
        }
        // skip useless known elements
        this.skippedElements.add("version");
        this.skippedElements.add("minorEdit");
        this.skippedElements.add("comment");
    }

    public XWikiDocument getDocument()
    {
        return (XWikiDocument) getCurrentBean();
    }

    public void setWiki(String wiki)
    {
        getDocument().setDatabase(wiki);
    }

    private void saveDocument(String comment) throws SAXException
    {
        try {
            XWikiContext context = getXWikiContext();
            XWikiDocument document = getDocument();

            if (!this.fromDatabase) {
                XWikiDocument existingDocument =
                    context.getWiki().getDocument(document.getDocumentReference(), context);
                existingDocument = existingDocument.getTranslatedDocument(document.getLanguage(), context);

                if (!existingDocument.isNew()) {
                    document.setVersion(existingDocument.getVersion());
                }

                this.fromDatabase = true;
            }

            context.getWiki().saveDocument(document, comment, context);

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

            // TODO: add attachment to documentxwikidoc
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
