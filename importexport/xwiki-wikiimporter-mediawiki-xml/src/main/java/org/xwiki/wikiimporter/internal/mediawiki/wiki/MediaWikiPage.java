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
package org.xwiki.wikiimporter.internal.mediawiki.wiki;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xwiki.wikiimporter.internal.mediawiki.MediaWikiConstants;
import org.xwiki.wikiimporter.wiki.AbstractWikiPage;
import org.xwiki.wikiimporter.wiki.Attachment;

/**
 * This class represents a MediaWiki Page.
 * 
 * @version $Id$
 */
public class MediaWikiPage extends AbstractWikiPage
{
    /**
     * The orginal page title present in the exported document. Useful for reference purpose while generating page
     * import log.
     */
    private String pageTitleForReference;

    public MediaWikiPage(String title, String defaultSpace, String wiki)
    {
        super(title, defaultSpace, wiki);
    }

    public MediaWikiPage(String defaultSpace, String wiki)
    {
        this(null, defaultSpace, wiki);
    }

    /**
     * Populate page properties.
     * 
     * @param pageProps properties of the page like page title.
     */
    public void populateProps(Map<String, String> pageProps)
    {
        setTitle(pageProps.get(MediaWikiConstants.PAGE_TITLE_TAG));
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "MediaWiki Page :" + pageTitleForReference;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getTitle()
     */
    public String getTitle()
    {
        return title;
    }

    @Override
    public void setTitle(String title)
    {
        super.setTitle(title);
        this.pageTitleForReference = title;
    }

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
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getName()
     */
    public String getName()
    {
        String tmpName = cleanName(title, "[^a-zA-Z0-9:/# ]", null);

        tmpName = cleanEdges(tmpName, ':');
        tmpName = cleanEdges(tmpName, '/');
        // Return null if the page name is blank.
        if (StringUtils.isBlank(tmpName)) {
            return null;
        }

        // Clean if title has a space declared.
        if (tmpName.indexOf(':') > 0 && tmpName.indexOf(':') < tmpName.length()) {
            tmpName = tmpName.substring(tmpName.indexOf(':') + 1);
        }

        // Check for hierarchy if any
        if (-1 != tmpName.indexOf('/')) {
            int lastIndex = tmpName.lastIndexOf('/');
            String parentStr = tmpName.substring(0, lastIndex);
            if (-1 != parentStr.indexOf('/')) {
                parentStr = parentStr.substring(parentStr.lastIndexOf('/') + 1);
            }

            tmpName = tmpName.substring(lastIndex + 1);
            this.parent = parentStr;
        }

        this.title = tmpName;
        this.pageName = tmpName.replaceAll("[ ]", "");

        return this.pageName;
    }

    /**
     * In MediaWiki the page hierarchy is of format Parent/Child. Hierarchy is maintained with page name separated with
     * forward slash ("/")
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getParent()
     */
    public String getParent()
    {
        return cleanName(parent, "[^a-zA-Z0-9]", "WebHome");
    }

    /**
     * Space is extracted from title.MediaWiki pages have SPACE:PAGENAME format as their title. If it is just PAGENAME,
     * then SPACE will be the default space.
     * 
     * @return space corresponding to this mediawiki page.
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getSpace()
     */
    public String getSpace()
    {
        String tmpSpace = cleanName(title, "[^a-z0-9A-Z:]", space);

        // Extract Space from title.
        if (tmpSpace.indexOf(':') > 0 && tmpSpace.indexOf(':') < tmpSpace.length() - 1) {
            this.space = tmpSpace.substring(0, tmpSpace.indexOf(':'));
            return this.space;
        } else {
            // TODO Handle Error with error log.
        }
        return space;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.wiki.WikiPage#getWiki()
     */
    public String getWiki()
    {
        return wiki;
    }

    /**
     * Cleans the given string as per the regex, if string is blank/null return the default value.
     * 
     * @param name Name to be cleaned
     * @param regex regex on which the sting to be cleaned. For eg: [^a-z0-9A-Z] All non alpha-numeric.
     * @param defaultName Name to return if the given string is null or blank.
     * @return the cleaned string or default name.
     */
    private String cleanName(String name, String regex, String defaultName)
    {
        if (StringUtils.isNotBlank(name)) {
            name = name.replaceAll(regex, "").trim();
            return name.trim();
        }
        return defaultName;
    }

    /**
     * Removes the given character if present at starting and ending of the string.
     */
    private String cleanEdges(String name, char c)
    {
        if (StringUtils.isNotBlank(name)) {
            if (0 == name.indexOf(c)) {
                name = name.substring(1);
            }

            if (name.length() - 1 == name.indexOf(c)) {
                name = name.substring(0, name.length() - 1);
            }

            if (StringUtils.isNotBlank(name)) {
                return name;
            }
        }

        return null;
    }

    /**
     * @return the orginal page title.
     */
    public String getPageTitleForReference()
    {
        return pageTitleForReference;
    }
}
