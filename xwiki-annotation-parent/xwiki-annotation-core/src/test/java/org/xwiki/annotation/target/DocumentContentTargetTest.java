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
import org.jmock.Mockery;
import org.junit.Test;
import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.Documents;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.IOTargetService;
import org.xwiki.annotation.internal.annotation.Annotation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.exception.AnnotationServiceException;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.test.AbstractComponentTestCase;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
public class DocumentContentTargetTest extends AbstractComponentTestCase
{
    private final Mockery mockery = new Mockery();

    private static IOTargetService ioTargetService;

    private static IOService ioService;

    private static AnnotationTarget documentContentAnnotationTarget;

    private static ExecutionContextManager executionContextManager;

    private static final CharSequence document01 = "LePrinceChapitre15";

    private static final XWikiContext deprecatedContext = null;

    private static final String user = "XWiki.Scribo";

    private static final int offset = 0;

    private static final CharSequence metadata = "Byte FM is a great web radio.";

    private static final CharSequence selection = "Mais, dans le dessein que";

    private static final CharSequence context = selection;

    {
        // IOTargetService mockup
        ioTargetService = mockery.mock(IOTargetService.class);

        ioService = mockery.mock(IOService.class);

        // ExecutionContextManager mockup
        executionContextManager = mockery.mock(ExecutionContextManager.class);
    }

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

        // Setting up WritableIOService
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        getComponentManager().registerComponent(ioDesc, ioService);

        // Setting up ExecutionContextManager
        DefaultComponentDescriptor<ExecutionContextManager> ecmDesc =
            new DefaultComponentDescriptor<ExecutionContextManager>();
        ecmDesc.setRole(ExecutionContextManager.class);
        getComponentManager().registerComponent(ecmDesc, executionContextManager);

        mockery.checking(new Expectations()
        {
            {
                /* IOService configuration */
                oneOf(ioService).getSafeAnnotations(with(document01), with(any(XWikiContext.class)));
                will(returnValue(Documents.valueOf(document01.toString()).getSafeAnnotations()));
                oneOf(ioService).addAnnotation(with(document01), with(any(Annotation.class)),
                    with(any(XWikiContext.class)));

                /* IOTargetService configuration */
                exactly(2).of(ioTargetService).getSource(with(document01), with(any(XWikiContext.class)));
                will(returnValue(new Source()
                {
                    public CharSequence getSource()
                    {
                        try {
                            return Documents.valueOf(document01.toString()).getSource();
                        } catch (IOException e) {
                            return "";
                        }
                    }
                }));
                exactly(2).of(ioTargetService).getRenderedContent(with(document01), with(equal(new Source()
                {
                    public CharSequence getSource()
                    {
                        try {
                            return Documents.valueOf(document01.toString()).getTaggedContent();
                        } catch (IOException e) {
                            return "";
                        }
                    }

                    public boolean equals(Object obj)
                    {
                        if (!(obj instanceof Source)) {
                            return false;
                        }
                        Source other = (Source) obj;
                        return other.getSource().toString().equals(getSource().toString());
                    };
                })), with(deprecatedContext));
                will(returnValue(Documents.valueOf(document01.toString()).getMixContent()));

                /* ExecutionContextManager configuration */
                oneOf(executionContextManager).initialize(with(any(ExecutionContext.class)));
            }
        });

        documentContentAnnotationTarget = getComponentManager().lookup(AnnotationTarget.class, "documentContent");

        super.setUp();
    }

    @Test
    public void getAnnotatedHTML()
    {
        try {
            CharSequence html = documentContentAnnotationTarget.getAnnotatedHTML(document01, deprecatedContext);
            CharSequence expected = Documents.valueOf(document01.toString()).getExpectedAnnotatedContent();
            Assert.assertEquals(expected, html);
        } catch (AnnotationServiceException e) {
            Assert.fail(getExceptionFailureMessage(e));
        } catch (IOException e) {
            Assert.fail(getExceptionFailureMessage(e));
        }
    }

    @Test
    public void addAnnotation()
    {
        try {
            documentContentAnnotationTarget.addAnnotation(metadata, selection, context, offset, document01, user,
                deprecatedContext);
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
