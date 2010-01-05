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
package org.xwiki.wikiimporter;

import java.util.Map;

import org.xml.sax.InputSource;

/**
 * Contains callback events called when a document to be imported has been parsed.
 * 
 * @version $Id$
 */
public interface WikiImporterListener
{

    /**
     * Call back for Start of a Wiki page.
     * 
     * @param pageName the page name.
     * @param params the parameters of page
     */
    void beginWikiPage(String pageName, Map<String, String> params);

    /**
     *Call back for  Start of Wiki page revision
     * 
     * @param pageName the page name
     * @param revision number/id of revision
     * @param params parameters of revision
     */
    void beginWikiPageRevision(String pageName, int revision, Map<String, String> params);

    /**
     * Call back for Start of an Object.
     * 
     * @param objectType type of Object
     * @param params parameters of the object
     */
    void beginObject(String objectType, Map<String, String> params);

    /**
     * Called when parser comes across a property associated to page or revision.
     * 
     * @param property name of the property.
     * @param params parameters associated with the property
     * @param value value of the property.
     */
    void onProperty(String property, Map<String, String> params, String value);

    /**
     *Call back for End of an Object.
     * 
     * @param objectType type of Object
     * @param params parameters of the object
     */
    void endObject(String objectType, Map<String, String> params);

    /**
     * Call back for End of Wiki page revision
     * 
     * @param pageName the page name
     * @param revision number/id of revision
     * @param params parameters of revision
     */
    void endWikiPageRevision(String pageName, int revision, Map<String, String> params);

    /**
     * Call back for End of a Wiki page.
     * 
     * @param pageName the page name.
     * @param params the parameters of page
     */
    void endWikiPage(String pageName, Map<String, String> params);

    /**
     * Call back for Start of Attachment
     * 
     * @param attachment name of the attachment
     * @param params parameters associated with the attachment
     */
    void beginAttachment(String attachmentName, Map<String, String> params);

    /**
     * Called when parser comes across a attachment.
     * 
     * @param attachmentName name of the attachment
     * @param params parameters associated with the attachment
     * @param input Attachment data as {@link InputSource}
     */
    void onAttachmentRevision(String attachmentName, Map<String, String> params, InputSource input);

    /**
     * Call back for End of Attachment
     * 
     * @param attachment name of the attachment
     * @param params parameters associated with the attachment
     */
    void endAttachment(String attachmentName, Map<String, String> params);

}
