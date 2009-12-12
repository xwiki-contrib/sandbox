package org.xwiki.model;

import org.xwiki.bridge.DocumentName;
import org.xwiki.rendering.block.XDOM;

import java.util.List;
import java.util.Locale;

public interface Document extends Persistable
{
    /**
     * @return the list of object definitions defined inside this document
     */
    List<ObjectDefinition> getObjectDefinitions();

    List<Object> getObjects();

    List<Attachment> getAttachments();

    void setParent(Document document);

    Document getParent();

    // Note: In order to make modifications to the document's content, modify the returned XDOM
    // Default language
    XDOM getContent();
    XDOM getContent(Locale locale);

    // get/setSyntax(Syntax syntax)

    // Q: Should we have this or should we force users to use a Parser for a given syntax, ie make Document
    // independent of the Syntax?
    // Note: If we make them independent then we have a pb of converting existing docs in the DB.
    //String setContent(String content);

    boolean hasObject(String objectName);

    // Q: Is this ok?
    DocumentName getDocumentName();
}
