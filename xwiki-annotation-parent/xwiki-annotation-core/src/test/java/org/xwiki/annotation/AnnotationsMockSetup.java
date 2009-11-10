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

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.xwiki.annotation.internal.context.Source;
import org.xwiki.annotation.internal.context.SourceImpl;
import org.xwiki.annotation.internal.exception.IOServiceException;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;

import com.xpn.xwiki.XWikiContext;

/**
 * Mock setup for the annotations tests, mocking the {@link IOService} and {@link IOTargetService} for documents related
 * functions, with documents loaded from file descriptions. If a file with the same name as the passed {@code docName}
 * of these functions, an {@link IOException} will be thrown.
 * 
 * @version $Id$
 */
public class AnnotationsMockSetup
{
    /**
     * Mockery to setup IO services in this test.
     */
    protected Mockery mockery = new Mockery();

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
     * @throws IOServiceException if something happens while mocking the documents
     */
    public AnnotationsMockSetup(ComponentManager componentManager) throws ComponentRepositoryException,
        IOServiceException
    {
        // IOTargetService mockup
        ioTargetService = mockery.mock(IOTargetService.class);
        DefaultComponentDescriptor<IOTargetService> iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        iotsDesc.setRoleHint("FEEDENTRY");
        componentManager.registerComponent(iotsDesc, ioTargetService);
        iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        componentManager.registerComponent(iotsDesc, ioTargetService);

        // IOService mockup
        ioService = mockery.mock(IOService.class);
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        componentManager.registerComponent(ioDesc, ioService);

        // reset the document factory, to start with a clean factory every time this mock is setup
        TestDocumentFactory.reset();

        // and now a little setup for these mocks, to allow all actions on documents and mock them correctly according
        // to their descriptions in files
        mockery.checking(new Expectations()
        {
            {
                allowing(ioService).getSafeAnnotations(with(aNonNull(CharSequence.class)),
                    with(any(XWikiContext.class)));
                will(returnDocumentProperty());

                allowing(ioTargetService).getSource(with(aNonNull(CharSequence.class)), with(any(XWikiContext.class)));
                will(returnDocumentProperty());

                allowing(ioTargetService).getRenderedContent(with(aNonNull(CharSequence.class)),
                    with(any(Source.class)), with(any(XWikiContext.class)));
                will(returnDocumentProperty());
            }
        });
    }

    /**
     * Factory method for the {@link ReturnDocPropertyAction}.
     * 
     * @return an action to return the requested document property from the passed parameters.
     */
    public static Action returnDocumentProperty()
    {
        return new AnnotationsMockSetup.ReturnDocPropertyAction();
    }

    /**
     * JMock action to mock functions on documents from the description files, according to the document requested, the
     * function called on the document and its parameters.
     * 
     * @version $Id$
     */
    public static class ReturnDocPropertyAction implements Action
    {
        /**
         * {@inheritDoc}
         * 
         * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
         */
        public void describeTo(Description description)
        {
            description
                .appendText("returns the corresponding values for test documents described in files for document "
                    + "aware calls in mocked services");
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.jmock.api.Invokable#invoke(org.jmock.api.Invocation)
         */
        public Object invoke(Invocation invocation) throws IOServiceException, IOException
        {
            if (invocation.getInvokedMethod().getName().equals("getSafeAnnotations")) {
                return TestDocumentFactory.getDocument(invocation.getParameter(0).toString()).getSafeAnnotations();
            }
            if (invocation.getInvokedMethod().getName().equals("getSource")) {
                return TestDocumentFactory.getDocument(invocation.getParameter(0).toString()).getSource();
            }
            if (invocation.getInvokedMethod().getName().equals("getRenderedContent")) {
                Source rawSource = TestDocumentFactory.getDocument(invocation.getParameter(0).toString()).getSource();
                Source taggedSource =
                    new SourceImpl(TestDocumentFactory.getDocument(invocation.getParameter(0).toString())
                        .getSourceWithMarkers());
                Source paramSource = (Source) invocation.getParameter(1);
                // if this function is called for the raw source, the raw rendered content is to be returned
                if (rawSource.equals(paramSource)) {
                    return TestDocumentFactory.getDocument(invocation.getParameter(0).toString()).getRenderedContent();
                }
                // if this function is called for the source with inserted annotation markers, then the markers should
                // be rendered as well
                if (taggedSource.equals(paramSource)) {
                    return TestDocumentFactory.getDocument(invocation.getParameter(0).toString())
                        .getRenderedContentWithMarkers();
                }
            }
            return null;
        }
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
