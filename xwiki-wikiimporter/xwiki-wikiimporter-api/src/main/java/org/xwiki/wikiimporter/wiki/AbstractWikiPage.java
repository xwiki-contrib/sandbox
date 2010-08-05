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

import java.util.LinkedList;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class AbstractWikiPage implements WikiPage
{

    protected LinkedList<WikiPageRevision> pageRevisionList = new LinkedList<WikiPageRevision>();

    protected LinkedList<Attachment> attachmentList = new LinkedList<Attachment>();

    protected String space;

    protected String pageName;

    protected String wiki;

    public AbstractWikiPage(String defaultSpace, String wiki)
    {
        this.space = defaultSpace;
        this.wiki = wiki;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getAttachments()
     */
    public List<Attachment> getAttachments()
    {
        return attachmentList;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getLastRevision()
     */
    public WikiPageRevision getLastRevision()
    {
        return pageRevisionList.getLast();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getRevisions()
     */
    public List<WikiPageRevision> getRevisions()
    {
        return pageRevisionList;
    }

    public void setSpace(String space)
    {
        this.space = space;
    }

    public void setWiki(String wiki)
    {
        this.wiki = wiki;
    }

    public void addRevision(WikiPageRevision revision)
    {
        pageRevisionList.addLast(revision);
    }

    public void addAttachment(Attachment attachment)
    {
        attachmentList.add(attachment);
    }
}
