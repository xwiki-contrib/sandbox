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
package org.xwiki.annotation.maintainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jmock.Expectations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.AnnotationsMockSetup;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.DocumentUpdateEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Tests the annotation maintainer that updates annotations when documents change.
 * 
 * @version $Id$
 */
@RunWith(Parameterized.class)
public class AnnotationMaintainerTest extends AbstractComponentTestCase
{
    /**
     * Document description files to run this test for.
     */
    private static Collection<String[]> files = new ArrayList<String[]>();

    /**
     * Mock document to run tests for.
     */
    protected String docName;

    /**
     * The annotation maintainer that this class will test.
     */
    protected EventListener annotationMaintainer;

    /**
     * The setup for mocking components needed in annotation code.
     */
    protected AnnotationsMockSetup setup;

    static {
        addFileToTest("maintainer/plaintext/Plain1");
    }

    /**
     * Builds a maintainer test to test the passed document name.
     * 
     * @param docName the name of the document (and mock file) to test
     */
    public AnnotationMaintainerTest(String docName)
    {
        this.docName = docName;
    }

    /**
     * Adds a file to the list of files to run tests for.
     * 
     * @param docName the name of the document / file to test
     */
    protected static void addFileToTest(String docName)
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
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        annotationMaintainer = getComponentManager().lookup(EventListener.class, "annotation-maintainer");
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

        // register the IO mockups
        setup = new AnnotationsMockSetup(getComponentManager(), new TestDocumentFactory());
        setup.setupExpectations(docName);
    }

    /**
     * Tests the update of a document.
     * 
     * @throws IOException if anything goes wrong mocking the documents
     */
    @Test
    public void testUpdate() throws IOException
    {
        // ignore the docName ftm, just test the marvelous setup
        Event documentUpdateEvt = new DocumentUpdateEvent();
        MockDocument doc = ((TestDocumentFactory) setup.getDocFactory()).getDocument(docName);
        DocumentModelBridge mock = getDocumentModelBridgeMock(docName, doc.getModifiedSource(), doc.getSource());

        annotationMaintainer.onEvent(documentUpdateEvt, mock, null);

        assertSameAnnotations(doc.getUpdatedAnnotations(), doc.getAnnotations());
    }

    /**
     * Helper method to test if the two passed annotation lists contain the same annotations.
     * 
     * @param expected the expected list of annotations
     * @param actual the actual list of annotations
     */
    private void assertSameAnnotations(List<Annotation> expected, List<Annotation> actual)
    {
        assertTrue(expected.size() == actual.size());

        for (Annotation actualAnn : actual) {
            Annotation expectedAnn = getAnnotation(actualAnn.getId(), expected);
            assertNotNull(expectedAnn);
            assertEquals(expectedAnn.getInitialSelection(), actualAnn.getInitialSelection());
            assertEquals(expectedAnn.getSelectionContext(), actualAnn.getSelectionContext());
            assertEquals(expectedAnn.getState(), actualAnn.getState());
        }
    }

    /**
     * Helper function to get the annotation with the passed id from the passed list.
     * 
     * @param annId the id of the searched annotation
     * @param list the list of annotations where to search for the passed annotation
     * @return the found annotation
     */
    private Annotation getAnnotation(int annId, Collection<Annotation> list)
    {
        for (Annotation ann : list) {
            if (ann.getId() == annId) {
                return ann;
            }
        }

        return null;
    }

    /**
     * Creates a mock document model bridge for an updated document for the passed document name with the current
     * content and previous content.
     * 
     * @param docName the name of the document to mock
     * @param content the content of the document
     * @param previousContent the previous content to set to this mock, to be returned as a document model bridge mock
     *            on call of getOriginalDocument. If this parameter is {@code null}, this function will not be mocked
     * @return the created document model bridge mock
     */
    public DocumentModelBridge getDocumentModelBridgeMock(final String docName, final String content,
        final String previousContent)
    {
        String mockName = docName + "-" + (previousContent != null ? "modified" : "original");
        final DocumentModelBridge dmbMock = setup.getMockery().mock(DocumentModelBridge.class, mockName);
        setup.getMockery().checking(new Expectations()
        {
            {
                allowing(dmbMock).getFullName();
                will(returnValue(docName));

                allowing(dmbMock).getContent();
                will(returnValue(content));

                if (previousContent != null) {
                    allowing(dmbMock).getOriginalDocument();
                    will(returnValue(getDocumentModelBridgeMock(docName, previousContent, null)));
                }
            }
        });

        return dmbMock;
    }
}
