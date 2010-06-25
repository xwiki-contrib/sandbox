package org.xwiki.model;

import java.util.List;

public interface Space extends Entity
{
    /**
     * @return the list of top level Space objects in this Space (excluding nested spaces)
     */
    List<Space> getSpaces();

    /**
     * @param spaceName the name of the nested space to look for
     * @return the nested space whose name is passed as parameter
     */
    Space getSpace(String spaceName);

    /**
     * @todo Should not be implemented with the old model
     */
    Space addSpace(String spaceName);

    /**
     * @todo Should not be implemented with the old model
     */
    void removeSpace(String spaceName);

    boolean hasSpace(String spaceName);

    List<Document> getDocuments();

    boolean hasDocument(String documentName);

    Document getDocument(String documentName);

    Document addDocument(String documentName);

    void removeDocument(String documentName);
}
