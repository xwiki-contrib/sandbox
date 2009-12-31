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
package org.xwiki.officepreview;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.xwiki.bridge.AttachmentName;
import org.xwiki.bridge.AttachmentNameFactory;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentName;
import org.xwiki.bridge.DocumentNameSerializer;
import org.xwiki.cache.Cache;
import org.xwiki.component.logging.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.officeimporter.OfficeImporterException;
import org.xwiki.officeimporter.builder.PresentationBuilder;
import org.xwiki.officeimporter.builder.XDOMOfficeDocumentBuilder;
import org.xwiki.officeimporter.document.XDOMOfficeDocument;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager.ManagerState;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Exposes office preview utility methods to velocity scripts.
 * 
 * @version $Id$
 */
public class OfficePreviewVelocityBridge
{
    /**
     * File extensions corresponding to slide presentations.
     */
    private static final List<String> PRESENTATION_FORMAT_EXTENSIONS = Arrays.asList("ppt", "pptx", "odp");

    /**
     * The key used to place any error messages while previewing office documents.
     */
    private static final String OFFICE_PREVIEW_ERROR = "OFFICE_PREVIEW_ERROR";

    /**
     * Used to lookup various renderer implementations.
     */
    private ComponentManager componentManager;

    /**
     * Logging support.
     */
    private Logger logger;

    /**
     * Reference to current execution.
     */
    private Execution execution;

    /**
     * Used to query openoffice server state.
     */
    private OpenOfficeManager officeManager;

    /**
     * Used to access attachment content.
     */
    private DocumentAccessBridge docBridge;

    /**
     * Office document previews cache.
     */
    private Cache<XDOM> previewsCache;

    /**
     * Used for serializing {@link DocumentName} instances into strings.
     */
    private DocumentNameSerializer documentNameSerializer;

    /**
     * Used to create {@link AttachmentName} instances from string formed attachment names.
     */
    private AttachmentNameFactory attachmentNameFactory;

    /**
     * Used to build xdom documents from office documents.
     */
    private XDOMOfficeDocumentBuilder xdomOfficeDocumentBuilder;

    /**
     * Used to build xdom presentations from office slide shows.
     */
    private PresentationBuilder presentationBuilder;

    /**
     * Constructs a new bridge instance.
     * 
     * @param componentManager used to lookup for other required components.
     * @param previewsCache cache of office attachment previews.
     * @param logger for logging support.
     * @throws Exception if an error occurs while initializing bridge.
     */
    public OfficePreviewVelocityBridge(ComponentManager componentManager, Cache<XDOM> previewsCache, Logger logger)
        throws Exception
    {
        // Base components.
        this.componentManager = componentManager;
        this.previewsCache = previewsCache;
        this.logger = logger;

        // Other dependent components.
        this.execution = componentManager.lookup(Execution.class);
        this.officeManager = componentManager.lookup(OpenOfficeManager.class);
        this.docBridge = componentManager.lookup(DocumentAccessBridge.class);
        this.documentNameSerializer = componentManager.lookup(DocumentNameSerializer.class);
        this.attachmentNameFactory = componentManager.lookup(AttachmentNameFactory.class);
        this.xdomOfficeDocumentBuilder = componentManager.lookup(XDOMOfficeDocumentBuilder.class);
        this.presentationBuilder = componentManager.lookup(PresentationBuilder.class);
    }

    /**
     * Builds a preview of the specified office attachment and renders the result in specified syntax.
     * 
     * @param attachmentNameString string identifying the office attachment.
     * @param outputSyntaxId output syntax identifier (e.g. xhtml/1.0).
     * @return preview of the specified office attachment rendered in output syntax or null if an error occurs.
     */
    public String preview(String attachmentNameString, String outputSyntaxId)
    {
        AttachmentName attachmentName = attachmentNameFactory.createAttachmentName(attachmentNameString);
        DocumentName reference = attachmentName.getDocumentName();                       

        try {
            // Formulate the preview cache key.
            String attachmentVersion = getAttachmentVersion(attachmentName);
            String previewKey = String.format("%s_%s", attachmentNameString, attachmentVersion);

            // Search the cache.
            XDOM preview = previewsCache.get(previewKey);

            // If a cached result is not available, build a preview.
            if (null == preview) {
                // Make sure an openoffice server is available.
                connect();
                
                // Build preview.
                InputStream officeFileStream = docBridge.getAttachmentContent(attachmentName);
                byte [] officeFileData = IOUtils.toByteArray(officeFileStream);
                XDOMOfficeDocument xdomOfficeDoc;
                if (isPresentation(attachmentNameString)) {
                    xdomOfficeDoc = presentationBuilder.build(officeFileData);
                } else {
                    xdomOfficeDoc = xdomOfficeDocumentBuilder.build(officeFileData, reference, true);
                }
                preview = buildPreview(xdomOfficeDoc, attachmentName);
                
                // Cache the preview.
                previewsCache.set(previewKey, preview);
            }
            
            // Done.
            return render(preview, outputSyntaxId);
        } catch (Exception ex) {
            String message = "Could not preview office document [%s] - [%s]";
            message = String.format(message, attachmentNameString, ex.getMessage());
            setErrorMessage(message);
            logger.error(message, ex);
        }

        return null;
    }

    /**
     * @return an error message set inside current execution or null.
     */
    public String getErrorMessage()
    {
        return (String) execution.getContext().getProperty(OFFICE_PREVIEW_ERROR);
    }

    /**
     * Utility method for setting an error message inside current execution.
     * 
     * @param message error message.
     */
    private void setErrorMessage(String message)
    {
        execution.getContext().setProperty(OFFICE_PREVIEW_ERROR, message);
    }

    /**
     * Attempts to connect to an openoffice server for conversions.
     * 
     * @throws Exception if an openoffice server is not available.
     */
    private void connect() throws Exception
    {
        if (!officeManager.getState().equals(ManagerState.CONNECTED)) {
            throw new OfficeImporterException("OpenOffice server unavailable.");
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
     * Prepares a preview {@link XDOM} from the given office document.
     * 
     * @param xdomOfficeDoc office document.
     * @param attachmentName name of the attachment which is to be previewed.
     * @return an {@link XDOM} holding a preview of the given office document.
     * @throws Exception if an error occurs while preparing the preview.
     */
    private XDOM buildPreview(XDOMOfficeDocument xdomOfficeDoc, AttachmentName attachmentName) throws Exception
    {        
        // Dummy implementation.
        return xdomOfficeDoc.getContentDocument();
    }

    /**
     * Renders the given block into specified syntax.
     * 
     * @param block {@link Block} to be rendered.
     * @param syntaxId expected output syntax.
     * @return string holding the result of rendering.
     * @throws Exception if an error occurs during rendering.
     */
    private String render(Block block, String syntaxId) throws Exception
    {
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, syntaxId);
        renderer.render(block, printer);
        return printer.toString();
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
        XWikiContext xcontext = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        String documentName = documentNameSerializer.serialize(attachmentName.getDocumentName());
        XWikiDocument doc = xcontext.getWiki().getDocument(documentName, xcontext);
        XWikiAttachment attach = doc.getAttachment(attachmentName.getFileName());
        return attach.getVersion();
    }        
}
