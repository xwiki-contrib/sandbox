package org.xwiki.model;

import java.util.List;

import javax.jcr.Node;

public interface Document extends Node
{
    /**
     * @return the list of object definitions defined inside this document
     */
    List<ObjectDefinition> getObjectDefinitions();

    List<Object> getObjects();

    List<Attachment> getAttachments();
    
    String setContent(String content);
}
