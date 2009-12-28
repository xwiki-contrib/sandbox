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

package org.xwiki.annotation.internal.target;

import java.io.StringReader;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.annotation.maintainer.AnnotationState;
import org.xwiki.annotation.renderer.AnnotationPrintRenderer;
import org.xwiki.annotation.target.AnnotationTarget;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * Defines components used for XWiki document target.
 * 
 * @version $Id$
 */
@Component()
public class DefaultDocumentContentTarget implements AnnotationTarget
{
    /**
     * The storage service for annotations.
     */
    @Requirement
    private IOService ioService;

    /**
     * Component manager used to lookup the content alterer needed for the specific document.
     */
    @Requirement
    private ComponentManager componentManager;

    /**
     * The storage service for annotation targets (documents).
     */
    @Requirement
    private IOTargetService documentContentTargetService;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.target.AnnotationTarget#addAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      java.lang.CharSequence, int, java.lang.CharSequence, java.lang.CharSequence)
     */
    public void addAnnotation(CharSequence metadata, CharSequence selection, CharSequence selectionContext, int offset,
        CharSequence documentName, CharSequence user) throws AnnotationServiceException
    {
        try {
            // nothing. FTM send invalid positions for annotation offset&length since they won't be used
            // create the annotation with this data and send it to the storage service
            // TODO: also think of mapping the annotation on the document at add time and fail it if it's not mappable,
            // for extra security
            Annotation annotation =
                new Annotation(documentName, user, null, AnnotationState.SAFE, metadata, selection, selectionContext,
                    0, -1, -1);
            ioService.addAnnotation(documentName, annotation);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException("An exception occurred when accessing the storage services", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.target.AnnotationTarget#getAnnotatedHTML(java.lang.CharSequence)
     */
    public CharSequence getAnnotatedHTML(CharSequence documentName) throws AnnotationServiceException
    {
        try {
            String source = documentContentTargetService.getSource(documentName);
            String sourceSyntaxId = documentContentTargetService.getSourceSyntax(documentName.toString());

            Parser parser = componentManager.lookup(Parser.class, sourceSyntaxId);
            XDOM xdom = parser.parse(new StringReader(source));

            // run transformations
            SyntaxFactory syntaxFactory = componentManager.lookup(SyntaxFactory.class);
            Syntax sourceSyntax = syntaxFactory.createSyntaxFromIdString(sourceSyntaxId);
            TransformationManager transformationManager = componentManager.lookup(TransformationManager.class);
            transformationManager.performTransformations(xdom, sourceSyntax);

            AnnotationPrintRenderer annotationsRenderer =
                componentManager.lookup(AnnotationPrintRenderer.class, "annotations-xhtml/1.0");
            WikiPrinter printer = new DefaultWikiPrinter();
            annotationsRenderer.setPrinter(printer);
            // set the annotations for this renderer
            annotationsRenderer.setAnnotations(ioService.getSafeAnnotations(documentName));

            xdom.traverse(annotationsRenderer);

            return printer.toString();
        } catch (Exception exc) {
            throw new AnnotationServiceException(exc);
        }
    }
}
