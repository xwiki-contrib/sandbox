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
package org.xwiki.wikiimporter;

/**
 * Encapsulate wiki importer error.
 * 
 * @version $Id$
 */
public class WikiImporterException extends Exception
{

    /**
     * Class version.
     */
    private static final long serialVersionUID = 162832454803316987L;

    /**
     * Constructs a new exception with the specified message.
     * 
     * @param message The explanation of the exception.
     */
    public WikiImporterException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param throwable The underlying cause for this exception.
     */
    public WikiImporterException(Throwable throwable)
    {
        super(throwable);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * 
     * @param message The explanation of the exception.
     * @param throwable The underlying cause for this exception.
     */
    public WikiImporterException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
