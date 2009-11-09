package org.xwiki.model;

import java.util.List;

import javax.jcr.Node;

public interface Space extends Node
{
    /**
     * @return the space's description
     */
    String getDescription();

    /**
     * @return the full list of all nested spaces
     */
    List<Space> getSpaces();

    void addSpace(String spaceName);
    
    void removeSpace(String spaceName);

    void addDocument(Document document);

    void removeDocument(String documentName);
    
    Document createDocument(String documentName);
}
