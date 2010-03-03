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
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.TransformationManager;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityManager;

/**
 * Implementation of {@link OfficePreviewBuilder} which is responsible for building previews of office presentations.
 * 
 * @version $Id$
 */
@Component("presentation")
public class PresentationOfficePreviewBuilder extends AbstractOfficePreviewBuilder
{
    /**
     * Used to parse the presentation template.
     */
    @Requirement
    private VelocityManager velocityManager;

    /**
     * Used to build the presentation xdom.
     */
    @Requirement("xwiki/2.0")
    private Parser xwiki20Parser;
    
    /**
     * Used to transform the XDOM.
     */
    @Requirement
    private TransformationManager transformationManager;

    /**
     * {@inheritDoc}
     */
    protected OfficeDocumentPreview build(AttachmentReference attachRef, String version, InputStream data)
        throws Exception
    {
        Map<String, InputStream> inputs = Collections.singletonMap(attachRef.getName(), data);
        Map<String, byte[]> artifacts =
            officeManager.getConverter().convert(inputs, attachRef.getName(), "output.html");

        // First extract all the image slides.
        List<String> slideNames = new ArrayList<String>();
        for (Map.Entry<String, byte[]> entry : artifacts.entrySet()) {
            String key = entry.getKey();
            // TODO: Verify if this pattern is a constant or not. I've checked most of the openoffice configuration
            // options but could not find a way to change the output image format or the file naming convention.
            if (key.matches("img\\d+\\.jpg")) {
                slideNames.add(key);
            }
        }

        // Sort the slide names in order.
        Collections.sort(slideNames, new Comparator<String>()
        {
            public int compare(String name1, String name2)
            {
                return extractSlideIndex(name1).compareTo(extractSlideIndex(name2));
            }
        });

        // Write the image slides in order.
        List<String> slideUrls = new ArrayList<String>();
        Set<File> slideFiles = new HashSet<File>();
        for (String slideName : slideNames) {
            try {
                // Write the slide into a temporary file.
                File tempFile = writeArtifact(attachRef, slideName, artifacts.get(slideName));

                // Collect the external URL which refers this temporary file.
                String url = getURL(attachRef, tempFile.getName());
                if (null != url) {
                    slideUrls.add(url);
                }

                // Collect the temporary file so that it can be cleaned when the preview is disposed.
                slideFiles.add(tempFile);
            } catch (Exception ex) {
                String message = "Error while processing slide [%s].";
                getLogger().error(String.format(message, slideName), ex);
            }
        }

        XDOM presentation = buildPresentationXDOM(slideUrls);

        return new OfficeDocumentPreview(attachRef, version, presentation, slideFiles);
    }

    /**
     * Builds a presentation which refers the slides pointed by <b>slideUrls</b> argument.
     * 
     * @param slideUrls list of urls which point to presentation slides.
     * @return {@link XDOM} containing a slide show.
     * @throws Exception if an error occurs while building the presentation xdom.
     */
    private XDOM buildPresentationXDOM(List<String> slideUrls) throws Exception
    {
        // TODO: Make this configurable.
        String template = "/org/xwiki/officepreview/internal/presentation.vm";

        InputStreamReader templateReader = null;

        try {
            VelocityEngine vEngine = velocityManager.getVelocityEngine();
            VelocityContext vContext = new VelocityContext();
            vContext.put("slides", slideUrls);

            StringWriter outputWriter = new StringWriter();
            templateReader = new InputStreamReader(getClass().getResourceAsStream(template));

            vEngine.evaluate(vContext, outputWriter, template, templateReader);

            XDOM xdom = xwiki20Parser.parse(new StringReader(outputWriter.toString()));
            
            transformationManager.performTransformations(xdom, Syntax.XWIKI_2_0);
                        
            return xdom;
        } catch (Exception ex) {
            throw new Exception("Error while building presentation XDOM.", ex);
        } finally {
            IOUtils.closeQuietly(templateReader);
        }
    }

    /**
     * Extracts the index of a presentation slide given it's name.
     * 
     * @param slideName name of the image representing the slide.
     * @return index of the slide in the slide show.
     */
    private Integer extractSlideIndex(String slideName)
    {
        int dot = slideName.indexOf('.');
        String strIndex = slideName.substring(3, dot);
        return Integer.parseInt(strIndex);
    }
}
