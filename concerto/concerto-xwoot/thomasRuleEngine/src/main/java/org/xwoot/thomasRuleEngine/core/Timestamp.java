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
package org.xwoot.thomasRuleEngine.core;

import java.io.Serializable;

/**
 * A timestamp containing the real time and the id of the {@link org.xwoot.thomasRuleEngine.ThomasRuleEngine
 * ThomasRuleEngine}. This timestamp is to be used in {@link org.xwoot.thomasRuleEngine.op.ThomasRuleOp ThomasRuleOp}
 * objects.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc677">RFC677 - The Maintenance of Duplicate Databases</a>
 * @version $Id$
 */
@SuppressWarnings("unchecked")
public class Timestamp implements Comparable, Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = 6774663614344345010L;

    /** The time in milliseconds this timestamp was created. */
    private long time;

    /** The id of the site that generated this ID. */
    private String siteId;

    /**
     * Creates a new Timestamp object.
     * 
     * @param time the current time in milliseconds.
     * @param siteId the siteId where this id was generated.
     */
    public Timestamp(long time, String siteId)
    {
        this.time = time;
        this.siteId = siteId;
    }

    /** @return the id of the site that generated this ID. */
    public String getId()
    {
        return this.siteId;
    }

    /** @return the time in milliseconds this timestamp was created. */
    public long getTime()
    {
        return this.time;
    }

    /** {@inheritDoc} */
    public int compareTo(Object o)
    {
        if (!(o instanceof Timestamp)) {
            throw new ClassCastException("Class cast problem : Timestamp expected");
        }

        Timestamp with = (Timestamp) o;

        if (this.equals(with)) {
            return 0;
        }

        if ((this.time > with.getTime())
            || ((this.time == with.getTime()) && (this.siteId.compareTo(with.getId()) > 0))) {
            return 1;
        }

        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final Timestamp other = (Timestamp) obj;

        return (this.siteId.equals(other.siteId) && this.time == other.time);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 7;
        result = prime * result + this.siteId.hashCode();
        result = prime * result + (int) (this.time ^ (this.time >>> 32));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Timestamp(" + this.time + "," + this.siteId + ")";
    }
}
