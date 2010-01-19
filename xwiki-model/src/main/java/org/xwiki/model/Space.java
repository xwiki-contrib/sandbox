package org.xwiki.model;

import org.xwiki.model.reference.SpaceReference;

import java.util.List;

public interface Space extends Persistable
{
    /**
     * @return the space's description
     * @todo Should not be implemented with the old model
     */
    String getDescription();

    /**
     * @return the names of all nested spaces
     */
    List<String> getSpaceNames();

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

    boolean hasSpace(String spaceName);

    List<String> getDocumentNames();

    boolean hasDocument(String documentName);

    Document getDocument(String documentName);

    void addDocument(Document document);

    void removeDocument(String documentName);

    Document createDocument(String documentName);

    SpaceReference getSpaceReference();
}
