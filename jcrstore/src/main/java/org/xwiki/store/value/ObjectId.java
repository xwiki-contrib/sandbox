package org.xwiki.store.value;

/**
 * immutable
 */
public class ObjectId extends AbstractId
{
    private DocumentId documentId;
    private int number;

    public ObjectId(DocumentId documentId, int number)
    {
        super();
        this.documentId = documentId;
        this.number = number;
    }

    public DocumentId getDocumentId()
    {
        return documentId;
    }

    public int getNumber()
    {
        return number;
    }
}
