package org.xwiki.store.value;

import java.util.Map;

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class ObjectValue implements Value<ObjectId>
{
    public DocumentId documentId;

    /** = Document fullName */
    @JcrProperty public String name;

    @JcrProperty public int number;

    @JcrProperty public String className;

    public Map<String, Object> properties;

    public ObjectId getId()
    {
        return new ObjectId(documentId, number);
    }

    @JcrName public String jcrName;

    @SuppressWarnings("unused")
    @JcrPath private String jcrPath;
}
