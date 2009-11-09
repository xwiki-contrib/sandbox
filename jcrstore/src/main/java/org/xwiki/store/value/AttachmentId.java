package org.xwiki.store.value;

/**
 * immutable
 */
public class AttachmentId extends AbstractId
{
    private DocumentId documentId;
    private String filename;

    public AttachmentId(DocumentId documentId, String filename)
    {
        this.documentId = documentId;
        this.filename = filename;
    }

    public DocumentId getDocumentId()
    {
        return documentId;
    }

    public String getFilename()
    {
        return filename;
    }
}
