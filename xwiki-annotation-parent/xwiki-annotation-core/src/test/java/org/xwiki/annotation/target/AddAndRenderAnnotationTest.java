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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.AnnotationsMockSetup;
import org.xwiki.annotation.TestDocumentFactory;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.test.AbstractComponentTestCase;

import com.xpn.xwiki.XWikiContext;

/**
 * Parameterized test to perform all annotations add and display tests described in test files. <br />
 * TODO: this class could be split in a getAnnotatedHTML test and an addAnnotation test, considering that we'd have
 * distinct test cases for them, but for now we test the full pass: add annotation -> map it as expected -> render it as
 * expected.
 * 
 * @version $Id$
 */
@RunWith(Parameterized.class)
public class AddAndRenderAnnotationTest extends AbstractComponentTestCase
{
    /**
     * Document description files to run this test for.
     */
    private static Collection<String[]> files = new ArrayList<String[]>();

    /**
     * The mocks setup.
     */
    protected AnnotationsMockSetup setup;

    /**
     * Annotation target component tested by this suite.
     */
    protected AnnotationTarget annotationTarget;

    /**
     * Mock document to run tests for.
     */
    protected String docName;

    static {
        // FIXME: checkstyle is so gonna shout when this will be longer than 30 files
        addFileToTest("LePrince.Chapitre15");
        addFileToTest("Robots.Laws");
    }

    /**
     * Creates a test for the passed document. Will be instantiated by the parameterized runner for all the parameters.
     * 
     * @param docName the document (and corpus filename) to run tests for
     */
    public AddAndRenderAnnotationTest(String docName)
    {
        this.docName = docName;
    }

    /**
     * Adds a file to the list of files to run tests for.
     * 
     * @param docName the name of the document / file to test
     */
    private static void addFileToTest(String docName)
    {
        files.add(new String[] {docName});
    }

    /**
     * @return list of corpus files to instantiate tests for
     */
    @Parameters
    public static Collection<String[]> data()
    {
        return files;
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

        setup = new AnnotationsMockSetup(getComponentManager());
        // setup the expectations here. might as well write a setUp(), there's no difference
        setup.setupExpectations(docName);
        annotationTarget = getComponentManager().lookup(AnnotationTarget.class, "documentContent");
    }

    /**
     * Test that adding each of the annotations in the document description file works as expected: the offset & length
     * are correctly computed.
     * 
     * @throws IOServiceException in case something goes wrong mocking the {@link org.xwiki.annotation.IOService}
     * @throws IOException in case something goes wrong mocking documents from corpus files
     */
    @Test
    public void addAnnotation() throws IOServiceException, IOException
    {
        // get the annotations for the tested document
        List<Annotation> annotations = TestDocumentFactory.getDocument(docName).getSafeAnnotations();
        // clean up the document's annotations list just to be sure that it wouldn't interfere with the test
        TestDocumentFactory.getDocument(docName).set("annotations", Collections.EMPTY_LIST);

        for (final Annotation ann : annotations) {
            // expect a storage service addAnnotation function to be called with the annotation read from the test file
            // comparison in the Annotation.equals is made on author, text and position
            setup.getMockery().checking(new Expectations()
            {
                {
                    oneOf(setup.getIoService()).addAnnotation(with(docName), with(ann), with(any(XWikiContext.class)));
                }
            });

            try {
                // add the annotation for the specification read from the test file
                annotationTarget.addAnnotation(ann.getAnnotation(), ann.getInitialSelection(), ann
                    .getSelectionContext(), 0, docName, ann.getAuthor(), null);
            } catch (AnnotationServiceException e) {
                Assert.fail(getExceptionFailureMessage(e));
            }
        }
    }

    /**
     * Test rendering the annotations in the document description file results in the annotated html.
     * 
     * @throws IOServiceException in case something goes wrong mocking the {@link org.xwiki.annotation.IOService}
     * @throws IOException in case something goes wrong mocking documents from corpus files
     */
    @Test
    public void getAnnotatedHTML() throws IOServiceException, IOException
    {
        // context used by the annotation target service
        final XWikiContext deprecatedContext = null;

        try {
            CharSequence html = annotationTarget.getAnnotatedHTML(docName, deprecatedContext);
            CharSequence expected = TestDocumentFactory.getDocument(docName.toString()).getAnnotatedContent();
            Assert.assertEquals(expected, html);
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
