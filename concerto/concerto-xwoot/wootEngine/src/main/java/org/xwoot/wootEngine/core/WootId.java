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
package org.xwoot.wootEngine.core;

import java.io.Serializable;

/**
 * Unique identifier in all P2P networks composed of siteId + internal clock value.
 * 
 * @version $Id$
 */
public class WootId implements Comparable<WootId>, Serializable, Cloneable
{
    /** Id of beginning of content. */
    public static final WootId FIRST_WOOT_ID = new WootId(String.valueOf(-1), -1);

    /** Id of end char. */
    public static final WootId LAST_WOOT_ID = new WootId(String.valueOf(-2), -2);

    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -471886787872933727L;

    /** ID of the P2P Node on which it was created. (of the WootEngine) */
    private String siteId;

    /** Stores the value of the clock when it was created. */
    private int localClock;

    /**
     * Creates a new WootId object.
     * 
     * @param siteId the id of the WootEngine that created it.
     * @param localClock the value of the clock when it was created.
     */
    public WootId(String siteId, int localClock)
    {
        this.siteId = siteId;
        this.localClock = localClock;
    }

    /** {@inheritDoc} */
    public int compareTo(WootId other)
    {
        if (this == WootId.FIRST_WOOT_ID || other == WootId.LAST_WOOT_ID) {
            return -1;
        }

        if (this == WootId.LAST_WOOT_ID || other == WootId.FIRST_WOOT_ID) {
            return 1;
        }

        if (this.siteId == other.siteId) {
            return this.localClock - other.localClock;
        }

        return this.siteId.compareTo(other.siteId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        WootId other = (WootId) obj;

        return (this.siteId.equals(other.siteId)) && (this.localClock == other.localClock);
    }

    /**
     * @return the value of the clock when it was created
     */
    public int getLocalClock()
    {
        return this.localClock;
    }

    /**
     * @param localClock the localClock to set.
     * @see #getLocalClock()
     */
    public void setLocalClock(int localClock)
    {
        this.localClock = localClock;
    }

    /**
     * @return the ID of the P2P Node on which it was created. (of the WootEngine)
     */
    public String getSiteid()
    {
        return this.siteId;
    }

    /**
     * @param siteId the siteId to set.
     * @see #getSiteid()
     */
    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "(wid " + this.siteId + "," + this.localClock + ")";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + this.siteId.hashCode();
        hash = 31 * hash + this.localClock;
        return hash;
    }

}
