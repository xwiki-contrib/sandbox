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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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
import org.xwiki.context.Execution;
import org.xwiki.officeimporter.builder.PresentationBuilder;
import org.xwiki.officeimporter.builder.XDOMOfficeDocumentBuilder;
import org.xwiki.officeimporter.document.XDOMOfficeDocument;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager.ManagerState;
import org.xwiki.officepreview.OfficePreviewBuilder;
import org.xwiki.rendering.block.XDOM;

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
    private Cache<XDOM> previewsCache;

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
        DocumentName reference = attachmentName.getDocumentName();
        String strAttachmentName = attachmentNameSerializer.serialize(attachmentName);

        // Formulate the preview cache key.
        String previewKey = String.format("%s_%s", strAttachmentName, getAttachmentVersion(attachmentName));

        // Search the cache.
        XDOM preview = previewsCache.get(previewKey);

        // If a cached result is not available, build a preview.
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
            preview = build(xdomOfficeDoc, attachmentName);

            // Cache the preview.
            previewsCache.set(previewKey, preview);
        }

        // Done.
        return preview;
    }

    /**
     * Prepares a preview {@link XDOM} from the given office document.
     * 
     * @param xdomOfficeDoc office document.
     * @param attachmentName name of the attachment which is to be previewed.
     * @return an {@link XDOM} holding a preview of the given office document.
     * @throws Exception if an error occurs while preparing the preview.
     */
    private XDOM build(XDOMOfficeDocument xdomOfficeDoc, AttachmentName attachmentName) throws Exception
    {
        // Dummy implementation.
        return xdomOfficeDoc.getContentDocument();
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
        XWikiContext xcontext = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        String documentName = documentNameSerializer.serialize(attachmentName.getDocumentName());
        XWikiDocument doc = xcontext.getWiki().getDocument(documentName, xcontext);
        XWikiAttachment attach = doc.getAttachment(attachmentName.getFileName());
        return attach.getVersion();
    }
}
