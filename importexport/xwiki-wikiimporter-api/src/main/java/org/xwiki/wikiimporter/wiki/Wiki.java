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
 * Wiki interface represents a Abstract Wiki in WikiImporter.
 * 
 * @version $Id$
 */
public interface Wiki {

	/**
	 *@return the list of Wiki pages.
	 */
	public List<WikiPage> getWikiPages();
	
	/**
	 *@return list of names of the spaces.
	 */
	public List<String> getSpaceNames();
	
	/**
	 *@param space name.
	 *@return corresponding list of pages.
	 */
    public List<String> getPageNames(String space);
    
    /**
     *@param space name.
     *@param page name.
     *@return the corresponding Wiki Page.
     */
    public WikiPage getWikiPage(String space,String page);
 }
