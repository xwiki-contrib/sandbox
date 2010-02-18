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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.xwiki.annotation.rest.model.jaxb.AnnotationField;
import org.xwiki.annotation.rest.model.jaxb.AnnotationFieldCollection;
import org.xwiki.annotation.rest.model.jaxb.ObjectFactory;
import org.xwiki.rest.Constants;
import org.xwiki.rest.XWikiRestComponent;

/**
 * Partial implementation of a reader from form submits requests for annotation related types, to handle generic request
 * reader code.
 * 
 * @param <T> the type read from the url encoded form
 * @version $Id$
 */
public abstract class AbstractFormUrlEncodedAnnotationReader<T extends AnnotationFieldCollection> implements
    MessageBodyReader<T>, XWikiRestComponent
{
    /**
     * Helper function to provide an instance of the read object from the object factory.
     * 
     * @param factory the object factory
     * @return an instance of the read type T, as built by the object factory.
     */
    protected abstract T getReadObjectInstance(ObjectFactory factory);

    /**
     * {@inheritDoc}
     * 
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.InputStream)
     */
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
        WebApplicationException
    {
        ObjectFactory objectFactory = new ObjectFactory();
        T annotationAddRequest = getReadObjectInstance(objectFactory);

        // parse a form from the content of this request
        Representation representation =
            new InputRepresentation(entityStream, org.restlet.data.MediaType.APPLICATION_WWW_FORM);
        Form form = new Form(representation);

        /*
         * If the form is empty then it might have happened that some filter has invalidated the entity stream. Try to
         * read data using getParameter()
         */
        if (form.getValuesMap().size() != 0) {
            for (Map.Entry<String, String> entry : ((Map<String, String>) form.getValuesMap()).entrySet()) {
                saveField(annotationAddRequest, entry.getKey(), entry.getValue(), objectFactory);
            }
        } else {
            HttpServletRequest httpServletRequest =
                (HttpServletRequest) Context.getCurrent().getAttributes().get(Constants.HTTP_REQUEST);
            for (Object entryObj : httpServletRequest.getParameterMap().entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;
                // FIXME: this needs to be done right, it can interfere with the custom parameters names
                // skip method & media parameters, used by REST to carry its own parameters
                if ("method".equals(entry.getKey()) || "media".equals(entry.getKey())) {
                    continue;
                }
                saveField(annotationAddRequest, (String) entry.getKey(), ((String[]) entry.getValue())[0],
                    objectFactory);
            }
        }

        return annotationAddRequest;
    }

    /**
     * Helper function to save a parameter in the read object. To implement in subclasses to provide type specific
     * behaviour.
     * 
     * @param readObject the request to fill with data
     * @param key the key of the field
     * @param value the value of the field
     * @param objectFactory the objects factory to create the annotation fields
     */
    protected void saveField(T readObject, String key, String value, ObjectFactory objectFactory)
    {
        AnnotationField extraField = objectFactory.createAnnotationField();
        extraField.setName(key);
        extraField.setValue(value);
        readObject.getFields().add(extraField);
    }
}
