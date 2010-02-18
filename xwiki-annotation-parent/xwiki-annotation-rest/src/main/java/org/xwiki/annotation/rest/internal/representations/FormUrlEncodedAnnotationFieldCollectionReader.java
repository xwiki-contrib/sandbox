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
package org.xwiki.annotation.rest.internal.representations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.xwiki.annotation.rest.model.jaxb.AnnotationFieldCollection;
import org.xwiki.annotation.rest.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Component;

/**
 * Implementation of the form url encoded reader for the annotation update requests.
 * 
 * @version $Id$
 */
@Component("org.xwiki.annotation.rest.internal.representations.FormUrlEncodedAnnotationFieldCollectionReader")
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class FormUrlEncodedAnnotationFieldCollectionReader extends
    AbstractFormUrlEncodedAnnotationReader<AnnotationFieldCollection>
{
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.rest.internal.representations.AbstractFormUrlEncodedAnnotationReader
     *      #getReadObjectInstance(org.xwiki.annotation.rest.model.jaxb.ObjectFactory)
     */
    @Override
    protected AnnotationFieldCollection getReadObjectInstance(ObjectFactory factory)
    {
        return factory.createAnnotationFieldCollection();
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    public boolean isReadable(Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        // this reader will only read annotationAddRequests, and none of the superclasses. Superclasses will read
        // themselves
        return type.equals(AnnotationFieldCollection.class);
    }
}
