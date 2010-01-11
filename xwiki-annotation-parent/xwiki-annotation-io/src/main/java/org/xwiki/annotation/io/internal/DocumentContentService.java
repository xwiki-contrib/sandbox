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

package org.xwiki.annotation.io.internal;

import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

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
     * The execution used to get the deprecated XWikiContext.
     */
    @Requirement
    private Execution execution;

    /**
     * Document access bridge to manipulate xwiki documents.
     */
    @Requirement
    private DocumentAccessBridge dab;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.internal.DefaultIOService#getSource(String)
     */
    public String getSource(String documentName) throws IOServiceException
    {

        try {
            return dab.getDocumentContent(documentName.toString());
        } catch (Exception e) {
            throw new IOServiceException("An exception message has occurred while getting the source of the document "
                + documentName, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOTargetService#getSourceSyntax(java.lang.String)
     */
    public String getSourceSyntax(String documentName) throws IOServiceException
    {
        try {
            return dab.getDocumentSyntaxId(documentName);
        } catch (Exception e) {
            throw new IOServiceException(
                "An exception has occurred while getting the syntax of the source for document " + documentName, e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.internal.DefaultIOService#getRenderedContent(String, String)
     */
    public String getRenderedContent(String documentName, String context) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            // set up messaging tools to have all velocity scripts well executed when document renders
            deprecatedContext.getWiki().prepareResources(deprecatedContext);
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            return document.getRenderedContent(context, document.getSyntaxId(), deprecatedContext);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while getting the rendered content for document "
                + documentName.toString(), e);
        }
    }

    /**
     * @return the deprecated xwiki context used to manipulate xwiki objects
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
