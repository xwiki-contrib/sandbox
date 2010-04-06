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
package org.xwiki.portlet;

/**
 * Convenient way of passing dispatch information.
 * 
 * @version $Id$
 */
public class Dispatch
{
    /**
     * The dispatch type.
     */
    private final DispatchType type;

    /**
     * The dispatch path.
     */
    private final String path;

    /**
     * Creates a new dispatch object with the given information.
     * 
     * @param type the dispatch type
     * @param path the dispatch type
     */
    public Dispatch(DispatchType type, String path)
    {
        this.type = type;
        this.path = path;
    }

    /**
     * @return the dispatch type
     */
    public DispatchType getType()
    {
        return type;
    }

    /**
     * @return the dispatch path
     */
    public String getPath()
    {
        return path;
    }
}
