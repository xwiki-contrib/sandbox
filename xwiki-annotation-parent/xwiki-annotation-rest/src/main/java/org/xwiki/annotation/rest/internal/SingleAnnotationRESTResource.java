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

import java.util.logging.Level;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.xwiki.annotation.rest.internal.model.jaxb.AnnotationRequestResponse;
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
    public AnnotationRequestResponse doDelete(@PathParam("spaceName") String space, @PathParam("pageName") String page,
        @PathParam("wikiName") String wiki, @PathParam("id") String id)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            annotationService.removeAnnotation(documentName, id);

            AnnotationRequestResponse result = new AnnotationRequestResponse();
            result.setResponseCode(0);
            // TODO: action should be obtained from the calling client in the parameters
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, "view");
            result.setSource(renderedHTML);
            result.getAnnotations().addAll(getAnnotationSet(annotationService.getAnnotations(documentName)));
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return getErrorResponse(e);
        }
    }
}
