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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.xwiki.annotation.rest.model.jaxb.AnnotationAddRequest;
import org.xwiki.annotation.rest.model.jaxb.AnnotationField;
import org.xwiki.annotation.rest.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.rest.Constants;
import org.xwiki.rest.XWikiRestComponent;

/**
 * Reads an annotation add request from a request encoded as a form submit.
 * 
 * @version $Id$
 */
@Component("org.xwiki.annotation.rest.internal.FormUrlEncodedAnnotationAddRequestReader")
@Provider
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class FormUrlEncodedAnnotationAddRequestReader implements MessageBodyReader<AnnotationAddRequest>,
    XWikiRestComponent
{
    // TODO: should send selection, contextLeft and contextRight
    /**
     * Name of the selection context field submitted by a form.
     */
    private static final String SELECTION_CONTEXT_FIELD_NAME = "selectionContext";

    /**
     * Name of the field holding the selection offset in context submitted by a form.
     */
    private static final String SELECTION_OFFSET_FIELD_NAME = "selectionOffset";

    /**
     * Name of the selection context field submitted by a form.
     */
    private static final String SELECTION_FIELD_NAME = "selection";

    /**
     * {@inheritDoc}
     * 
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
     */
    public boolean isReadable(Class< ? > type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return AnnotationAddRequest.class.isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     * 
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.InputStream)
     */
    public AnnotationAddRequest readFrom(Class<AnnotationAddRequest> type, Type genericType, Annotation[] annotations,
        MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
        WebApplicationException
    {
        ObjectFactory objectFactory = new ObjectFactory();
        AnnotationAddRequest annotationAddRequest = objectFactory.createAnnotationAddRequest();

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
     * Helper function to save a parameter in the annotation add request.
     * 
     * @param annotationAddRequest the request to fill with data
     * @param key the key of the field
     * @param value the value of the field
     * @param objectFactory the objects factory to create the annotation fields
     */
    private void saveField(AnnotationAddRequest annotationAddRequest, String key, String value,
        ObjectFactory objectFactory)
    {
        // check this key against the 'known fields'
        if (SELECTION_FIELD_NAME.equals(key)) {
            annotationAddRequest.setSelection(value);
            return;
        }
        if (SELECTION_CONTEXT_FIELD_NAME.equals(key)) {
            annotationAddRequest.setSelectionContext(value);
            return;
        }
        if (SELECTION_OFFSET_FIELD_NAME.equals(key)) {
            // use the parameter as a string and parse it as an integer
            int offset = 0;
            try {
                offset = Integer.parseInt(value);
            } catch (NumberFormatException exc) {
                // nothing, will leave the 0 value
            }
            annotationAddRequest.setSelectionOffset(offset);
            return;
        }
        // if none matched, add as extra field
        AnnotationField extraField = objectFactory.createAnnotationField();
        extraField.setName(key);
        extraField.setValue(value);
        annotationAddRequest.getFields().add(extraField);
    }
}
