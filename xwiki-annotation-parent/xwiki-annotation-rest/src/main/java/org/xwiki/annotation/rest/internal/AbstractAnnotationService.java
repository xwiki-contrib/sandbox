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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.rest.internal.model.jaxb.Annotation;
import org.xwiki.annotation.rest.internal.model.jaxb.AnnotationRequestResponse;
import org.xwiki.annotation.rest.internal.model.jaxb.Annotations;
import org.xwiki.annotation.rest.internal.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.rest.XWikiResource;
import org.xwiki.velocity.VelocityManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * @version $Id$
 */
public abstract class AbstractAnnotationService extends XWikiResource
{
    /**
     * The annotations service to be used by this REST interface.
     */
    @Requirement
    protected AnnotationService annotationService;

    /**
     * Helper function to translate a collection of annotations from the {@link org.xwiki.annotation.Annotation} model
     * to the JAXB model to be serialized for REST communication.
     * 
     * @param annotations the annotations collection to be translated
     * @return translate set of org.xwiki.annotation.internal.annotation.Annotation to set of
     *         org.xwiki.annotation.internal.annotation.Annotation
     */
    protected Collection<Annotation> getAnnotationSet(Collection<org.xwiki.annotation.Annotation> annotations)
    {
        ObjectFactory factory = new ObjectFactory();
        List<Annotation> set = new ArrayList<Annotation>();
        for (org.xwiki.annotation.Annotation xwikiAnnotation : annotations) {
            Annotation annotation = factory.createAnnotation();
            annotation.setAnnotation(xwikiAnnotation.getAnnotation());
            annotation.setAnnotationId(xwikiAnnotation.getId());
            annotation.setAuthor(xwikiAnnotation.getAuthor());
            annotation.setDate(xwikiAnnotation.getDisplayDate());
            annotation.setInitialSelection(xwikiAnnotation.getSelection());
            annotation.setTarget(xwikiAnnotation.getTarget());
            annotation.setSelectionContext(xwikiAnnotation.getSelectionContext());
            annotation.setState(xwikiAnnotation.getState().toString());
            set.add(annotation);
        }
        return set;
    }

    /**
     * Helper function to build an {@link Annotations} object from a collection of annotations of type
     * {@link org.xwiki.annotation.Annotation} and the rendered html, to the JAXB model to be serialized for REST
     * communication.
     * 
     * @param annotations the list of annotations to be transformed for serialization
     * @param htmlContent the rendered content of the document to be packed with the collection of annotations
     * @return wrapped set of annotation, annotated and rendered content
     */
    protected Annotations getAnnotations(Collection<org.xwiki.annotation.Annotation> annotations,
        String htmlContent)
    {
        ObjectFactory factory = new ObjectFactory();
        Annotations result = factory.createAnnotations();
        result.getAnnotations().addAll(getAnnotationSet(annotations));
        result.setSource(htmlContent);
        return result;
    }

    /**
     * Helper function to create an error response from a passed exception. <br />
     * 
     * @param exception the exception that was encountered during regular execution of service
     * @return an error response
     */
    protected AnnotationRequestResponse getErrorResponse(Throwable exception)
    {
        AnnotationRequestResponse result = new AnnotationRequestResponse();
        result.setResponseCode(1);
        String responseMessage = exception.getMessage();
        if (responseMessage == null) {
            // serialize the stack trace and send it as an error response
            StringWriter stackTraceWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stackTraceWriter));
            responseMessage = stackTraceWriter.toString();
        }
        result.setResponseMessage(responseMessage);
        result.setSource(null);
        return result;
    }

    /**
     * Helper function to get the rendered content of the document with annotations. All setup of context for rendering
     * content similar to the rendering on standard view will be done in this function. <br />
     * FIXME: find out if this whole context setup code has to be here or in the annotations service
     * 
     * @param docName the name of the document to render
     * @param language the language in which to render the document
     * @param action the context action to render the document for
     * @return the HTML rendered content of the document
     * @throws Exception in case anything wrong happens while rendering the document
     */
    protected String renderDocumentWithAnnotations(String docName, String language, String action) throws Exception
    {
        String isInRenderingEngineKey = "isInRenderingEngine";
        XWikiContext context = org.xwiki.rest.Utils.getXWikiContext(componentManager);
        Object isInRenderingEngine = context.get(isInRenderingEngineKey);
        String result = null;
        try {
            setUpDocuments(docName, language);
            // set the current action on the context
            context.setAction(action);
            context.put(isInRenderingEngineKey, true);
            result = annotationService.getAnnotatedHTML(docName);
        } finally {
            if (isInRenderingEngine != null) {
                context.put(isInRenderingEngineKey, isInRenderingEngine);
            } else {
                context.remove(isInRenderingEngineKey);
            }
        }
        return result;
    }

    /**
     * Helper function to prepare the XWiki documents and translations on the context and velocity context. <br />
     * TODO: check how this code could be written only once (not duplicate the prepareDocuments function in XWiki)
     * 
     * @param docName the full name of the document to prepare context for
     * @param language the language of the document
     * @throws Exception if anything goes wrong accessing documents
     */
    protected void setUpDocuments(String docName, String language) throws Exception
    {
        VelocityManager velocityManager = componentManager.lookup(VelocityManager.class);
        VelocityContext vcontext = velocityManager.getVelocityContext();

        XWikiContext context = org.xwiki.rest.Utils.getXWikiContext(componentManager);
        XWiki xwiki = context.getWiki();

        // prepare the messaging tools and set them on context
        xwiki.prepareResources(context);

        XWikiDocument doc = xwiki.getDocument(docName, context);
        // setup the xwiki context and the velocity context
        String docKey = "doc";
        context.put(docKey, doc);
        vcontext.put(docKey, doc.newDocument(context));
        vcontext.put("cdoc", vcontext.get(docKey));
        XWikiDocument tdoc = doc.getTranslatedDocument(language, context);
        String translatedDocKey = "tdoc";
        context.put(translatedDocKey, tdoc);
        vcontext.put(translatedDocKey, tdoc.newDocument(context));
    }
}
