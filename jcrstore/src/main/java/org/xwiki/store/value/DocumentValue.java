package org.xwiki.store.value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class DocumentValue implements Value<DocumentId>
{
    @JcrProperty public String title;
    @JcrProperty public String parent;
    @JcrProperty public String space;
    @JcrName     public String name;
    @JcrProperty public String content;
    @JcrProperty public String meta;
    @JcrProperty public String format;
    @JcrProperty public String creator;
    @JcrProperty public String author;
    @JcrProperty public String contentAuthor;
    @JcrProperty public String customClass;

    @JcrProperty public Date date;
    @JcrProperty public Date contentUpdateDate;
    @JcrProperty public Date creationDate;

    @JcrProperty public String version;
    @JcrProperty public long id = 0;
    @JcrProperty public String language;
    @JcrProperty public String defaultLanguage;
    @JcrProperty public int translation;
    public String database;
    @JcrProperty public String comment;
    @JcrProperty public String syntaxId;
    @JcrProperty public boolean isMinorEdit = false;
    @JcrProperty public boolean isContentDirty = true;
    @JcrProperty public boolean isMetaDataDirty = true;
    @JcrChildNode public List<AttachmentValue> attachments = new ArrayList<AttachmentValue>(0);
    @JcrChildNode public List<ObjectValue> objects = new ArrayList<ObjectValue>(0);

    @SuppressWarnings("unused")
    @JcrPath     private String jcrPath;

    public DocumentId getId()
    {
        return new DocumentId(database, space, name, language);
    }
}
