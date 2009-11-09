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

import org.jmock.Mockery;
import org.xwiki.annotation.AnnotationTarget;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.IOTargetService;
import org.xwiki.annotation.TestDocumentFactory;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Abstract super class for all test classes testing the target services.
 * 
 * @version $Id$
 */
public abstract class AbstractTargetTest extends AbstractComponentTestCase
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
     * Annotation target component tested by this suite.
     */
    protected AnnotationTarget annotationTarget;

    /**
     * Mock document tested in this suite.
     */
    protected String docName;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();
        // IOTargetService mockup
        ioTargetService = mockery.mock(IOTargetService.class);
        DefaultComponentDescriptor<IOTargetService> iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        iotsDesc.setRoleHint("FEEDENTRY");
        getComponentManager().registerComponent(iotsDesc, ioTargetService);
        iotsDesc = new DefaultComponentDescriptor<IOTargetService>();
        iotsDesc.setRole(IOTargetService.class);
        getComponentManager().registerComponent(iotsDesc, ioTargetService);

        // IOService mockup
        ioService = mockery.mock(IOService.class);
        DefaultComponentDescriptor<IOService> ioDesc = new DefaultComponentDescriptor<IOService>();
        ioDesc.setRole(IOService.class);
        getComponentManager().registerComponent(ioDesc, ioService);
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
        // reset mock documents from corpus factory
        TestDocumentFactory.reset();
    }
}
