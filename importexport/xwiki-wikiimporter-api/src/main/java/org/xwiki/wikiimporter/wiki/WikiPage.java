/*
hi 
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
 * WikiPage Interface represents a Page in Wiki Importer.
 * 
 * @version $Id$
 */
public interface WikiPage {

	/**
	 *@return title of the Page.
	 */
	public String getTitle();
	
	/**
	 *@return name of the Page.
	 */
	public String getName();
	
	/**
	 *@return name of the Space to which the Page corresponds to.
	 */
	public String getSpace();
	
	/**
	 *@return properties of the Page.
	 */
	public Properties getProperties();
	
	/**
	 *@return list of revisions of Page.
	 */
	public List<WikiPageRevision> getRevisions();
	
	/**
	 *@return list of attachments of the Page.
	 */
	public List<Attachment> getAttachments();
	
	/**
	 *@return last revision of the Page.
	 */
	public WikiPageRevision getLastRevision();
	
	/**
	 *@param fileName name of the Attachment.
	 *@return attachment of the Page. 
	 */
	public Attachment getAttachment(String fileName);
	
	/**
	 *@return the list of names of child pages associated with the Page.
	 */
	public List<String> getChildren();
	
	/**
	 *@return the list of tags associated with the Page.
	 */
	public List<String> getTags();
	
	/**
	 *@return the name of parent page.
	 */
	public String getParent();
}
