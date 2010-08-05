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
package org.xwiki.wikiimporter.listener;

import org.xml.sax.InputSource;
import org.xwiki.rendering.listener.Listener;

/**
 * Contains callback events called when a document to be imported has been parsed.
 * 
 * @version $Id$
 */
public interface WikiImporterListener extends Listener
{
    /**
     * Call back for Start of a Wiki page.
     */
    void beginWikiPage();

    /**
     * Call back for Start of Wiki page revision
     */
    void beginWikiPageRevision();

    /**
     * Call back for Start of an Object.
     * 
     * @param objectType type of Object
     */
    void beginObject(String objectType);

    /**
     * Called when parser comes across a property associated to page or revision.
     * 
     * @param property name of the property.
     * @param value value of the property.
     */
    void onProperty(String property, String value);

    /**
     * Call back for End of an Object.
     * 
     * @param objectType type of Object
     * @param params parameters of the object
     */
    void endObject(String objectType);

    /**
     * Call back for End of Wiki page revision
     */
    void endWikiPageRevision();

    /**
     * Call back for End of a Wiki page.
     */
    void endWikiPage();

    /**
     * Call back for Start of Attachment
     * 
     * @param attachment name of the attachment
     */
    void beginAttachment(String attachmentName);

    /**
     * Called when parser comes across a attachment.
     * 
     * @param attachmentName name of the attachment
     * @param input Attachment data as {@link InputSource}
     */
    void onAttachmentRevision(String attachmentName, InputSource input);

    /**
     * Call back for End of Attachment
     */
    void endAttachment();

}
