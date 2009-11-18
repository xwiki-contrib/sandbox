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

import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.rest.internal.model.jaxb.AnnotationRequest;
import org.xwiki.annotation.rest.internal.model.jaxb.AnnotationRequestResponse;
import org.xwiki.annotation.rest.internal.model.jaxb.Annotations;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Component("org.xwiki.annotation.rest.internal.AnnotationsRESTService")
@Path("/wikis/{wikiName}/spaces/{spaceName}/pages/{pageName}/annotation")
public class AnnotationsRESTService extends AbstractAnnotationService
{
    @Requirement
    private AnnotationService annotationService;

    /**
     * @param space concerned
     * @param page concerned
     * @param wiki concerned
     * @return annotations of a given XWiki page
     */
    @GET
    public Annotations doGet(@PathParam("spaceName") String space, @PathParam("pageName") String page,
        @PathParam("wikiName") String wiki)
    {
        try {
            DocumentInfo docInfo = getDocumentInfo(wiki, space, page, null, null, true, true);
            String documentName = docInfo.getDocument().getFullName();
            return getAnnotations(annotationService.getAnnotations(documentName, xwikiContext), annotationService
                .getAnnotatedHTML(documentName, xwikiContext));
        } catch (XWikiException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return null;
        } catch (AnnotationServiceException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return null;
        }
    }

    /**
     * Add annotation to a given page.
     * 
     * @param t contains annotation information
     * @param wiki concerned
     * @param space concerned
     * @param page concerned
     * @return AnnotationRequestResponse, responseCode = 0 if no error
     */
    @PUT
    public AnnotationRequestResponse doPut(AnnotationRequest t, @PathParam("wikiName") String wiki,
        @PathParam("spaceName") String space, @PathParam("pageName") String page)
    {
        try {
            DocumentInfo docInfo = getDocumentInfo(wiki, space, page, null, null, true, true);
            String documentName = docInfo.getDocument().getFullName();
            annotationService.addAnnotation(t.getAnnotation(), t.getInitialSelection(), t.getSelectionContext(), t
                .getContextOffset(), documentName, xwikiUser, xwikiContext);
            AnnotationRequestResponse result = new AnnotationRequestResponse();
            result.setResponseCode(0);
            result.setSource(annotationService.getAnnotatedHTML(documentName, xwikiContext).toString());
            result.getAnnotations().addAll(
                getAnnotationSet(annotationService.getAnnotations(documentName, xwikiContext)));
            return result;
        } catch (XWikiException e) {
            logger.log(Level.SEVERE, e.getMessage());
            AnnotationRequestResponse result = new AnnotationRequestResponse();
            result.setResponseCode(1);
            return result;
        } catch (AnnotationServiceException e) {
            logger.log(Level.SEVERE, e.getMessage());
            AnnotationRequestResponse result = new AnnotationRequestResponse();
            result.setResponseCode(1);
            return result;
        }
    }
}
