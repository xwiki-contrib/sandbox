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
package org.xwiki.component.wiki;

/**
 * Exception thrown by component builders when a document holds an invalid compoentn definition.
 * (For example if no role has been specified).
 * 
 *  @since 2.4-M2
 *  @version $Id$
 */
public class InvalidComponentDefinitionException extends Exception
{
    /**
     * Constructor of this exception.
     */
    public InvalidComponentDefinitionException()
    {
        super();
    }

    /**
     * Constructor of this exception.
     * 
     * @param message a message associated with the exception, that explains why the definition is invalid 
     */
    public InvalidComponentDefinitionException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor of this exception.
     * 
     * @param message a message associated with the exception, that explains why the definition is invalid
     * @param t the root cause 
     */
    public InvalidComponentDefinitionException(String message, Throwable t)
    {
        super(message);
    }
}
