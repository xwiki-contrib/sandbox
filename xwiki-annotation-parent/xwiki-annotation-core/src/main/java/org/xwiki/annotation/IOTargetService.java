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

package org.xwiki.annotation;

import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;

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
     * @param deprecatedContext the XWiki context needed to operate with XWiki objects
     * @return html content corresponding to given source
     * @throws IOServiceException can be thrown if any exception occurs when manipulating documents
     */
    CharSequence getRenderedContent(CharSequence documentName, String source, XWikiContext deprecatedContext)
        throws IOServiceException;

    /**
     * @param documentName concerned document
     * @return source of given document
     * @param deprecatedContext the XWiki context needed to operate with XWiki objects
     * @throws IOServiceException can be thrown if any exception occurs when manipulating documents
     */
    String getSource(CharSequence documentName, XWikiContext deprecatedContext) throws IOServiceException;
}
