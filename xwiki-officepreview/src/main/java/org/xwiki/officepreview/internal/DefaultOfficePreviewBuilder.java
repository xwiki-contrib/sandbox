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
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.xwiki.bridge.AttachmentName;
import org.xwiki.bridge.DocumentName;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.officeimporter.builder.XDOMOfficeDocumentBuilder;
import org.xwiki.officeimporter.document.XDOMOfficeDocument;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.URLImage;

/**
 * Default implementation of {@link OfficePreviewBuilder}.
 * 
 * @version $Id$
 */
@Component
public class DefaultOfficePreviewBuilder extends AbstractOfficePreviewBuilder
{        
    /**
     * Used to build xdom documents from office documents.
     */
    @Requirement
    private XDOMOfficeDocumentBuilder xdomOfficeDocumentBuilder;    
    
    /**
     * {@inheritDoc}
     */
    protected OfficeDocumentPreview build(AttachmentName attachmentName, String attachmentVersion,
        InputStream attachmentStream) throws Exception
    {
        byte [] officeFileData = IOUtils.toByteArray(attachmentStream);
        DocumentName reference = attachmentName.getDocumentName();
        
        XDOMOfficeDocument xdomOfficeDoc = xdomOfficeDocumentBuilder.build(officeFileData, reference, true);
        
        XDOM xdom = xdomOfficeDoc.getContentDocument();
        Map<String, byte[]> artifacts = xdomOfficeDoc.getArtifacts();
        Set<File> tempFiles = new HashSet<File>();

        // Process all image blocks.
        List<ImageBlock> imgBlocks = xdom.getChildrenByType(ImageBlock.class, true);
        for (ImageBlock imgBlock : imgBlocks) {
            String imageName = imgBlock.getImage().getName();

            // Check whether there is a corresponding artifact.
            if (artifacts.containsKey(imageName)) {             
                try {
                    // Write the image into a temporary file.
                    File tempFile = writeArtifact(attachmentName, imageName, artifacts.get(imageName));

                    // Build a URLImage which links to above temporary image file.
                    URLImage urlImage = new URLImage(getURL(attachmentName, tempFile.getName()));

                    // Replace the old image block with new one backed by the URLImage.
                    Block newImgBlock = new ImageBlock(urlImage, false, imgBlock.getParameters());
                    imgBlock.getParent().replaceChild(Arrays.asList(newImgBlock), imgBlock);

                    // Collect the temporary file so that it can be cleaned up when the preview is disposed.
                    tempFiles.add(tempFile);
                } catch (Exception ex) {
                    String message = "Error while processing artifact image [%s].";
                    getLogger().error(String.format(message, imageName), ex);
                }
            }
        }

        return new OfficeDocumentPreview(attachmentName, attachmentVersion, xdom, tempFiles);
    }   
}