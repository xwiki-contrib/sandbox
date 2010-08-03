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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Id$
 */
public abstract class AbstractWikiPage implements WikiPage
{

    protected String title;

    protected LinkedList<WikiPageRevision> pageRevisionList = new LinkedList<WikiPageRevision>();

    protected LinkedList<Attachment> attachmentList = new LinkedList<Attachment>();

    protected List<String> children = new ArrayList<String>();

    protected String parent;

    protected String space;

    protected String pageName;

    protected String wiki;

    protected Set<String> tags = new TreeSet<String>();

    public AbstractWikiPage(String title, String defaultSpace, String wiki)
    {
        this.title = title;
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
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getChildren()
     */
    public List<String> getChildren()
    {
        // TODO Auto-generated method stub
        return null;
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
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getProperties()
     */
    public Properties getProperties()
    {
        // TODO Auto-generated method stub
        return null;
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

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getTags()
     */
    public Set<String> getTags()
    {
        // TODO Auto-generated method stub
        return tags;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getTagsAsString()
     */
    public String getTagsAsString()
    {
        StringBuilder tagString = new StringBuilder();
        for (String tag : tags) {
            tagString.append(tag + "|");
        }
        return tagString.toString();
    }

    public void setTitle(String title)
    {
        this.title = title;
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

    public void addTag(String tag)
    {
        tags.add(tag);
    }

    public void addAttachment(Attachment attachment)
    {
        attachmentList.add(attachment);
    }

}
