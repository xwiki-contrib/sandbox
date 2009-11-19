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

package org.xwiki.annotation.selection;

/**
 * Thrown when we can't match altered selection in altered document source.
 * 
 * @version $Id$
 */
public class SelectionMappingException extends Exception
{
    /**
     * Serial version number for this type.
     */
    private static final long serialVersionUID = -8811533030070926444L;

    /**
     * Builds a selection mapping exception with the specified message.
     * 
     * @param message the message of this exception
     */
    public SelectionMappingException(String message)
    {
        super(message);
    }
}
