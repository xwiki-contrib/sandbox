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

package org.xwiki.xoo.xwikilib.convertersupport.internal;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.swizzle.confluence.Attachment;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ImageBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.DefaultAttachement;
import org.xwiki.rendering.listener.DocumentImage;
import org.xwiki.rendering.listener.Image;
import org.xwiki.rendering.listener.ImageType;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.xoo.xwikilib.convertersupport.XOOConverterSupport;

public class DefaultConverterSupport implements XOOConverterSupport
{

    private EmbeddableComponentManager ecm;

    /**
     * Constructor.
     * 
     * @param ecm the component manager
     */
    public DefaultConverterSupport(EmbeddableComponentManager ecm)
    {
        this.ecm = ecm;
    }

    /**
     * {@inheritDoc}
     */
    public List<Attachment> getAllImageAttachments(String source, Syntax syntax)
    {
        List<Attachment> attachments = new ArrayList<Attachment>();

        try {
            // Parse XWiki 2.0 Syntax using a Parser.
            Parser parser = ecm.lookup(Parser.class, syntax.toIdString());
            XDOM xdom = parser.parse(new StringReader(source));

            for (ImageBlock block : xdom.getChildrenByType(ImageBlock.class, true)) {

                Image image = block.getImage();
                if (image.getType() == ImageType.DOCUMENT) {
                    DocumentImage docImage = (DocumentImage) image;
                    String attName = docImage.getAttachmentName();
                    String docName = docImage.getDocumentName();
                    Attachment att = new Attachment(new HashMap<Object, Object>());
                    att.setPageId(docName);
                    att.setId(attName);
                    attachments.add(att);
                }
            }
            return attachments;

        } catch (ComponentLookupException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String imageNameCleaner(String source, Syntax syntax, List<Attachment> attachments)
    {

        try {
            // Parse XWiki 2.0 Syntax using a Parser.
            Parser parser = ecm.lookup(Parser.class, syntax.toIdString());
            XDOM xdom = parser.parse(new StringReader(source));

            for (ImageBlock block : xdom.getChildrenByType(ImageBlock.class, true)) {
                Block parentBlock = block.getParent();
                Image image = block.getImage();

                if (image.getType() == ImageType.DOCUMENT) {
                    DocumentImage docImage = (DocumentImage) image;
                    String attName = docImage.getAttachmentName();
                    String docName = docImage.getDocumentName();
                    Attachment att = new Attachment(new HashMap<Object, Object>());
                    att.setPageId(docName);
                    att.setId(attName);
                    attachments.add(att);

                    String newName = clearImageName(attName);
                    org.xwiki.rendering.listener.Attachment newAtt = new DefaultAttachement(null, newName);
                    Image newImage = new DocumentImage(newAtt);
                    Block newBlock = new ImageBlock(newImage, block.isFreeStandingURI());
                    parentBlock.replaceChild(newBlock, block);
                }
            }

            WikiPrinter printer = new DefaultWikiPrinter();
            BlockRenderer renderer = ecm.lookup(BlockRenderer.class, Syntax.XWIKI_2_0.toIdString());
            renderer.render(xdom, printer);

            return printer.toString();

        } catch (ComponentLookupException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the valid image name 
     * @param imageName the dirty image name
     * @return the short image name
     */
    private String clearImageName(String imageName)
    {
        File file = new File(imageName);
        if (imageName.contains(File.separator) && file.exists())
            return file.getName();
        return imageName;
    }

}
