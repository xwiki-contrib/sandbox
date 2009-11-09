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

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.xwiki.annotation.AnnotationService.Target;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.test.AbstractComponentTestCase;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public class AnnotationServiceTest extends AbstractComponentTestCase
{
    private final Mockery mockery = new Mockery();

    private CharSequence annotationID = "-1";

    private static int offset = 0;

    private static CharSequence documentName = "DocumentO1";

    private static CharSequence selection = "Three Laws of Robotics A robot may";

    private static CharSequence selectionContext = selection;

    private static CharSequence metadata;

    private static Target target = Target.documentContent;

    private static XWikiContext deprecatedContext = null;

    private static CharSequence user = "XWiki.Scribo";

    private static IOTargetService ioTargetService;

    private static IOService ioService;

    private static AnnotationService annotationService;

    {
        // IOTargetService mockup
        ioTargetService = mockery.mock(IOTargetService.class);

        ioService = mockery.mock(IOService.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception
    {
        // Setting up IOTargetService
        DefaultComponentDescriptor<IOTargetService> iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        iotsDesc.setRoleHint("FEEDENTRY");
        getComponentManager().registerComponent(iotsDesc, ioTargetService);
        iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        getComponentManager().registerComponent(iotsDesc, ioTargetService);

        // Setting up IOService
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        getComponentManager().registerComponent(ioDesc, ioService);

        mockery.checking(new Expectations()
        {
            {
                exactly(2).of(ioTargetService).getSource(documentName, deprecatedContext);
                will(returnValue(new Source()
                {
                    public CharSequence getSource()
                    {
                        try {
                            return Documents.valueOf(documentName.toString()).getSource();
                        } catch (IOException e) {
                            return "";
                        }
                    }

                    public boolean equals(Object obj)
                    {
                        if (!(obj instanceof Source)) {
                            System.err.println(false);
                            return false;
                        }
                        Source other = (Source) obj;
                        System.err.println(other.getSource().toString().equals(getSource().toString()));
                        return other.getSource().toString().equals(getSource().toString());
                    }
                }));

                oneOf(ioService).addAnnotation(with(documentName), with(any(Annotation.class)),
                    with(any(XWikiContext.class)));
                oneOf(ioService).removeAnnotation(documentName, annotationID, deprecatedContext);

                oneOf(ioService).getSafeAnnotations(documentName, deprecatedContext);
                will(returnValue(Documents.valueOf(documentName.toString()).getSafeAnnotations()));

                oneOf(ioTargetService).getRenderedContent(with(documentName), with(any(Source.class)),
                    with(deprecatedContext));
                will(returnValue(Documents.valueOf(documentName.toString()).getRenderedContent()));
            }
        });

        annotationService = getComponentManager().lookup(AnnotationService.class);

        super.setUp();
    }

    /**
     * @param e the exception to provide failure message for
     * @return a failure message computed for the passed exception
     */
    private String getExceptionFailureMessage(Throwable e)
    {
        return "An exception was thrown: " + e.getMessage();
    }

    @Test
    public void addAnnotation()
    {
        try {
            annotationService.addAnnotation(metadata, selection, selectionContext, offset, documentName, user,
                deprecatedContext, target);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    @Test
    public void getAnnotatedHTML()
    {
        try {
            CharSequence html = annotationService.getAnnotatedHTML(documentName, deprecatedContext, target);
            Assert.assertEquals(Documents.valueOf(documentName.toString()).getRenderedContent(), html);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        } catch (IOException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    @Test
    public void getSafeAnnotations()
    {
        try {
            Collection<Annotation> actual =
                annotationService.getSafeAnnotations(documentName, deprecatedContext, target);
            Collection<Annotation> expected = Documents.valueOf(documentName.toString()).getSafeAnnotations();
            Assert.assertEquals(expected, actual);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    @Test
    public void removeAnnotation()
    {
        try {
            annotationService.removeAnnotation(documentName, annotationID, deprecatedContext, target);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }
}
