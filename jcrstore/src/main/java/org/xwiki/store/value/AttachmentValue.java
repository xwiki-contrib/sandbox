package org.xwiki.store.value;

import java.io.InputStream;
import java.util.Date;

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class AttachmentValue implements Value<AttachmentId>
{
    public DocumentId documentId;

    @JcrProperty public int filesize;

    @JcrName public String filename;

    @JcrProperty public String author;

    @JcrProperty public String version;

    @JcrProperty public String comment;

    @JcrProperty public Date date;

    @JcrProperty public InputStream content;

    @SuppressWarnings("unused")
    @JcrPath private String jcrPath;

    public AttachmentId getId()
    {
        return new AttachmentId(documentId, filename);
    }
}
