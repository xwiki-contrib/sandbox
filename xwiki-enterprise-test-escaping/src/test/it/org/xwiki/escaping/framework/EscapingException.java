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
package org.xwiki.escaping.framework;


/**
 * Exception thrown on various escaping errors. Can handle a list of errors.
 * 
 * @version $Id$
 * @since 2.5
 */
public class EscapingException extends Exception
{
    /** Serial version ID. */
    private static final long serialVersionUID = 7784831403359592333L;

    /**
     * Create new EscapingException.
     * 
     * @param message error message
     */
    public EscapingException(String message)
    {
        super(message);
    }

    /**
     * Create new EscapingException.
     * 
     * @param cause error cause
     */
    public EscapingException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Create new EscapingException.
     * 
     * @param message error message
     * @param cause error cause
     */
    public EscapingException(String message, Throwable cause)
    {
        super(message, cause);
    }

}

