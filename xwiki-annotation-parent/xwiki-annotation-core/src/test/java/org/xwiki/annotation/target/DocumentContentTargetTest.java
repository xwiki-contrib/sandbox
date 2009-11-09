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

package org.xwiki.annotation.target;

import java.io.IOException;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.TestDocumentFactory;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.context.SourceImpl;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.utils.TestPurposeAnnotationImpl;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public class DocumentContentTargetTest extends AbstractTargetTest
{
    /**
     * Default constructor.
     */
    public DocumentContentTargetTest()
    {
        // setup tested document name
        docName = "LePrince.Chapitre15";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        annotationTarget = getComponentManager().lookup(AnnotationTarget.class, "documentContent");
    }

    /**
     * Test getting the annotated HTML for a document with a few added annotations.
     * 
     * @throws IOServiceException in case something goes wrong mocking the {@link org.xwiki.annotation.IOService}
     * @throws IOException in case something goes wrong mocking documents from corpus files
     */
    @Test
    public void getAnnotatedHTML() throws IOServiceException, IOException
    {
        // context used by the annotation target service
        final XWikiContext deprecatedContext = null;
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioService).getSafeAnnotations(with(docName), with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getSafeAnnotations()));

                exactly(2).of(ioTargetService).getSource(with(docName), with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getSource()));
                Source expectedSource =
                    new SourceImpl(TestDocumentFactory.getDocument(docName.toString()).getSourceWithMarkers());
                oneOf(ioTargetService).getRenderedContent(docName, expectedSource, deprecatedContext);
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getRenderedContentWithMarkers()));
            }
        });

        try {
            CharSequence html = annotationTarget.getAnnotatedHTML(docName, deprecatedContext);
            CharSequence expected = TestDocumentFactory.getDocument(docName.toString()).getAnnotatedContent();
            Assert.assertEquals(expected, html);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * Test that adding an annotation works as expected.
     * 
     * @throws IOServiceException in case something goes wrong mocking the {@link org.xwiki.annotation.IOService}
     * @throws IOException in case something goes wrong mocking documents from corpus files
     */
    @Test
    public void addAnnotation() throws IOServiceException, IOException
    {
        String user = "XWiki.Scribo";
        String metadata = "Metadata #4";
        String selection = "Mais, dans le dessein que";
        String context = selection;

        // date is not tested on annotation equality so we can always use null
        final Annotation expectedAnnotation =
            new TestPurposeAnnotationImpl(docName.toString(), user, null, AnnotationState.SAFE, metadata, selection,
                context, 0, 411, selection.length());

        mockery.checking(new Expectations()
        {
            {
                oneOf(ioService).addAnnotation(with(docName), with(expectedAnnotation), with(any(XWikiContext.class)));
                oneOf(ioTargetService).getSource(with(docName), with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getSource()));
            }
        });

        try {
            annotationTarget.addAnnotation(metadata, selection, context, 0, docName, user, null);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * @param e the exception to provide failure message for
     * @return a failure message computed for the passed exception
     */
    private String getExceptionFailureMessage(Throwable e)
    {
        return "An exception was thrown: " + e.getMessage();
    }
}
