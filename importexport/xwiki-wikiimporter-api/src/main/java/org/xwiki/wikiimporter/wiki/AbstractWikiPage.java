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
import java.util.Properties;

/**
 * 
 * @version $Id$
 */
public abstract class AbstractWikiPage implements WikiPage
{

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getAttachment(java.lang.String)
     */
    public Attachment getAttachment(String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getAttachments()
     */
    public List<Attachment> getAttachments()
    {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getName()
     */
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getParent()
     */
    public String getParent()
    {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getSpace()
     */
    public String getSpace()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getTags()
     */
    public List<String> getTags()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getTitle()
     */
    public String getTitle()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
