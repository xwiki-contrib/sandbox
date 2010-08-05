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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xwiki.rendering.block.XDOM;

/**
 * @version $Id$
 */
public abstract class AbstractWikiPageRevision implements WikiPageRevision
{
    protected String title;

    protected String parent;

    protected String author;

    protected String comment;

    protected String version;

    protected boolean minorEdit;

    protected List<Attachment> attachmentList;

    protected Set<String> tags = new TreeSet<String>();

    protected XDOM content;

    public AbstractWikiPageRevision()
    {
    }

    public AbstractWikiPageRevision(WikiPageRevision previousWikiPageRevision)
    {
        setTitle(previousWikiPageRevision.getTitle());
        setParent(previousWikiPageRevision.getParent());
        setAuthor(previousWikiPageRevision.getAuthor());
        setComment(previousWikiPageRevision.getComment());
        setVersion(previousWikiPageRevision.getVersion());
        setMinorEdit(previousWikiPageRevision.isMinorEdit());
        setTags(previousWikiPageRevision.getTags());
        setContent(previousWikiPageRevision.getContent());
    }

    public AbstractWikiPageRevision(String title, String author, String comment, String version, boolean minorEdit)
    {
        this.title = title;
        this.author = author;
        this.comment = comment;
        this.version = version;
        this.minorEdit = minorEdit;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getTitle()
     */
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getParent()
     */
    public String getParent()
    {
        return this.parent;
    }

    public void setParent(String parent)
    {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getAttachments()
     */
    public List<Attachment> getAttachments()
    {
        return this.attachmentList;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getAuthor()
     */
    public String getAuthor()
    {
        return this.author;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getComment()
     */
    public String getComment()
    {
        return this.comment;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getContent()
     */
    public XDOM getContent()
    {
        return this.content;
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
        return this.minorEdit;
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
    public void setContent(XDOM textContent)
    {
        this.content = textContent;
    }

    public void addAttachment(Attachment attachment)
    {
        this.attachmentList.add(attachment);
    }

    public Set<String> getTags()
    {
        return this.tags;
    }

    public void setTags(Set<String> tags)
    {
        this.tags = new LinkedHashSet<String>(tags);
    }

    public void addTag(String tag)
    {
        this.tags.add(tag);
    }
}
