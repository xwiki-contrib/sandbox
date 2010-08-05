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
package org.xwiki.wikiimporter.wiki;

import java.util.List;
import java.util.Set;

import org.xwiki.rendering.block.XDOM;

/**
 * WikiPageRevision interface represents a Wiki Page Revision used during import process.
 * 
 * @version $Id$
 */
public interface WikiPageRevision
{
    /**
     * @return title of the Page.
     */
    public String getTitle();

    /**
     * @return the author of Page revision.
     */
    public String getAuthor();

    /**
     * @return the name of parent page.
     */
    public String getParent();

    /**
     * @return the list of attachments of a Page revision.
     */
    public List<Attachment> getAttachments();

    /**
     * @return the version of Page revision.
     */
    public String getVersion();

    /**
     * @return the text content of Page revision.
     */
    public XDOM getContent();

    /**
     * @return the comment on Page revision.
     */
    public String getComment();

    /**
     * @return <tt>true</tt> if the revision is a minor edit, <tt>false</tt> if not.
     */
    public boolean isMinorEdit();

    /**
     * @return the collection of tags associated with the Page.
     */
    public Set<String> getTags();
}
