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

import java.io.File;
import java.util.Set;

import org.xwiki.bridge.AttachmentName;
import org.xwiki.cache.DisposableCacheValue;
import org.xwiki.rendering.block.XDOM;

/**
 * Used for holding all the information belonging to an office attachment preview.
 * 
 * @version $Id$
 */
public class OfficeDocumentPreview implements DisposableCacheValue
{
    /**
     * Name of the attachment to which this preview belongs.
     */
    private AttachmentName attachmentName;

    /**
     * Specific version of the attachment to which this preview corresponds.
     */
    private String attachmentVersion;

    /**
     * {@link XDOM} holding the preview document syntax.
     */
    private XDOM xdom;

    /**
     * Temporary files used by this preview.
     */
    private Set<File> tempFiles;

    /**
     * Creates a new {@link OfficeDocumentPreview} instance.
     * 
     * @param attachmentName name of the attachment to which this preview belongs.
     * @param attachmentVersion version of the attachment to which this preview corresponds.
     * @param xdom {@link XDOM} holding the preview document syntax.
     * @param tempFiles temporary files that belongs to this preview.
     */
    public OfficeDocumentPreview(AttachmentName attachmentName, String attachmentVersion, XDOM xdom,
        Set<File> tempFiles)
    {
        this.attachmentName = attachmentName;
        this.attachmentVersion = attachmentVersion;
        this.xdom = xdom;
        this.tempFiles = tempFiles;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() throws Exception
    {
        // Cleanup all the temporary files.
        for (File tempFile : tempFiles) {
            tempFile.delete();
        }
    }

    /**
     * @return name of the attachment to which this preview belongs.
     */
    public AttachmentName getAttachmentName()
    {
        return this.attachmentName;
    }

    /**
     * @return version of the attachment to which this preview corresponds.
     */
    public String getAttachmentVersion()
    {
        return this.attachmentVersion;
    }

    /**
     * @return {@link XDOM} holding the preview document syntax.
     */
    public XDOM getXdom()
    {
        return this.xdom;
    }
}
