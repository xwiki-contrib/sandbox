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
package org.xwoot.thomasRuleEngine;

/**
 * Exception handling for LPBcast module.
 * 
 * @version $Id$
 */
public class ThomasRuleEngineException extends Exception
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -2568698052987298242L;

    /**
     * @see Exception#Exception()
     */
    public ThomasRuleEngineException()
    {
        super();
    }

    /**
     * @param cause the cause.
     * @see Exception#Exception(Throwable)
     */
    public ThomasRuleEngineException(Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message the message.
     * @see Exception#Exception(String)
     */
    public ThomasRuleEngineException(String message)
    {
        super(message);
    }

    /**
     * @param message the message.
     * @param cause the cause.
     * @see Exception#Exception(String, Throwable)
     */
    public ThomasRuleEngineException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
