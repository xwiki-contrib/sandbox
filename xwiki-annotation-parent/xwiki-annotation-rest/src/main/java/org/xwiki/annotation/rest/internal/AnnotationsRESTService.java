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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.rest.internal.model.jaxb.AnnotationRequest;
import org.xwiki.annotation.rest.internal.model.jaxb.AnnotationRequestResponse;
import org.xwiki.annotation.rest.internal.model.jaxb.Annotations;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
@Component("org.xwiki.annotation.rest.internal.AnnotationsRESTService")
@Path("/wikis/{wikiName}/spaces/{spaceName}/pages/{pageName}/annotation")
public class AnnotationsRESTService extends AbstractAnnotationService
{
    /**
     * The default action to render the document for. <br />
     * TODO: action should be obtained from the calling client in the parameters
     */
    private static final String DEFAULT_ACTION = "view";

    /**
     * The execution needed to get the annotation author from the context user.
     */
    @Requirement
    private Execution execution;

    /**
     * Entity reference serializer used to get reference to the document to perform annotation operation on.
     */
    @Requirement
    private EntityReferenceSerializer<String> referenceSerializer;

    /**
     * @param wiki the wiki of the document to get annotations for
     * @param space the space of the document to get annotations for
     * @param page the name of the document to get annotation for
     * @return annotations of a given XWiki page
     */
    @GET
    public Annotations doGet(@PathParam("spaceName") String space, @PathParam("pageName") String page,
        @PathParam("wikiName") String wiki)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            // TODO: action should be obtained from the calling client in the parameters
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, DEFAULT_ACTION);
            // TODO: return AnnotationRequestResponse so that we can return an error here somehow
            return getAnnotations(annotationService.getAnnotations(documentName), renderedHTML);
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
     * @param request the request object with the annotation to be added
     * @param wiki the wiki of the document to add annotation on
     * @param space the space of the document to add annotation on
     * @param page the name of the document to add annotation on
     * @return AnnotationRequestResponse, responseCode = 0 if no error
     */
    @PUT
    public AnnotationRequestResponse doPut(AnnotationRequest request, @PathParam("wikiName") String wiki,
        @PathParam("spaceName") String space, @PathParam("pageName") String page)
    {
        try {
            DocumentReference docRef = new DocumentReference(wiki, space, page);
            String documentName = referenceSerializer.serialize(docRef);
            annotationService.addAnnotation(documentName, request.getInitialSelection(), request.getSelectionContext(),
                request.getContextOffset(), getXWikiUser(), request.getAnnotation());
            AnnotationRequestResponse result = new AnnotationRequestResponse();
            result.setResponseCode(0);
            String renderedHTML = renderDocumentWithAnnotations(documentName, null, DEFAULT_ACTION);
            result.setSource(renderedHTML);
            result.getAnnotations().addAll(getAnnotationSet(annotationService.getAnnotations(documentName)));
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
     * @return the xwiki user in the context.
     */
    protected String getXWikiUser()
    {
        return ((XWikiContext) execution.getContext().getProperty("xwikicontext")).getUser();
    }
}
