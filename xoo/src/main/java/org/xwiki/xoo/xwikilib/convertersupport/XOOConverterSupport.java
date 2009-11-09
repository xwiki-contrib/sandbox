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

package org.xwiki.xoo.xwikilib.convertersupport;

import java.util.List;

import org.codehaus.swizzle.confluence.Attachment;
import org.xwiki.rendering.syntax.Syntax;

public interface XOOConverterSupport
{
    /**
     * Gets all image attachments from an input content.  
     * @param source the input text
     * @param syntax the syntax in which the text was written
     * @return a list with all the attachments from the given input
     */
    List<Attachment> getAllImageAttachments(String source, Syntax syntax);

    /**
     * Creates a list with all the attachments from the input text and modifies the image blocks from the syntax. 
     * @param source the input text
     * @param syntax the syntax in which the text was written 
     * @param attachments the list with all the attachments from given input
     * @return the modified content
     */
    String imageNameCleaner(String source, Syntax syntax, List<Attachment> attachments);
}
