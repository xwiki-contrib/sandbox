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

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.maintainer.AnnotationState;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * @version $Id$
 */
public class AnnotationServiceTest extends AbstractComponentTestCase
{
    /**
     * The mocks setup.
     */
    private AnnotationsMockSetup setup;

    /**
     * The tested annotation service.
     */
    private AnnotationService annotationService;

    /**
     * The document name to test.
     */
    private String docName = "Robots.Laws";

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

        setup = new AnnotationsMockSetup(getComponentManager());
        // setup the expectations here, might as well write a setUp
        setup.setupExpectations(docName);
        // lookup the annotation service to test
        annotationService = getComponentManager().lookup(AnnotationService.class);
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
     * @throws IOServiceException in case the IOService mock cannot return the source of a document
     * @throws IOException in case the mock document cannot be read correctly
     */
    @Test
    public void addAnnotation() throws IOServiceException, IOException
    {
        final Annotation expectedAnnotation =
            new Annotation(docName, user, new Date().toString(), AnnotationState.SAFE, metadata, selection,
                selectionContext, 1, 2, 39);
        // expect the addAnnotation method of the IOService to be called with an annotation parameter
        setup.getMockery().checking(new Expectations()
        {
            {
                oneOf(setup.getIoService()).addAnnotation(with(docName), with(expectedAnnotation));
            }
        });

        try {
            annotationService.addAnnotation(metadata, selection, selectionContext, 0, docName, user);
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
        try {
            CharSequence html = annotationService.getAnnotatedHTML(docName);
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
        try {
            Collection<Annotation> actual = annotationService.getSafeAnnotations(docName);
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
        setup.getMockery().checking(new Expectations()
        {
            {
                oneOf(setup.getIoService()).removeAnnotation(docName, "1");
            }
        });
        try {
            annotationService.removeAnnotation(docName, "1");
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }
}
