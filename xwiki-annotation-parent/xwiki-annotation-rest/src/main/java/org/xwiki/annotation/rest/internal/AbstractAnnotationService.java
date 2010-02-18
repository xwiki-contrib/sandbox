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
import org.xwiki.annotation.rest.model.jaxb.AnnotatedContent;
import org.xwiki.annotation.rest.model.jaxb.AnnotationField;
import org.xwiki.annotation.rest.model.jaxb.AnnotationResponse;
import org.xwiki.annotation.rest.model.jaxb.AnnotationStub;
import org.xwiki.annotation.rest.model.jaxb.ObjectFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
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
     * The default action to render the document for. <br />
     * TODO: action should be obtained from the calling client in the parameters
     */
    protected static final String DEFAULT_ACTION = "view";

    /**
     * The annotations service to be used by this REST interface.
     */
    @Requirement
    protected AnnotationService annotationService;

    /**
     * The execution needed to get the annotation author from the context user.
     */
    @Requirement
    protected Execution execution;

    /**
     * Helper function to translate a collection of annotations from the {@link org.xwiki.annotation.Annotation} model
     * to the JAXB model to be serialized for REST communication.
     * 
     * @param annotations the annotations collection to be translated
     * @param fields the extra parameters that should be set for the prepared annotations
     * @return translate set of org.xwiki.annotation.internal.annotation.Annotation to set of
     *         org.xwiki.annotation.internal.annotation.Annotation
     */
    protected Collection<AnnotationStub> prepareAnnotationStubsSet(
        Collection<org.xwiki.annotation.Annotation> annotations, List<String> fields)
    {
        ObjectFactory factory = new ObjectFactory();
        List<AnnotationStub> set = new ArrayList<AnnotationStub>();
        for (org.xwiki.annotation.Annotation xwikiAnnotation : annotations) {
            AnnotationStub annotation = factory.createAnnotationStub();
            annotation.setAnnotationId(xwikiAnnotation.getId());
            annotation.setState(xwikiAnnotation.getState().toString());
            // for all the requested extra fields, get them from the annotation and send them
            for (String extraField : fields) {
                Object value = xwikiAnnotation.get(extraField);
                AnnotationField field = new AnnotationField();
                field.setName(extraField);
                // value.toString() by default, null if value is missing
                field.setValue(value != null ? value.toString() : null);
                annotation.getFields().add(field);
            }
            set.add(annotation);
        }
        return set;
    }

    /**
     * Helper function to build an {@link AnnotatedContent} object from a collection of annotations of type
     * {@link org.xwiki.annotation.Annotation} and the rendered html, to the JAXB model to be serialized for REST
     * communication. <br />
     * TODO: make all callers of this function pass custom parameters, such as color, from client request
     * 
     * @param annotations the list of annotations to be transformed for serialization
     * @param htmlContent the rendered content of the document to be packed with the collection of annotations
     * @param fields the fields to return from the annotations stubs
     * @return wrapped set of annotation, annotated and rendered content
     */
    protected AnnotatedContent prepareAnnotatedContent(Collection<org.xwiki.annotation.Annotation> annotations,
        String htmlContent, List<String> fields)
    {
        ObjectFactory factory = new ObjectFactory();
        AnnotatedContent result = factory.createAnnotatedContent();
        result.getAnnotations().addAll(prepareAnnotationStubsSet(annotations, fields));
        result.setContent(htmlContent);
        return result;
    }

    /**
     * Helper function to create an error response from a passed exception. <br />
     * 
     * @param exception the exception that was encountered during regular execution of service
     * @return an error response
     */
    protected AnnotationResponse getErrorResponse(Throwable exception)
    {
        AnnotationResponse result = new ObjectFactory().createAnnotationResponse();
        result.setResponseCode(1);
        String responseMessage = exception.getMessage();
        if (responseMessage == null) {
            // serialize the stack trace and send it as an error response
            StringWriter stackTraceWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stackTraceWriter));
            responseMessage = stackTraceWriter.toString();
        }
        result.setResponseMessage(responseMessage);
        result.setAnnotatedContent(null);
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

    /**
     * @return the xwiki user in the context.
     */
    protected String getXWikiUser()
    {
        return ((XWikiContext) execution.getContext().getProperty("xwikicontext")).getUser();
    }
}
