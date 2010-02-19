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
import java.util.logging.Level;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.rest.model.jaxb.AnnotationField;
import org.xwiki.annotation.rest.model.jaxb.AnnotationFieldCollection;
import org.xwiki.annotation.rest.model.jaxb.AnnotationResponse;
import org.xwiki.annotation.rest.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

/**
 * This class allow to do delete a single annotation.
 * 
 * @version $Id$
 */
@Component("org.xwiki.annotation.rest.internal.SingleAnnotationRESTResource")
@Path("/wikis/{wikiName}/spaces/{spaceName}/pages/{pageName}/annotation/{id}")
public class SingleAnnotationRESTResource extends AbstractAnnotationService
{
    /**
     * Entity reference serializer used to get reference to the document to perform annotation operation on.
     */
    @Requirement
    private EntityReferenceSerializer<String> referenceSerializer;

    /**
     * Deletes the specified annotation.
     * 
     * @param space the space of the document to delete the annotation from
     * @param page the name of the document to delete the annotation from
     * @param wiki the wiki of the document to delete the annotation from
     * @param id the id of the annotation to delete
     * @return a annotation response for which the response code will be 0 in case of success and non-zero otherwise
     */
    @DELETE
    public AnnotationResponse doDelete(@PathParam("spaceName") String space, @PathParam("pageName") String page,
        @PathParam("wikiName") String wiki, @PathParam("id") String id)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            annotationService.removeAnnotation(documentName, id);

            AnnotationResponse result = new ObjectFactory().createAnnotationResponse();
            result.setResponseCode(0);
            // TODO: action should be obtained from the calling client in the parameters
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, DEFAULT_ACTION);
            result.setAnnotatedContent(prepareAnnotatedContent(annotationService.getAnnotations(documentName),
                renderedHTML, Collections.<String> emptyList()));
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return getErrorResponse(e);
        }
    }

    /**
     * Updates the specified annotation with the values of the fields in received collection.
     * 
     * @param space the space of the document to update the annotation from
     * @param page the name of the document to update the annotation from
     * @param wiki the wiki of the document to update the annotation from
     * @param id the id of the annotation to update
     * @param fields the pairs of name value to update the annotation data
     * @return a annotation response for which the response code will be 0 in case of success and non-zero otherwise
     */
    @PUT
    public AnnotationResponse doUpdate(@PathParam("spaceName") String space, @PathParam("pageName") String page,
        @PathParam("wikiName") String wiki, @PathParam("id") String id, AnnotationFieldCollection fields)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            // id from the url
            Annotation newAnnotation = new Annotation(id);
            // fields from the posted content
            for (AnnotationField field : fields.getFields()) {
                newAnnotation.set(field.getName(), field.getValue());
            }
            // overwrite author if any was set because we know better who's logged in
            newAnnotation.setAuthor(getXWikiUser());
            // and update
            annotationService.updateAnnotation(documentName, newAnnotation);

            AnnotationResponse result = new ObjectFactory().createAnnotationResponse();
            result.setResponseCode(0);
            // TODO: action should be obtained from the calling client in the parameters
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, DEFAULT_ACTION);
            result.setAnnotatedContent(prepareAnnotatedContent(annotationService.getAnnotations(documentName),
                renderedHTML, Collections.<String> emptyList()));
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return getErrorResponse(e);
        }
    }
}
