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
package org.xwiki.officepreview.internal;

import org.xwiki.model.reference.AttachmentReference;

/**
 * This interface is used to separate out the dependency on xwiki-core module so that it is easy to use mock objects for
 * testing {@link OfficePreviewBuilder} components based on {@link AbstractOfficePreviewBuilder} class.
 * 
 * @version $Id$
 */
public interface AttachmentVersionProvider
{
    /**
     * Queries the current version of an attachment.
     * 
     * @param attachmentReference reference to an attachment.
     * @return current version of the attachment.
     * @throws Exception if an error occurs while accessing attachment details.
     */
    String getAttachmentVersion(AttachmentReference attachmentReference) throws Exception;
}
