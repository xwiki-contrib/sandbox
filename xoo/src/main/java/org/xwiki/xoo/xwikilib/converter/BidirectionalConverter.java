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

package org.xwiki.xoo.xwikilib.converter;

import org.xwiki.rendering.converter.ConversionException;

/**
 * Converts HTML to/from XWiki syntax
 * 
 * @version $Id$
 * @since 1.0 M
 */

public interface BidirectionalConverter
{
    /**
     * Converts the given source text from the specified syntax to HTML.
     * 
     * @param source the text to be converted
     * @param syntaxId the syntax identifier
     * @return the XHTML result of the conversion
     */
    String toXHTML(String source) throws ConversionException;

    /**
     * Converts the given HTML fragment to the specified syntax.
     * 
     * @param html the HTML text to be converted
     * @param syntaxId the syntax identifier
     * @return the result on the conversion
     */
    String fromXHTML(String html) throws ConversionException;

}
