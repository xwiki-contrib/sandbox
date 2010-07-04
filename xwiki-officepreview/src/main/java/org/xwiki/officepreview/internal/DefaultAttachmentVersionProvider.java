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
package org.xwiki.officepreview.internal;

import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Default implementation of {@link AttachmentVersionProvider}.
 * 
 * @version $Id$
 */
public class DefaultAttachmentVersionProvider implements AttachmentVersionProvider
{
    /**
     * Access to current execution.
     */
    private Execution execution;

    /**
     * Creates a new {@link AttachmentVersionProvider} instance.
     * 
     * @param execution used to access current execution.
     */
    public DefaultAttachmentVersionProvider(Execution execution)
    {
        this.execution = execution;
    }

    /**
     * {@inheritDoc}
     */
    public String getAttachmentVersion(AttachmentReference attachmentReference) throws Exception
    {
        XWikiContext xcontext = getContext();
        XWikiDocument doc = xcontext.getWiki().getDocument(attachmentReference.getDocumentReference(), xcontext);
        XWikiAttachment attachment = doc.getAttachment(attachmentReference.getName());
        return attachment.getVersion();
    }
    
    /**
     * Used to retrieve a reference to {@link XWikiContext}.
     * 
     * @return {@link XWikiContext} instance.
     */
    private XWikiContext getContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
