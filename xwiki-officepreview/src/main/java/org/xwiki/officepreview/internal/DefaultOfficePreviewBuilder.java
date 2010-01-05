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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.xwiki.bridge.AttachmentName;
import org.xwiki.bridge.AttachmentNameSerializer;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentName;
import org.xwiki.bridge.DocumentNameSerializer;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.container.Container;
import org.xwiki.context.Execution;
import org.xwiki.officeimporter.builder.PresentationBuilder;
import org.xwiki.officeimporter.builder.XDOMOfficeDocumentBuilder;
import org.xwiki.officeimporter.document.XDOMOfficeDocument;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager.ManagerState;
import org.xwiki.officepreview.OfficePreviewBuilder;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.URLImage;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Default implementation of {@link OfficePreviewBuilder}.
 * 
 * @version $Id$
 */
@Component
public class DefaultOfficePreviewBuilder extends AbstractLogEnabled implements OfficePreviewBuilder, Initializable
{
    /**
     * File extensions corresponding to slide presentations.
     */
    private static final List<String> PRESENTATION_FORMAT_EXTENSIONS = Arrays.asList("ppt", "pptx", "odp");

    /**
     * Used to access the temporary directory.
     */
    @Requirement
    private Container container;

    /**
     * Reference to current execution.
     */
    @Requirement
    private Execution execution;

    /**
     * Used to access attachment content.
     */
    @Requirement
    private DocumentAccessBridge docBridge;

    /**
     * Used for serializing {@link DocumentName} instances into strings.
     */
    @Requirement
    private DocumentNameSerializer documentNameSerializer;

    /**
     * Used for serializing {@link AttachmentName} instances into strings.
     */
    @Requirement
    private AttachmentNameSerializer attachmentNameSerializer;

    /**
     * Used to query openoffice server state.
     */
    @Requirement
    private OpenOfficeManager officeManager;

    /**
     * Used to build xdom documents from office documents.
     */
    @Requirement
    private XDOMOfficeDocumentBuilder xdomOfficeDocumentBuilder;

    /**
     * Used to build xdom presentations from office slide shows.
     */
    @Requirement
    private PresentationBuilder presentationBuilder;

    /**
     * Used to initialize the previews cache.
     */
    @Requirement
    private CacheManager cacheManager;

    /**
     * Office document previews cache.
     */
    private Cache<OfficeDocumentPreview> previewsCache;

    /**
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {
        CacheConfiguration config = new CacheConfiguration();
        LRUEvictionConfiguration lec = new LRUEvictionConfiguration();

        // TODO: Make this configurable.
        lec.setMaxEntries(10);

        config.put(LRUEvictionConfiguration.CONFIGURATIONID, lec);
        try {
            previewsCache = cacheManager.createNewCache(config);
        } catch (CacheException ex) {
            throw new InitializationException("Error while initializing previews cache.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public XDOM build(AttachmentName attachmentName) throws Exception
    {
        String strAttachmentName = attachmentNameSerializer.serialize(attachmentName);
        DocumentName reference = attachmentName.getDocumentName();

        // Search the cache.
        OfficeDocumentPreview preview = previewsCache.get(strAttachmentName);

        // It's possible that the attachment has been deleted. We need to catch such events and cleanup the cache.
        if (!docBridge.getAttachments(reference).contains(attachmentName)) {
            // If a cached preview exists, flush it.
            if (null != preview) {
                previewsCache.remove(strAttachmentName);
                preview.dispose();
            }
            throw new Exception(String.format("Attachment [%s] does not exist.", strAttachmentName));
        }

        // Query the current version of the attachment.
        String currentVersion = getAttachmentVersion(attachmentName);

        // Check if the preview has been expired.
        if (null != preview && !currentVersion.equals(preview.getAttachmentVersion())) {
            // Flush the cached preview.
            previewsCache.remove(strAttachmentName);
            preview.dispose();
            preview = null;
        }

        // If a preview in not available, build one.
        if (null == preview) {
            // Make sure an openoffice server is available.
            connect();

            // Build preview.
            InputStream officeFileStream = docBridge.getAttachmentContent(attachmentName);
            byte[] officeFileData = IOUtils.toByteArray(officeFileStream);
            XDOMOfficeDocument xdomOfficeDoc;
            if (isPresentation(strAttachmentName)) {
                xdomOfficeDoc = presentationBuilder.build(officeFileData);
            } else {
                xdomOfficeDoc = xdomOfficeDocumentBuilder.build(officeFileData, reference, true);
            }
            preview = build(attachmentName, currentVersion, xdomOfficeDoc);

            // Cache the preview.
            previewsCache.set(strAttachmentName, preview);
        }

        // Done.
        return preview.getXdom();
    }

    /**
     * Builds an {@link OfficeDocumentPreview} instance corresponding to the given {@link XDOMOfficeDocument}.
     * 
     * @param attachmentName name of the attachment being previewed.
     * @param attachmentVersion current version of the attachment.
     * @param xdomOfficeDoc {@link XDOMOfficeDocument} corresponding to the attachment.
     * @return {@link OfficeDocumentPreview} corresponding to the specified attachment.
     * @throws Exception if an error occurs while building the preview.
     */
    private OfficeDocumentPreview build(AttachmentName attachmentName, String attachmentVersion,
        XDOMOfficeDocument xdomOfficeDoc)
    {
        XDOM xdom = xdomOfficeDoc.getContentDocument();
        Map<String, byte[]> artifacts = xdomOfficeDoc.getArtifacts();
        Set<File> tempFiles = new HashSet<File>();

        // Process all image blocks.
        List<ImageBlock> imgBlocks = xdom.getChildrenByType(ImageBlock.class, true);
        for (ImageBlock imgBlock : imgBlocks) {
            String imageName = imgBlock.getImage().getName();

            // Check whether there is a corresponding artifact.
            if (artifacts.containsKey(imageName)) {
                // Prepare a suitable file for holding the temporary image.
                String extension = imageName.substring(imageName.lastIndexOf('.') + 1);
                String tempFileName = String.format("%s.%s", UUID.randomUUID().toString(), extension);
                File tempFile = new File(getTempDir(attachmentName), tempFileName);
                
                FileOutputStream fos = null;
                try {
                    // Write the temporary image file.
                    fos = new FileOutputStream(tempFile);
                    IOUtils.write(artifacts.get(imageName), fos);
                    
                    // Build a URLImage which links to the temporary image file.
                    URLImage urlImage = new URLImage(getURL(attachmentName, tempFileName));
                    
                    // Replace the old image block with new one backed by URLImage.
                    Block newImgBlock = new ImageBlock(urlImage, false, imgBlock.getParameters());                                        
                    imgBlock.getParent().replaceChild(Arrays.asList(newImgBlock), imgBlock);
                    
                    // Collect the temporary file so that it can be cleaned up when the preview is disposed.
                    tempFiles.add(tempFile);
                } catch (IOException ex) {
                    String message = "Error while writing temporary image file [%s].";
                    getLogger().error(String.format(message, tempFileName), ex);
                } finally {
                    IOUtils.closeQuietly(fos);
                }
            }
        }

        return new OfficeDocumentPreview(attachmentName, attachmentVersion, xdom, tempFiles);
    }

    /**
     * @return directory used to hold temporary files belonging to the office preview of the specified attachment.
     */
    private File getTempDir(AttachmentName attachmentName)
    {
        // TODO: For the moment we're using the charting directory to store office preview images. We need to change
        // this when a generic temporary-resource action becomes available.
        File tempDir = container.getApplicationContext().getTemporaryDirectory();
        return new File(tempDir, "charts");
    }

    /**
     * Utility method for building a URL for the specified temporary file belonging to the office preview of the given
     * attachment.
     * 
     * @param attachmentName name of the attachment being previewed.
     * @param fileName name of the temporary file.
     * @return URL string that refers the specified temporary file.
     */
    private String getURL(AttachmentName attachmentName, String fileName)
    {
        // TODO: Since we are using the charting directory for holding temporary image files, here also we have to use
        // the charting action so that we have valid URL references. This needs to be updated when a temporary-resource
        // action becomes available.
        XWikiContext xcontext = getContext();
        try {
            return xcontext.getWiki().getExternalURL(null, "charting", xcontext) + "/" + fileName;
        } catch (Exception ex) {
            getLogger().error("Unexpected error.", ex);
        }
        return null;
    }

    /**
     * Attempts to connect to an openoffice server for conversions.
     * 
     * @throws Exception if an openoffice server is not available.
     */
    private void connect() throws Exception
    {
        if (!officeManager.getState().equals(ManagerState.CONNECTED)) {
            throw new Exception("OpenOffice server unavailable.");
        }
    }

    /**
     * Utility method for checking if a file name corresponds to an office presentation.
     * 
     * @param officeFileName office file name.
     * @return true if the file name / extension represents an office presentation format.
     */
    private boolean isPresentation(String officeFileName)
    {
        String extension = officeFileName.substring(officeFileName.lastIndexOf('.') + 1);
        return PRESENTATION_FORMAT_EXTENSIONS.contains(extension);
    }

    /**
     * Utility method for finding the current version of the specified attachment.
     * 
     * @param attachmentName name of the office attachment.
     * @return current version of the specified attachment.
     * @throws Exception if an error occurs while accessing attachment details.
     */
    private String getAttachmentVersion(AttachmentName attachmentName) throws Exception
    {
        XWikiContext xcontext = getContext();
        String documentName = documentNameSerializer.serialize(attachmentName.getDocumentName());
        XWikiDocument doc = xcontext.getWiki().getDocument(documentName, xcontext);
        XWikiAttachment attach = doc.getAttachment(attachmentName.getFileName());
        return attach.getVersion();
    }

    /**
     * @return {@link XWikiContext} instance.
     */
    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }
}
