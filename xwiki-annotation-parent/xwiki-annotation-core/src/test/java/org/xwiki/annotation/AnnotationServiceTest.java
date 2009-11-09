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

package org.xwiki.annotation;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.xwiki.annotation.AnnotationService.Target;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.context.SourceImpl;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.utils.TestPurposeAnnotationImpl;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.test.AbstractComponentTestCase;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public class AnnotationServiceTest extends AbstractComponentTestCase
{
    /**
     * Mockery to setup IO services in this test.
     */
    private Mockery mockery = new Mockery();

    /**
     * IOTargetService used by this test.
     */
    private IOTargetService ioTargetService;

    /**
     * IOService used in this test.
     */
    private IOService ioService;

    /**
     * The tested annotation service.
     */
    private AnnotationService annotationService;

    /**
     * The document name to test.
     */
    private String docName = "Robots.Laws";

    /**
     * The XWikiContext used to invoke the AnnotationService.
     */
    private XWikiContext deprecatedContext;

    /**
     * The selection of the annotation to add.
     */
    private String selection = "Three Laws of Robotics A robot may";

    /**
     * The context of the selection.
     */
    private String selectionContext = selection;

    /**
     * The annotation text of the annotation.
     */
    private String metadata = "Metadata #1";

    /**
     * The user adding the annotation.
     */
    private String user = "XWiki.Guest";

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        // register mock IOService and mock IOTargetService
        ioTargetService = mockery.mock(IOTargetService.class);
        DefaultComponentDescriptor<IOTargetService> iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        iotsDesc.setRoleHint("FEEDENTRY");
        getComponentManager().registerComponent(iotsDesc, ioTargetService);
        iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        getComponentManager().registerComponent(iotsDesc, ioTargetService);

        ioService = mockery.mock(IOService.class);
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        getComponentManager().registerComponent(ioDesc, ioService);
        
        // lookup the annotation service to test
        annotationService = getComponentManager().lookup(AnnotationService.class);        
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        TestDocumentFactory.reset();
    }

    /**
     * @param e the exception to provide failure message for
     * @return a failure message computed for the passed exception
     */
    private String getExceptionFailureMessage(Throwable e)
    {
        return "An exception was thrown: " + e.getMessage();
    }

    /**
     * Test adding an annotation to a document.
     * 
     * @throws IOServiceException in case the {@link IOService} mock cannot return the source of a document
     * @throws IOException in case the mock document cannot be read correctly
     */
    @Test
    public void addAnnotation() throws IOServiceException, IOException
    {
        final Annotation expectedAnnotation =
            new TestPurposeAnnotationImpl(docName, user, new Date().toString(), AnnotationState.SAFE, metadata,
                selection, selectionContext, 1, 2, 39);
        // expect the addAnnotation method of the IOService to be called with an annotation parameter
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioTargetService).getSource(docName, deprecatedContext);
                will(returnValue(TestDocumentFactory.getDocument(docName).getSource()));
                oneOf(ioService).addAnnotation(with(docName), with(expectedAnnotation), with(any(XWikiContext.class)));
            }
        });

        try {
            annotationService.addAnnotation(metadata, selection, selectionContext, 0, docName, user, deprecatedContext,
                Target.documentContent);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * Test that rendering a document with no annotation doesn't change the HTML of that document.
     * 
     * @throws IOServiceException in case any exception occurs returning the source of a document
     * @throws IOException in case the mock document cannot be read correctly
     */
    @Test
    public void getAnnotatedHTML() throws IOServiceException, IOException
    {
        // expect the source of the doc with no modification whatsoever because there's no annotation to be rendered
        final Source expectedSource = new SourceImpl(TestDocumentFactory.getDocument(docName).getTextSource());
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioTargetService).getSource(docName, deprecatedContext);
                will(returnValue(TestDocumentFactory.getDocument(docName).getSource()));

                oneOf(ioTargetService).getRenderedContent(docName, expectedSource, deprecatedContext);
                will(returnValue(TestDocumentFactory.getDocument(docName).getRenderedContent()));

                oneOf(ioService).getSafeAnnotations(docName, deprecatedContext);
                will(returnValue(TestDocumentFactory.getDocument(docName).getSafeAnnotations()));
            }
        });
        try {
            CharSequence html = annotationService.getAnnotatedHTML(docName, deprecatedContext, Target.documentContent);
            Assert.assertEquals(TestDocumentFactory.getDocument(docName).getRenderedContent(), html);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        } catch (IOException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * Test that the annotation set is returned by the annotation service with no modification from the actual IOService
     * response of the annotations.
     * 
     * @throws IOServiceException in case anything goes wrong in the mock retrieving the annotations
     * @throws IOException in case anything goes wrong mocking the document from file
     */
    @Test
    public void getSafeAnnotations() throws IOServiceException, IOException
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioService).getSafeAnnotations(docName, deprecatedContext);
                will(returnValue(TestDocumentFactory.getDocument(docName).getSafeAnnotations()));
            }
        });
        try {
            Collection<Annotation> actual =
                annotationService.getSafeAnnotations(docName, deprecatedContext, Target.documentContent);
            Collection<Annotation> expected = TestDocumentFactory.getDocument(docName).getSafeAnnotations();
            Assert.assertEquals(expected, actual);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * Test that requesting the remove of an annotation calls correctly the remove method in the IOService.
     * 
     * @throws IOServiceException if there is an exception mocking the IOService remove method.
     */
    @Test
    public void removeAnnotation() throws IOServiceException
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioService).removeAnnotation(docName, "1", deprecatedContext);
            }
        });
        try {
            annotationService.removeAnnotation(docName, "1", deprecatedContext, Target.documentContent);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }
}
