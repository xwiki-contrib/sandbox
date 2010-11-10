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
package org.xwiki.model;

import org.xwiki.rendering.syntax.Syntax;

import java.util.List;
import java.util.Locale;

public interface Document extends Entity
{
    Locale getLocale();

    // Q: Should we have instead: setContent(Content content) with Content encapsulating the syntax?
    Syntax getSyntax();
    void setSyntax(Syntax syntax);

    /**
     * @return the list of object definitions defined inside this document
     */
    List<ObjectDefinition> getObjectDefinitions();

    ObjectDefinition getObjectDefinition(String objectDefinitionName);

    ObjectDefinition addObjectDefinition(String objectDefinitionName);

    void removeObjectDefinition(String objectDefinitionName);

    List<Object> getObjects();

    Object getObject(String objectName);

    Object addObject(String objectName);

    void removeObject(String objectName);

    // Q: Could attachments be an Object?
    List<Attachment> getAttachments();

    Attachment getAttachment(String attachmentName);

    Attachment addAttachment(String attachmentName);

    void removeAttachment(String attachmentName);

    // Note: returning a XDOM is a problem because it would require Renderers for all syntaxes (for example).
    String getContent();
    void setContent(String content);

    boolean hasObject(String objectName);

    boolean hasObjectDefinition(String objectDefinitionName);

    boolean hasAttachment(String attachmentName);
}
