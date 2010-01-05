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

/**
 * 
 * @version $Id$
 */
public abstract class AbstractWikiPageRevision implements WikiPageRevision
{

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getAttachments()
     */
    public List<Attachment> getAttachments()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getAuthor()
     */
    public String getAuthor()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getComment()
     */
    public String getComment()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getContent()
     */
    public String getContent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#getVersion()
     */
    public String getVersion()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPageRevision#isMinorEdit()
     */
    public boolean isMinorEdit()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
