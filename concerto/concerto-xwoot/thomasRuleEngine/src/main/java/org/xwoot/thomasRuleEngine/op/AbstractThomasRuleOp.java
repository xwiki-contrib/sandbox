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
package org.xwoot.thomasRuleEngine.op;

import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;

/**
 * Implements the behavior of a ThomasRuleOp, common to all implementations.
 * 
 * @version $Id$
 */
public abstract class AbstractThomasRuleOp implements ThomasRuleOp
{
    /** SerialVesrionUID for serialized object. */
    private static final long serialVersionUID = 3952443924428463564L;

    /**
     * @see #getId()
     */
    private Identifier id;

    /**
     * @see #getTimestampModif()
     */
    private Timestamp timestampModif;

    /**
     * @see #getTimestampIdCreation()
     */
    private Timestamp timestampIdCreation;

    /**
     * @see #getValue()
     */
    private Value value;

    /**
     * @see #isDeleted()
     */
    private boolean isDeleted;

    /**
     * Creates a new ThomasRuleOp object.
     * 
     * @param id the id of the affected entry.
     * @param value the new value of the affected entry.
     * @param isDeleted the new deletion status of the affected entry.
     * @param timestampIdCreation when the affected entry was created.
     * @param timestampModif when this operation affected the entry.
     */
    public AbstractThomasRuleOp(Identifier id, Value value, boolean isDeleted, Timestamp timestampIdCreation,
        Timestamp timestampModif)
    {
        this.id = id;
        this.value = value;
        this.isDeleted = isDeleted;
        this.timestampIdCreation = timestampIdCreation;
        this.timestampModif = timestampModif;
    }

    /**
     * Checks if this operation's timestamps are valid when comparing to an entry.
     * 
     * @param entry the entry to compare to.
     * @return false if the entry's timestampIdCreation is greater than this timestampIdCreation or if both the
     *         timestampIdCreation are equal but the entry's timestampModif is not smaller than this timestampModif;
     *         true otherwise, including when the entry is null.
     */
    protected boolean isOpTimestampsValid(Entry entry)
    {
        if (entry != null) {
            // existing TimestampIdCreation > : nothing to do
            if (entry.getTimestampIdCreation().compareTo(this.getTimestampIdCreation()) >= 1) {
                return false;
            } else if (entry.getTimestampIdCreation().compareTo(this.getTimestampIdCreation()) == 0) {
                // existing TimestampIdCreation == :

                // nothing to do when existing entry modif timestamp > given om
                // modif timestamp
                if (entry.getTimestampModif().compareTo(this.getTimestampModif()) > -1) {
                    return false;
                }
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    public abstract Entry execute(Entry from);

    /** {@inheritDoc} */
    public Identifier getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    public Timestamp getTimestampIdCreation()
    {
        return this.timestampIdCreation;
    }

    /** {@inheritDoc} */
    public Timestamp getTimestampModif()
    {
        return this.timestampModif;
    }

    /** {@inheritDoc} */
    public Value getValue()
    {
        return this.value;
    }

    /** {@inheritDoc} */
    public boolean isDeleted()
    {
        return this.isDeleted;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String separator = ",";
        return "Op(" + this.getId() + separator + this.getValue() + separator + this.isDeleted() + separator
            + this.getTimestampIdCreation() + separator + this.getTimestampModif() + ")";
    }
}
