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

import org.xwiki.wikiimporter.internal.mediawiki.MediaWikiConstants;
import org.xwiki.wikiimporter.wiki.AbstractWikiPageRevision;

/**
 * This class represents MediaWiki page revision.
 * 
 * @version $Id$
 */
public class MediaWikiPageRevision extends AbstractWikiPageRevision
{

    public MediaWikiPageRevision(String author, String comment, String version, boolean minorEdit)
    {
        super(author, comment, version, minorEdit);
    }

    public MediaWikiPageRevision(Map<String, String> pageRevProps)
    {

        this(pageRevProps.get(MediaWikiConstants.AUTHOR_TAG), pageRevProps.get(MediaWikiConstants.COMMENT_TAG),
            pageRevProps.get(MediaWikiConstants.VERSION_TAG), "true".equals(pageRevProps
                .get(MediaWikiConstants.IS_MINOR_TAG)) ? true : false);
    }

}
