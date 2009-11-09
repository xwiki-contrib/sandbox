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


package org.xwiki.xoo.xwikilib;

import org.xwiki.rendering.wiki.WikiModel;
import org.xwiki.component.annotation.Component;

/**
 * XWiki Component that implements the WikiModel interface
 * 
 * @version $Id$
 * @since 1.0
 */

@Component
public class XOOWikiModel implements WikiModel
{

    /**
     * {@inheritDoc}
     */
    public String getAttachmentURL(String documentName, String attachmentName)
    {
        return attachmentName;
    }

    /**
     * {@inheritDoc}
     */
    public String getDocumentEditURL(String documentName, String anchor, String queryString)
    {
        return documentName;
    }

    /**
     * {@inheritDoc}
     */
    public String getDocumentViewURL(String documentName, String anchor, String queryString)
    {
        return documentName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDocumentAvailable(String documentName)
    {
        return true;
    }

}
