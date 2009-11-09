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
 * Entry Assignment operation.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc677">RFC677 - The Maintenance of Duplicate Databases</a>
 * @version $Id$
 */
public class ThomasRuleOpSet extends AbstractThomasRuleOp
{
    /** Unique ID used in the serialization process. */
    private static final long serialVersionUID = 4431965730152629744L;

    /**
     * Creates a new ThomasRuleOpSet object.
     * 
     * @param id the id of the affected entry.
     * @param value the new value of the affected entry.
     * @param isDeleted the new deletion status of the affected entry.
     * @param timestampIdCreation when the affected entry was created.
     * @param timestampModif when this operation affected the entry.
     */
    public ThomasRuleOpSet(Identifier id, Value value, boolean isDeleted, Timestamp timestampIdCreation,
        Timestamp timestampModif)
    {
        super(id, value, isDeleted, timestampIdCreation, timestampModif);
    }

    /**
     * {@inheritDoc}
     * 
     * @return if the entry is null, a ThomasRuleOpNew will be created by using this operation's data and its result
     *         will be returned.
     *         <p>
     *         If this operation's timestamps are not valid when compared to the entry, null is returned.
     *         <p>
     *         Otherwise, an new entry will be returned having it's data the data of this operation.
     */
    @Override
    public Entry execute(Entry from)
    {
        if (from == null) {
            return (new ThomasRuleOpNew(this.getId(), this.getValue(), this.isDeleted(), this.getTimestampIdCreation(),
                this.getTimestampModif())).execute(null);
        } else if (!this.isOpTimestampsValid(from)) {
            return null;
        }

        return new Entry(this.getId(), this.getValue(), this.isDeleted(), this.getTimestampIdCreation(), this
            .getTimestampModif());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Set" + super.toString();
    }
}
