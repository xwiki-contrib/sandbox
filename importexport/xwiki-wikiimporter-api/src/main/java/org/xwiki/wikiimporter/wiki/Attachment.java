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

/**
 * Attachment interface is used to handle attachments during import process by Wiki Importer.
 * @version $Id$
 */
public interface Attachment {

	/**
	 * @return the name of attachment file.
	 */
	public String getFileName();
	
	/**
	 * @return the content of attachment in bytes
	 */
	public byte[] getContent();
	
	/**
	 * @return the MIME type of attachment.
 	 */
	public String getMimeType();
	
	/**
	 * 
	 * @return the file size of attachment.
	 */
	public int getFileSize();

	/**
	 * 
	 * @return the author of attachment.
	 */
	public String getAuthor();
}
