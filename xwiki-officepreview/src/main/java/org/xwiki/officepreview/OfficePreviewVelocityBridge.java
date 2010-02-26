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

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.logging.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

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
     * Used for constructing {@link AttachmentReference} instances from strings.
     */
    private AttachmentReferenceResolver<String> attachRefResolver;

    /**
     * For building previews of non-presentation office documents.
     */
    private OfficePreviewBuilder defaultOfficePreviewBuilder;

    /**
     * For building previews of presentation office documents.
     */
    private OfficePreviewBuilder presentationOfficePreviewBuilder;

    /**
     * Used to query current document syntax.
     */
    private DocumentAccessBridge docBridge;

    /**
     * Constructs a new bridge instance.
     * 
     * @param componentManager used to lookup for other required components.
     * @param logger for logging support.
     * @throws Exception if an error occurs while initializing bridge.
     */
    @SuppressWarnings("unchecked")
    public OfficePreviewVelocityBridge(ComponentManager componentManager, Logger logger) throws Exception
    {
        // Base components.
        this.componentManager = componentManager;
        this.logger = logger;

        // Lookup other required components.
        this.execution = componentManager.lookup(Execution.class);
        this.attachRefResolver = componentManager.lookup(AttachmentReferenceResolver.class);
        this.defaultOfficePreviewBuilder = componentManager.lookup(OfficePreviewBuilder.class);
        this.presentationOfficePreviewBuilder = componentManager.lookup(OfficePreviewBuilder.class, "presentation");
        this.docBridge = componentManager.lookup(DocumentAccessBridge.class);
    }

    /**
     * Builds a preview of the specified office attachment and renders the result in default document syntax.
     * 
     * @param attachmentNameString string identifying the office attachment.
     * @return preview of the specified office attachment rendered in default document syntax or null if an error
     *         occurs.
     */
    public String preview(String attachmentNameString)
    {
        return preview(attachmentNameString, null);
    }

    /**
     * Builds a preview of the specified office attachment and renders the result in specified syntax.
     * 
     * @param attachmentNameString string identifying the office attachment.
     * @param outputSyntaxId output syntax identifier or null if default document syntax should be used.
     * @return preview of the specified office attachment rendered in specified output syntax (defaulting to document
     *         syntax if output syntax is not specified) or null if an error occurs.
     */
    public String preview(String attachmentNameString, String outputSyntaxId)
    {
        AttachmentReference attachRef = attachRefResolver.resolve(attachmentNameString);
        try {
            return preview(attachRef, outputSyntaxId);
        } catch (Exception ex) {
            String message = "Could not preview office document [%s] - %s";
            message = String.format(message, attachmentNameString, ex.getMessage());
            setErrorMessage(message);
            logger.error(message, ex);
        }
        return null;
    }

    /**
     * Builds a preview of the specified office attachment and renders the result in specified syntax.
     * 
     * @param attachRef reference to the attachment to be previewed.
     * @param outputSyntaxId output syntax identifier or null if default document syntax should be used.
     * @return preview of the specified office attachment rendered in specified output syntax or default document
     *         syntax.
     * @throws Exception if current user does not have enough privileges to view the requested attachment or if an error
     *             occurs while generating the preview.
     */
    private String preview(AttachmentReference attachRef, String outputSyntaxId) throws Exception
    {
        DocumentReference docRef = attachRef.getDocumentReference();

        // Check whether current user has view rights on the document containing the attachment.
        if (!docBridge.isDocumentViewable(docRef)) {
            throw new Exception("Inadequate privileges.");
        }

        // If output syntax is not specified, use the default document syntax.
        String syntaxId = (outputSyntaxId == null) ? docBridge.getDocument(docRef).getSyntaxId() : outputSyntaxId;

        XDOM preview;
        if (isPresentation(attachRef.getName())) {
            preview = presentationOfficePreviewBuilder.build(attachRef);
        } else {
            preview = defaultOfficePreviewBuilder.build(attachRef);
        }

        // Build the preview and render the result.
        return render(preview, syntaxId);
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
}
