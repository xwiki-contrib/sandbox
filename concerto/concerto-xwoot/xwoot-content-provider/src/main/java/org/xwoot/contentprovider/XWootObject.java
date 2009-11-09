/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwoot.contentprovider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an XWootObject. XWootObjects are the abstraction used for representing XWiki data structures
 * (i.e. XWikiPages and XWikiObejcts) in a uniform way and for communicating them to the XWoot engine. XWootObjects can
 * be cumulative or non-cumulative (i.e., at most one instance of the underlying entity can exist per page). Pages, and
 * several types of XWikiObjects are an example what is considered a non-cumulative XWootObjects. Comments, Calendar
 * Events, etc. are examples of what is considerer a cumulative XWootObject. For more details see: {@link http
 * ://concerto.xwiki.com/xwiki/bin/view/Main/APIChat281108}
 * 
 * @version $Id$
 */
public class XWootObject implements Serializable
{
    /**
     * For serialization.
     */
    private static final long serialVersionUID = 155439744078747171L;

    /**
     * The pageId of the page this XWootObject is built from.
     */
    private String pageId;

    private Integer pageVersion;

    private Integer pageMinorVersion;

    /**
     * The GUID for uniquely identifying this object. See: {@link http
     * ://concerto.xwiki.com/xwiki/bin/view/Main/APIChat101208}
     */
    private String guid;

    /**
     * True if the object is cumulative.
     */
    private boolean cumulative;

    /**
     * True if this object didn't exist in the previous version of the page from where it comes from.
     */
    private boolean newlyCreated;

    /**
     * The list of the fields that make up the object.
     */
    private List<XWootObjectField> fields;

    private Map<String, String> metadata;

    public XWootObject(String pageId, Integer pageVersion, Integer pageMinorVersion, String guid, boolean cumulative,
        List<XWootObjectField> fields, boolean newlyCreated)
    {
        this.pageId = pageId;
        this.pageVersion = pageVersion;
        this.pageMinorVersion = pageMinorVersion;
        this.guid = guid;
        this.cumulative = cumulative;
        this.fields = fields;
        this.newlyCreated = newlyCreated;
        this.metadata = new HashMap<String, String>();
    }

    public String getPageId()
    {
        return pageId;
    }

    public List<String> getFieldNames()
    {
        List<String> result = new ArrayList<String>();
        for (XWootObjectField field : fields) {
            result.add(field.getName());
        }

        return result;
    }

    public List<XWootObjectField> getFields()
    {
        return fields;
    }

    public Serializable getFieldValue(String name)
    {
        XWootObjectField field = lookupField(name);
        return field != null ? field.getValue() : null;
    }

    public void setFieldValue(String name, Serializable value)
    {
        XWootObjectField field = lookupField(name);
        if (field != null) {
            field.setValue(value);
        }
    }

    public boolean isFieldWootable(String name)
    {
        XWootObjectField field = lookupField(name);
        return field != null ? field.isWootable() : false;
    }

    public String getGuid()
    {
        return guid;
    }

    public boolean isCumulative()
    {
        return cumulative;
    }

    public boolean isNewlyCreated()
    {
        return newlyCreated;
    }

    public boolean hasWootableFields()
    {
        for (XWootObjectField field : fields) {
            if (field.isWootable()) {
                return true;
            }
        }

        return false;
    }

    private XWootObjectField lookupField(String name)
    {
        for (XWootObjectField field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fields == null) ? 0 : fields.hashCode());
        result = prime * result + ((guid == null) ? 0 : guid.hashCode());
        result = prime * result + ((pageId == null) ? 0 : pageId.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof XWootObject))
            return false;
        XWootObject other = (XWootObject) obj;
        if (fields == null) {
            if (other.fields != null)
                return false;
        } else if (!fields.equals(other.fields))
            return false;
        if (guid == null) {
            if (other.guid != null)
                return false;
        } else if (!guid.equals(other.guid))
            return false;
        if (pageId == null) {
            if (other.pageId != null)
                return false;
        } else if (!pageId.equals(other.pageId))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        Formatter f = new Formatter();
        f.format("XWootObject\n");
        f.format(" PageId: %s\n", pageId);
        f.format(" Page version: %d.%d", pageVersion, pageMinorVersion);
        f.format(" Cumulative: %b\n", cumulative);
        f.format(" GUID: %s\n", guid);
        f.format(" Newly created: %b\n", newlyCreated);
        f.format(" Metadata: %s\n", metadata);
        for (XWootObjectField field : fields) {
            f.format(" Field '%s': %s (wootable: %b)\n", field.getName(), field.getValue(), field.isWootable());
        }
        f.format("\n");

        return f.toString();
    }

    public Integer getPageVersion()
    {
        return pageVersion;
    }

    public Integer getPageMinorVersion()
    {
        return pageMinorVersion;
    }

    public void setPageVersion(Integer pageVersion)
    {
        this.pageVersion = pageVersion;
    }

    public void setPageMinorVersion(Integer pageMinorVersion)
    {
        this.pageMinorVersion = pageMinorVersion;
    }

    public boolean isPage()
    {
        String namespace = guid.split(":")[0];

        if (namespace.equals(Constants.PAGE_NAMESPACE)) {
            return true;
        }

        return false;
    }

    public Map<String, String> getMetadataMap()
    {
        return metadata;
    }

    public void setMetadataMap(Map<String, String> metadata)
    {
        this.metadata = metadata;
    }

    public void putMetadata(String key, String value)
    {
        metadata.put(key, value);
    }

    public String getMetadata(String key)
    {
        return metadata.get(key);
    }

    public Set<String> getMetadataKeys()
    {
        return metadata.keySet();
    }

}
