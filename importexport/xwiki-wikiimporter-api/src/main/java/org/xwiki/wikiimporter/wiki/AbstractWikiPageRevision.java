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

import org.xwiki.rendering.block.XDOM;

/**
 * @version $Id$
 */
public abstract class AbstractWikiPageRevision implements WikiPageRevision
{

    protected String author;

    protected String comment;

    protected String version;

    protected boolean minorEdit;

    protected List<Attachment> attachmentList;

    protected XDOM textContent;

    /**
     * @param author
     * @param comment
     * @param version
     * @param minorEdit
     */
    public AbstractWikiPageRevision(String author, String comment, String version, boolean minorEdit)
    {
        super();
        this.author = author;
        this.comment = comment;
        this.version = version;
        this.minorEdit = minorEdit;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getAttachments()
     */
    public List<Attachment> getAttachments()
    {
        return attachmentList;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getAuthor()
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getComment()
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getContent()
     */
    public XDOM getContent()
    {
        return textContent;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getVersion()
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#isMinorEdit()
     */
    public boolean isMinorEdit()
    {
        return minorEdit;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version)
    {
        this.version = version;
    }

    /**
     * @param minorEdit the minorEdit to set
     */
    public void setMinorEdit(boolean minorEdit)
    {
        this.minorEdit = minorEdit;
    }

    /**
     * @param textContent the textContent to set
     */
    public void setTextContent(XDOM textContent)
    {
        this.textContent = textContent;
    }

    public void addAttachment(Attachment attachment)
    {
        this.attachmentList.add(attachment);
    }
}
