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

package org.xwiki.annotation.io;

import org.xwiki.component.annotation.ComponentRole;

/**
 * This service provides functions to operate with annotations targets (wiki documents or objects).
 * 
 * @version $Id$
 */
@ComponentRole
public interface IOTargetService
{
    /**
     * @param documentName concerned document
     * @param source to be rendered
     * @return html content corresponding to given source
     * @throws IOServiceException if any exception occurs when manipulating documents
     */
    String getRenderedContent(String documentName, String source) throws IOServiceException;

    /**
     * @param documentName concerned document
     * @return source of given document
     * @throws IOServiceException if any exception occurs when manipulating documents
     */
    String getSource(String documentName) throws IOServiceException;

    /**
     * @param documentName the name of the document whose source syntax to return
     * @return the syntax of the source of the given document
     * @throws IOServiceException if any exception occurs when manipulating documents
     */
    String getSourceSyntax(String documentName) throws IOServiceException;
}
