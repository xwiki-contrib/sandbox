package org.xwiki.model;

import org.xwiki.rendering.syntax.Syntax;

import java.util.List;
import java.util.Locale;

public interface Document extends Entity
{
    Locale getLocale();

    // Q: Should we have instead: setContent(Content content) with Content encapsulating the syntax?
    Syntax getSyntax();
    void setSyntax(Syntax syntax);

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

    // Note: returning a XDOM is a problem because it would require Renderers for all syntaxes (for example).
    String getContent();
    void setContent(String content);

    boolean hasObject(String objectName);

    boolean hasObjectDefinition(String objectDefinitionName);

    boolean hasAttachment(String attachmentName);
}
