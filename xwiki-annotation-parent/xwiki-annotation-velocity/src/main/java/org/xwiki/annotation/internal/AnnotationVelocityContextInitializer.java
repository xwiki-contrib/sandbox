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
package org.xwiki.annotation.internal;

import org.apache.velocity.VelocityContext;
import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.rights.AnnotationRightService;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.velocity.VelocityContextInitializer;

/**
 * Velocity context initializer used to add the annotations service on the velocity context.
 * 
 * @version $Id$
 */
@Component("annotations")
public class AnnotationVelocityContextInitializer implements VelocityContextInitializer
{
    /**
     * The key to add to the velocity context.
     */
    public static final String VELOCITY_CONTEXT_KEY = "annotations";

    /**
     * The annotations service instance to wrap with a velocity bridge and add in the context.
     */
    @Requirement
    private AnnotationService annotationService;

    /**
     * The annotations rights checking service, to add access restrictions to the annotations service.
     */
    @Requirement
    private AnnotationRightService annotationRightService;

    /**
     * The current execution, to get this execution's context.
     */
    @Requirement
    private Execution execution;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.velocity.VelocityContextInitializer#initialize(org.apache.velocity.VelocityContext)
     */
    public void initialize(VelocityContext context)
    {
        // create a wrapper of the annotation service for exposing its methods in velocity
        AnnotationVelocityBridge bridge =
            new AnnotationVelocityBridge(annotationService, annotationRightService, execution);
        context.put(VELOCITY_CONTEXT_KEY, bridge);
    }
}
