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

import java.io.Serializable;

import org.xwoot.thomasRuleEngine.core.Entry;
import org.xwoot.thomasRuleEngine.core.Identifier;
import org.xwoot.thomasRuleEngine.core.Timestamp;
import org.xwoot.thomasRuleEngine.core.Value;

/**
 * Defines the structure of an operation as described in RFC677.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc677">RFC677 - The Maintenance of Duplicate Databases</a>
 * @version $Id$
 */
public interface ThomasRuleOp extends Serializable
{
    /** @return the id of the affected entry. */
    Identifier getId();

    /** @return when the affected entry was created. */
    Timestamp getTimestampIdCreation();

    /** @return when this operation affected the entry. */
    Timestamp getTimestampModif();

    /** @return the new value of the affected entry. */
    Value getValue();

    /** @return the new deletion status of the affected entry. */
    boolean isDeleted();

    /**
     * Execute this operation.
     * 
     * @param from the entry from which to determine the operation's type.
     * @return the resulting entry.
     */
    Entry execute(Entry from);

    /**
     * {@inheritDoc}
     */
    String toString();
}
