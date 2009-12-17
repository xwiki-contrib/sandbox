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
package org.xwiki.annotation.renderer;

import org.xwiki.rendering.listener.chaining.EventType;

/**
 * Reference to an xwiki event in a processing, defined by the event type and the index of that event in the list of
 * events of the same type.
 * 
 * @version $Id$
 */
public class EventReference
{
    /**
     * The index of this event in the list of all events of this type in the current rendering.
     */
    private int index;

    /**
     * The referred event.
     */
    private EventType type;

    /**
     * Builds an event from a queue listener event and an index.
     * 
     * @param evtType the type of the original queue listener event
     * @param index the index of this event in the list of emitted events of this type.
     */
    public EventReference(EventType evtType, int index)
    {
        type = evtType;
        this.index = index;
    }

    /**
     * @return the index
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * {@inheritDoc}. Customized to identify an event reference by the wrapped event type and position in the emitted
     * events of this type.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof EventReference) {
            // they're equal if they're the same type and have the same index
            return (((EventReference) obj).type == type && ((EventReference) obj).index == index);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        // build an unique hashCode from the type and index, corresponding to the equals function
        return (type.toString() + index).hashCode();
    }
}
