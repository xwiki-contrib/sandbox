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
package org.xwiki.spaces;

/**
 * A generic exception for wrapping exceptions that are caught when executing methods of group manager implementations.
 * They are typically exceptions that we want to catch at the user API level.
 * 
 * @version $Id$
 */
public class GroupManagerException extends Exception
{

    /**
     * Constructor of this exception.
     * 
     * @param t the original exception.
     */
    public GroupManagerException(Throwable t)
    {
        super(t);
    }

    /**
     * Constructor of this exception.
     * 
     * @param t the original exception.
     * @param message the message to associated with the error.
     */
    public GroupManagerException(String message, Throwable t)
    {
        super(message, t);
    }

}
