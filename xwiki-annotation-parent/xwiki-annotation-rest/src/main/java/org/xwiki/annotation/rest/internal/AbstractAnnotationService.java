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

package org.xwiki.annotation.rest.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xwiki.annotation.AnnotationMaintainer;
import org.xwiki.annotation.rest.internal.model.jaxb.Annotation;
import org.xwiki.annotation.rest.internal.model.jaxb.Annotations;
import org.xwiki.annotation.rest.internal.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rest.XWikiResource;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationService extends XWikiResource implements Initializable
{
    private static boolean maintainerRegistered;

    @Requirement("1d7dde8b-3480-4eca-ab68-12f3fd4b4ab3")
    private static AnnotationMaintainer annotationMaintainer;

    @Requirement
    private static ObservationManager observationManager;

    /**
     * @param annotations
     * @return translate set of org.xwiki.annotation.internal.annotation.Annotation to set of
     *         org.xwiki.annotation.internal.annotation.Annotation
     */
    protected Collection<Annotation> getAnnotationSet(
        Collection<org.xwiki.annotation.internal.annotation.Annotation> annotations)
    {
        ObjectFactory factory = new ObjectFactory();
        List<Annotation> set = new ArrayList<Annotation>();
        for (org.xwiki.annotation.internal.annotation.Annotation xwikiAnnotation : annotations) {
            Annotation annotation = factory.createAnnotation();
            annotation.setAnnotation(xwikiAnnotation.getAnnotation().toString());
            annotation.setAnnotationId(xwikiAnnotation.getId());
            annotation.setAuthor(xwikiAnnotation.getAuthor().toString());
            annotation.setDate(xwikiAnnotation.getDate().toString());
            annotation.setInitialSelection(xwikiAnnotation.getInitialSelection().toString());
            annotation.setLength(xwikiAnnotation.getLength());
            annotation.setOffset(xwikiAnnotation.getOffset());
            annotation.setPageId(xwikiAnnotation.getPage().toString());
            annotation.setSelectionContext(xwikiAnnotation.getSelectionContext().toString());
            annotation.setState(xwikiAnnotation.getState().toString());
            set.add(annotation);
        }
        return set;
    }

    /**
     * AnnotationManager component must listen document updates. <br />
     * TODO: delete it and test.
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public synchronized void initialize() throws InitializationException
    {
        super.initialize();
        if (!maintainerRegistered) {
            observationManager.addListener(annotationMaintainer);
            maintainerRegistered = !maintainerRegistered;
        }
    }

    /**
     * @param annotations
     * @param htmlContent
     * @return wrapped set of annotation, annotated and rendered content
     */
    protected Annotations getAnnotations(Collection<org.xwiki.annotation.internal.annotation.Annotation> annotations,
        CharSequence htmlContent)
    {
        ObjectFactory factory = new ObjectFactory();
        Annotations result = factory.createAnnotations();
        result.getAnnotations().addAll(getAnnotationSet(annotations));
        result.setSource(htmlContent.toString());
        return result;
    }
}
