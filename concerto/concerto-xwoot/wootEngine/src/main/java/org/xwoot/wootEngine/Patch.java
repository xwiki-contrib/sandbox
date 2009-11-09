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
package org.xwoot.wootEngine;

import java.io.Serializable;

import java.util.List;

/**
 * This class describes a patch that is to be applied on XWiki pages. The patch can contain changes in the content or in
 * the page's metadata.
 * 
 * @version $Id$
 */
public class Patch implements Serializable
{
    /** Unique ID used in the serialization process. */
    private static final long serialVersionUID = -2386840405304459581L;

    /**
     * List of {@link org.xwoot.wootEngine.op.WootOp WootOp} elements representing changes in a page's content.
     **/
    private List<Object> elements;

    /**
     * List of {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} elements representing changes in a page's
     * metadata.
     */
    private List<Object> mDelements;

    /** Id of the concerned page. */
    private String pageId;

    /** Id of the concerned object in the page. */
    private String objectId;

    /** Timestamp of the concerned page modification. */
    private long timestamp;

    /** Version of the concerned page modification. */
    private int version;

    /** Minor version of the concerned page modification. */
    private int minorVersion;

    /**
     * Creates a new Patch instance.
     * 
     * @param data List of {@link org.xwoot.wootEngine.op.WootOp WootOp} elements representing changes in a page's
     *            content.
     * @param metaData List of {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} elements representing
     *            changes in a page's metadata.
     * @param pageId the name (XWiki page id) of the page that this patch applies to.
     * @param objectId the content id that this patch applies to.
     * @param timestamp the timestamp of the modification
     * @param version the version of the page
     * @param minorV the minor version of the page
     */
    public Patch(List data, List metaData, String pageId, String objectId, long timestamp, int version, int minorV)
    {
        this.setData(data);
        this.setMDelements(metaData);
        this.setObjectId(objectId);
        this.setPageId(pageId);
        this.setTimestamp(timestamp);
        this.setVersion(version);
        this.setMinorVersion(minorV);

    }

    /**
     * Default constructor.
     */
    public Patch()
    {
        // void
    }

    /**
     * @return a List of {@link org.xwoot.wootEngine.op.WootOp WootOp} elements representing changes in the content of a
     *         page.
     */
    public Iterable<Object> getData()
    {
        return this.elements;
    }

    /**
     * @param elements a List of {@link org.xwoot.wootEngine.op.WootOp WootOp} elements representing changes in the
     *            content of a page.
     */
    public void setData(Iterable elements)
    {
        this.elements = (List) elements;
    }

    /**
     * @return a List of {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} elements representing changes
     *         in the metadata of a page.
     */
    public List<Object> getMDelements()
    {
        return this.mDelements;
    }

    /**
     * @param delements a List of {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp} elements representing
     *            changes in the metadata of a page.
     */
    public void setMDelements(List<Object> delements)
    {
        this.mDelements = delements;
    }

    /**
     * @return the name (XWiki page id) of the page that this patch applies to.
     */
    public String getPageId()
    {
        return this.pageId;
    }

    /**
     * @param pageName the name (XWiki page id) of the page on which to apply this patch.
     */
    public void setPageId(String pageName)
    {
        this.pageId = pageName;
    }

    /**
     * @return the id of the concerned object in the concerned page.
     */
    public String getObjectId()
    {
        return this.objectId;
    }

    /**
     * @param objectId the id to set.
     */
    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }

    /**
     * @return a String representation of the global id (pageId+"."+objectId)
     */
    public String globalId()
    {
        return this.pageId + "." + this.objectId;
    }

    /**
     * @param timestamp the timestamp to set.
     */
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    /**
     * @param version the version to set.
     */
    public void setVersion(int version)
    {
        this.version = version;
    }

    /**
     * @param minorVersion the minor version to set.
     */
    public void setMinorVersion(int minorVersion)
    {
        this.minorVersion = minorVersion;
    }

    /**
     * @return the timestamp of the concerned page modification.
     */
    public long getTimestamp()
    {
        return this.timestamp;
    }

    /**
     * @return the version of the concerned page modification.
     */
    public int getVersion()
    {
        return this.version;
    }

    /**
     * @return the minor version of the concerned page modification.
     */
    public int getMinorVersion()
    {
        return this.minorVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String ident = "   ";
        String newLine = "\n";

        StringBuilder toString = new StringBuilder();
        toString.append("Patch:\n");
        toString.append(" PageId: " + this.pageId + " ObjectId: " + objectId + newLine);
        toString.append(" Timestamp: " + this.timestamp + " Version: " + this.version + " MinorVersion: "
            + this.minorVersion + newLine);
        toString.append(" Contents: \n");

        toString.append("  WootOps: \n");
        if (this.elements != null) {
            for (Object wootOp : this.elements) {
                toString.append(ident + wootOp.toString() + newLine);
            }
        }

        toString.append("  TreOps: \n");
        if (this.mDelements != null) {
            for (Object treOp : this.mDelements) {
                toString.append(ident + treOp.toString() + newLine);
            }
        }
        toString.append("End Of Patch.");

        return toString.toString();
    }
}
