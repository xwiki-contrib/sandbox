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

/**
 * A class representing an XWoot Id, i.e. a page id and a timestamp. This id is used to reference a modification.
 * 
 * @vesion $Id$
 */
public class XWootId
{
    private String pageId;

    private long timestamp;

    private int version;

    private int minorVersion;

    public XWootId(String pageId, long timestamp, int version, int minorVersion)
    {
        this.pageId = pageId;
        this.timestamp = timestamp;
        this.version = version;
        this.minorVersion = minorVersion;
    }

    public String getPageId()
    {
        return pageId;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public int getVersion()
    {
        return version;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + minorVersion;
        result = prime * result + ((pageId == null) ? 0 : pageId.hashCode());
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XWootId other = (XWootId) obj;
        if (minorVersion != other.minorVersion)
            return false;
        if (pageId == null) {
            if (other.pageId != null)
                return false;
        } else if (!pageId.equals(other.pageId))
            return false;
        if (timestamp != other.timestamp)
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("[XWootId: %s %d %d.%d]", pageId, timestamp, version, minorVersion);
    }

}
