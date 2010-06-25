package org.xwiki.model;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;

import java.util.List;
import java.util.Locale;

public interface Document extends Entity
{
    /**
     * @return the list of object definitions defined inside this document
     */
    List<ObjectDefinition> getObjectDefinitions();

    ObjectDefinition getObjectDefinition(String objectDefinitionName);

    ObjectDefinition addObjectDefinition(String objectDefinitionName);

    void removeObjectDefinition(String objectDefinitionName);

    List<Object> getObjects();

    Object getObject(String objectName);

    Object addObject(String objectName);

    void removeObject(String objectName);

    List<Attachment> getAttachments();

    Attachment getAttachment(String attachmentName);

    Attachment addAttachment(String attachmentName);

    void removeAttachment(String attachmentName);

    // Note: In order to make modifications to the document's content, modify the returned XDOM
    // Default language
    XDOM getContent();
    XDOM getContent(Locale locale);

    // get/setSyntax(Syntax syntax)

    // Q: Should we have this or should we force users to use a Parser for a given syntax, ie make Document
    // independent of the Syntax?
    // Note: If we make them independent then we have the question of converting existing docs in the DB.
    //String setContent(String content);

    boolean hasObject(String objectName);

    boolean hasObjectDefinition(String objectDefinitionName);

    boolean hasAttachment(String attachmentName);

    //Q: What about aliases?
    DocumentReference getDocumentReference();
}
