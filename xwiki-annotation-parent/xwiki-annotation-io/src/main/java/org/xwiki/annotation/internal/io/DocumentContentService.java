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

package org.xwiki.annotation.internal.io;

import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * XWiki document source retrieval and rendering function.
 * 
 * @version $Id$
 */
@Component
public class DocumentContentService implements IOTargetService
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.io.DefaultIOService#getSource(java.lang.CharSequence,
     *      com.xpn.xwiki.XWikiContext)
     */
    public String getSource(CharSequence documentName, XWikiContext deprecatedContext) throws IOServiceException
    {
        try {
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            String t = document.getContent().replace("\r", "");
            return t;
        } catch (XWikiException e) {
            throw new IOServiceException("An exception message has occurred while getting the source of the document "
                + documentName, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.io.DefaultIOService#getRenderedContent(java.lang.CharSequence, String,
     *      com.xpn.xwiki.XWikiContext)
     */
    public CharSequence getRenderedContent(CharSequence documentName, String context, XWikiContext deprecatedContext)
        throws IOServiceException
    {
        try {
            // TODO FIX XWikiMessageTool
            // deprecatedContext.getMessageTool();
            // This is required in order to have message tool initialized
            // quite weird isn't it ?
            deprecatedContext.getMessageTool();
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            return document.getRenderedContent(context, document.getSyntaxId(), deprecatedContext);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while getting the rendered content for document "
                + documentName.toString(), e);
        }
    }
}
