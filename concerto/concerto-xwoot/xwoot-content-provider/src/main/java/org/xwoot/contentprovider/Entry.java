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

public class Entry
{
    private String pageId;

    private long timestamp;

    private int version;

    private int minorVersion;

    private boolean cleared;

    public Entry(String pageId, long timestamp, int version, int minorVersion, boolean cleared)
    {
        this.pageId = pageId;
        this.timestamp = timestamp;
        this.version = version;
        this.minorVersion = minorVersion;
        this.cleared = cleared;
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

    public boolean isCleared()
    {
        return cleared;
    }

}
