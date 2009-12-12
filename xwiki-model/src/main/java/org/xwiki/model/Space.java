package org.xwiki.model;

import java.util.List;

public interface Space extends Persistable
{
    /**
     * @return the space's description
     * @todo Should not be implemented with the old model
     */
    String getDescription();

    /**
     * @return the full list of all nested spaces
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
    void addSpace(String spaceName);

    /**
     * @todo Should not be implemented with the old model
     */
    Space createSpace(String spaceName);

    /**
     * @todo Should not be implemented with the old model
     */
    void removeSpace(String spaceName);

    List<Document> getDocuments();

    boolean hasSpace(String spaceName);

    boolean hasDocument(String shortDocumentName);

    Document getDocument(String shortDocumentName);

    void addDocument(Document document);

    void removeDocument(String shortDocumentName);

    Document createDocument(String shortDocumentName);
}
