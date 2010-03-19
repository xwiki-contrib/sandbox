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
 * Exception thrown when attempting to create a space with a key that does not validate against the regular expression
 * of valid space keys defined in the configuration.
 * 
 * @version $Id$
 */
public class IllegalSpaceKeyException extends Exception
{
    /**
     * Our constructor.
     * 
     * @param message the message associated with this exception.
     */
    public IllegalSpaceKeyException(String message)
    {
        super(message);
    }
}
