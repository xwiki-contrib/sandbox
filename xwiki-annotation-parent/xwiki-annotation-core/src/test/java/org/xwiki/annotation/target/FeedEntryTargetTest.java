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
import org.junit.Ignore;
import org.junit.Test;
import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.TestDocumentFactory;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.annotation.internal.maintainment.AnnotationState;
import org.xwiki.annotation.utils.TestPurposeAnnotationImpl;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public class FeedEntryTargetTest extends AbstractTargetTest
{
    /**
     * Default constructor.
     */
    public FeedEntryTargetTest()
    {
        docName = "Feeds.FeedEntry";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.target.AbstractTargetTest#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();
        annotationTarget = getComponentManager().lookup(AnnotationTarget.class, "feedEntry");
    }

    /**
     * Test that rendering an annotation with no annotation results in the same html as the feedentry source.
     * 
     * @throws IOServiceException in case something goes wrong mocking the {@link org.xwiki.annotation.IOService}
     * @throws IOException in case something goes wrong mocking documents from corpus files
     */
    @Test
    public void getAnnotatedHTML() throws IOException, IOServiceException
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioTargetService).getSource(with(docName), with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getSource()));
                oneOf(ioTargetService).getRenderedContent(with(docName),
                    with(TestDocumentFactory.getDocument(docName.toString()).getSource()),
                    with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getRenderedContent()));

                oneOf(ioService).getSafeAnnotations(with(docName), with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName).getSafeAnnotations()));
            }
        });
        try {
            CharSequence html = annotationTarget.getAnnotatedHTML(docName, null);
            Assert.assertEquals(TestDocumentFactory.getDocument(docName.toString()).getAnnotatedContent(), html);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    /**
     * Test adding an annotation on a feed entry. <br />
     * NOTE: this test is failing because of the wrong handling of CDATA sections from the HTML parser: the selection
     * specified here appears twice, once in the description and another time in the content:encoded. The parser handles
     * CDATA sections as start tags, and parses the contents of these sections. This makes the content of the
     * content:encoded CDATA to be parsed as HTML, and all the text in the description be ignored (since there are no
     * tags in the description). Normally the first encounter of the selection should be in the description, at index
     * 649. The content index is at 1279. Also see the comment in the HTMLContentAlterer alter function.
     * 
     * @throws IOServiceException in case something goes wrong mocking the {@link org.xwiki.annotation.IOService}
     * @throws IOException in case something goes wrong mocking documents from corpus files
     */
    @Test
    @Ignore
    public void addAnnotation() throws IOServiceException, IOException
    {
        XWikiContext deprecatedContext = null;
        String user = "XWiki.Scribo";
        int offset = 0;
        String metadata = "Metadata #1";
        String selection =
            "dass die abgebildete Sternbrücke in dieser Form bald Geschichte sein "
                + "könnte. Zum 31.12.09 hat die Bahn eine Kündigung für einen wichtigen";
        String context = selection;
        final Annotation expectedAnnotation =
            new TestPurposeAnnotationImpl(docName, user, null, AnnotationState.SAFE, metadata, selection, context, 0,
                649, selection.length());
        mockery.checking(new Expectations()
        {
            {
                oneOf(ioTargetService).getSource(with(docName), with(any(XWikiContext.class)));
                will(returnValue(TestDocumentFactory.getDocument(docName.toString()).getSource()));

                oneOf(ioService).addAnnotation(with(docName), with(expectedAnnotation), with(any(XWikiContext.class)));
            }
        });
        try {
            annotationTarget.addAnnotation(metadata, selection, context, offset, docName, user, deprecatedContext);
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
