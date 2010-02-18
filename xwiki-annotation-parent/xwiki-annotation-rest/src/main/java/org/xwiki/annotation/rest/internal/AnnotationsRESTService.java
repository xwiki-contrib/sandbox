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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.rest.model.jaxb.AnnotatedContent;
import org.xwiki.annotation.rest.model.jaxb.AnnotationAddRequest;
import org.xwiki.annotation.rest.model.jaxb.AnnotationField;
import org.xwiki.annotation.rest.model.jaxb.AnnotationFieldCollection;
import org.xwiki.annotation.rest.model.jaxb.AnnotationResponse;
import org.xwiki.annotation.rest.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

/**
 * @version $Id$
 */
@Component("org.xwiki.annotation.rest.internal.AnnotationsRESTService")
@Path("/wikis/{wikiName}/spaces/{spaceName}/pages/{pageName}/annotations")
public class AnnotationsRESTService extends AbstractAnnotationService
{
    /**
     * The default action to render the document for. <br />
     * TODO: action should be obtained from the calling client in the parameters
     */
    private static final String DEFAULT_ACTION = "view";

    /**
     * Entity reference serializer used to get reference to the document to perform annotation operation on.
     */
    @Requirement
    private EntityReferenceSerializer<String> referenceSerializer;

    /**
     * @param wiki the wiki of the document to get annotations for
     * @param space the space of the document to get annotations for
     * @param page the name of the document to get annotation for
     * @param fields the extra fields to be returned from the annotation structure when returning the annotation resume
     *            to the client side
     * @return annotations of a given XWiki page. Note that we're returning a response holding the AnnotatedContent
     *         instead of an AnnotatedContent object because we need to be able to set custom expire fields to prevent
     *         IE from caching this resource.
     */
    @GET
    public Response doGetAnnotatedContent(@PathParam("spaceName") String space, @PathParam("pageName") String page,
        @PathParam("wikiName") String wiki, @QueryParam("field") List<String> fields)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            // TODO: action should be obtained from the calling client in the parameters
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, DEFAULT_ACTION);
            // TODO: return AnnotationResponse so that we can return an error here somehow
            AnnotatedContent response =
                prepareAnnotatedContent(annotationService.getAnnotations(documentName), renderedHTML, fields);
            // make this content expire now because cacheControl is not implemented in this version of restlet
            return Response.ok(response).expires(new Date()).build();
        } catch (AnnotationServiceException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    /**
     * Add annotation to a given page.
     * 
     * @param wiki the wiki of the document to add annotation on
     * @param space the space of the document to add annotation on
     * @param page the name of the document to add annotation on
     * @param request the request object with the annotation to be added
     * @return AnnotationRequestResponse, responseCode = 0 if no error
     */
    @POST
    public AnnotationResponse doPostAnnotation(@PathParam("wikiName") String wiki,
        @PathParam("spaceName") String space, @PathParam("pageName") String page, AnnotationAddRequest request)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            Map<String, Object> annotationMetadata = getMap(request);
            annotationService.addAnnotation(documentName, request.getSelection(), request.getSelectionContext(),
                request.getSelectionOffset(), getXWikiUser(), annotationMetadata);
            AnnotationResponse result = new ObjectFactory().createAnnotationResponse();
            result.setResponseCode(0);
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, DEFAULT_ACTION);
            result.setAnnotatedContent(prepareAnnotatedContent(annotationService.getAnnotations(documentName),
                renderedHTML, Collections.EMPTY_LIST));
            return result;
        } catch (AnnotationServiceException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return getErrorResponse(e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return getErrorResponse(e);
        }
    }

    /**
     * Builds a simple map from a field collection.
     * 
     * @param fields the collection of fields to build a map for
     * @return a map of the fields, as string key and Object value pair
     */
    protected Map<String, Object> getMap(AnnotationFieldCollection fields)
    {
        Map<String, Object> metadataMap = new HashMap<String, Object>();
        for (AnnotationField f : fields.getFields()) {
            metadataMap.put(f.getName(), f.getValue());
        }

        return metadataMap;
    }
}
