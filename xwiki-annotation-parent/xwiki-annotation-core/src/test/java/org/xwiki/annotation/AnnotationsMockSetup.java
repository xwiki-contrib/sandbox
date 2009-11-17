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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

import com.xpn.xwiki.XWikiContext;

/**
 * Mock setup for the annotations tests, mocking the {@link IOService} and {@link IOTargetService} to provide documents
 * functions for the data in the test files.
 * 
 * @version $Id$
 */
public class AnnotationsMockSetup
{
    /**
     * Mockery to setup IO services in this test, setup as a JUnit4 mockery so that tests fail when expectations are not
     * met so that we test components through invocation expectations.
     */
    protected Mockery mockery = new JUnit4Mockery();

    /**
     * IOTargetService used by this test.
     */
    protected IOTargetService ioTargetService;

    /**
     * IOService used in this test.
     */
    protected IOService ioService;

    /**
     * Builds an annotation mock setup registering mocked {@link IOService} and {@link IOTargetService} to manipulate
     * documents from the test description files.
     * 
     * @param componentManager the component manager to register the services with
     * @throws ComponentRepositoryException if the components cannot be registered
     */
    public AnnotationsMockSetup(ComponentManager componentManager) throws ComponentRepositoryException
    {
        // IOTargetService mockup
        ioTargetService = mockery.mock(IOTargetService.class);
        DefaultComponentDescriptor<IOTargetService> iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        componentManager.registerComponent(iotsDesc, ioTargetService);

        // IOService mockup
        ioService = mockery.mock(IOService.class);
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        componentManager.registerComponent(ioDesc, ioService);

        // reset the document factory, to start with a clean factory every time this mock is setup
        TestDocumentFactory.reset();
    }

    /**
     * Sets up the expectations for the {@link IOService} and {@link IOTargetService} to return correctly the values in
     * the test files for {@code docName}. Call this function when operating with mocked documents to provide all the
     * information in the test file (document source, rendered contents, annotations).
     * 
     * @param docName the name of the document to setup expectations for
     * @throws IOServiceException if something wrong happens while mocking the documents access
     * @throws IOException if something wrong happens while mocking the documents access
     */
    public void setupExpectations(final String docName) throws IOServiceException, IOException
    {
        mockery.checking(new Expectations()
        {
            {
                MockDocument mDoc = TestDocumentFactory.getDocument(docName);
                allowing(ioService).getSafeAnnotations(with(docName), with(any(XWikiContext.class)));
                will(returnValue(mDoc.getSafeAnnotations()));

                allowing(ioTargetService).getSource(with(docName), with(any(XWikiContext.class)));
                will(returnValue(mDoc.getSource()));

                // return the rendered content of the doc if the input is the unchanged source
                allowing(ioTargetService).getRenderedContent(with(docName), with(mDoc.getSource()),
                    with(any(XWikiContext.class)));
                will(returnValue(mDoc.getRenderedContent()));

                // return the rendered content with annotation markers if the input is the source with markers inserted
                allowing(ioTargetService).getRenderedContent(with(docName),
                    with(mDoc.getSourceWithMarkers()), with(any(XWikiContext.class)));
                will(returnValue(mDoc.getRenderedContentWithMarkers()));
            }
        });
    }

    /**
     * @return the mockery
     */
    public Mockery getMockery()
    {
        return mockery;
    }

    /**
     * @return the ioTargetService
     */
    public IOTargetService getIoTargetService()
    {
        return ioTargetService;
    }

    /**
     * @return the ioService
     */
    public IOService getIoService()
    {
        return ioService;
    }
}
