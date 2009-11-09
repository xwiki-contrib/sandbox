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
package org.xwoot.wootEngine;

/**
 * Exception handling for the Woot Engine.
 * 
 * @version $Id$
 */
public class WootEngineException extends Exception
{

    /** Unique ID for the serialization process. */
    private static final long serialVersionUID = -2568698052987298242L;

    /** Default constructor. */
    public WootEngineException()
    {
        super();
    }

    /**
     * Constructor with cause as parameters.
     * 
     * @param cause the exception that caused this exception.
     **/
    public WootEngineException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor with message as parameter.
     * 
     * @param message a message describing the exception.
     **/
    public WootEngineException(String message)
    {
        super(message);
    }

    /**
     * Constructor with message and cause as parameters.
     * 
     * @param message a message describing the exception.
     * @param cause the exception that caused this exception.
     **/
    public WootEngineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
