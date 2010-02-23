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
package org.xwiki.annotation.internal;

import java.util.Collection;
import java.util.Map;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.rights.AnnotationRightService;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Wrapper for the annotation service functions to be exposed to the velocity context with rights checking, and velocity
 * accessible exception handling.
 * 
 * @version $Id$
 */
public class AnnotationVelocityBridge
{
    /**
     * The annotation service to execute annotation functions.
     */
    private AnnotationService annotationService;

    /**
     * The annotations rights service.
     */
    private AnnotationRightService rightsService;

    /**
     * The execution to get the context.
     */
    private Execution execution;

    /**
     * Builds a velocity bridge wrapper for the passed annotation service, with the passed rights checking service and
     * the specified execution context.
     * 
     * @param annotationService the annotation service to wrap
     * @param annotationRightService the annotations rights service
     * @param execution the execution context
     */
    public AnnotationVelocityBridge(AnnotationService annotationService, AnnotationRightService annotationRightService,
        Execution execution)
    {
        this.annotationService = annotationService;
        this.rightsService = annotationRightService;
        this.execution = execution;
    }

    /**
     * Adds an the specified annotation for the specified target.
     * 
     * @param target serialized reference of the target of the annotation
     * @param selection HTML selection concerned by annotations
     * @param selectionContext HTML selection context
     * @param offset offset of the selection in context
     * @param author the author of the annotation
     * @param metadata annotation metadata, as key, value pairs
     * @return {@code true} if the adding succeeds, {@code false} if an exception occurs and the exception is saved on
     *         the xwiki context
     */
    public boolean addAnnotation(String target, String selection, String selectionContext, int offset, String author,
        Map<String, Object> metadata)
    {
        if (!rightsService.canAddAnnotation(target, getCurrentUser())) {
            setAccessExceptionOnContext();
            return false;
        }
        try {
            annotationService.addAnnotation(target, selection, selectionContext, offset, author, metadata);
            return true;
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return false;
        }
    }

    /**
     * Returns the XHTML of the requested source, along with annotations inserted as {@code span} elements inside it.
     * It's a particular case of {@link #getAnnotatedRenderedContent(String, String, String)} for unspecified input
     * syntax and {@code xhtml/1.0} output syntax.
     * 
     * @param sourceReference reference to the source to be rendered in XHTML with annotations
     * @return rendered and annotated document or {@code null} if an exception occurs and the exception is saved on the
     *         xwiki context
     * @see #getAnnotatedRenderedContent(String, String, String)
     * @see AnnotationService#getAnnotatedHTML(String)
     */
    public String getAnnotatedHTML(String sourceReference)
    {
        if (!rightsService.canViewAnnotatedTarget(sourceReference, getCurrentUser())) {
            setAccessExceptionOnContext();
            return null;
        }
        try {
            return annotationService.getAnnotatedHTML(sourceReference);
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return null;
        }
    }

    /**
     * Returns result obtained by rendering with annotations the source referenced by the {@code sourceReference},
     * parsed in {@code sourceSyntax}.
     * 
     * @param sourceReference the reference to the source to be rendered in XHTML with annotations
     * @param sourceSyntax the syntax to parse the source in. If this parameter is null, the default source syntax will
     *            be used, as returned by the target IO service.
     * @param outputSyntax the syntax to render in (e.g. "xhtml/1.0")
     * @return the annotated rendered source, or @code null} if an exception occurs and the exception is saved on the
     *         xwiki context
     * @see AnnotationService#getAnnotatedRenderedContent(String, String, String)
     */
    public String getAnnotatedRenderedContent(String sourceReference, String sourceSyntax, String outputSyntax)
    {
        if (!rightsService.canViewAnnotatedTarget(sourceReference, getCurrentUser())) {
            setAccessExceptionOnContext();
            return null;
        }
        try {
            return annotationService.getAnnotatedRenderedContent(sourceReference, sourceSyntax, outputSyntax);
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return null;
        }
    }

    /**
     * Returns the annotation identified by {@code id} on the specified target.
     * 
     * @param target the serialized reference to the content on which the annotation is added
     * @param id the identifier of the annotation
     * @return the annotation identified by {@code id}, or {@code null} if an exception occurs and the exception is
     *         saved on the xwiki context
     * @see AnnotationService#getAnnotation(String, String)
     */
    public Annotation getAnnotation(String target, String id)
    {
        if (!rightsService.canViewAnnotations(target, getCurrentUser())) {
            setAccessExceptionOnContext();
            return null;
        }
        try {
            return annotationService.getAnnotation(target, id);
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return null;
        }
    }

    /**
     * Returns all the annotations on the passed content.
     * 
     * @param target the string serialized reference to the content for which to get the annotations
     * @return all annotations which target the specified content, or {@code null} if an exception occurs and the
     *         exception is saved on the xwiki context
     * @see AnnotationService#getAnnotations(String)
     */
    public Collection<Annotation> getAnnotations(String target)
    {
        if (!rightsService.canViewAnnotations(target, getCurrentUser())) {
            setAccessExceptionOnContext();
            return null;
        }
        try {
            return annotationService.getAnnotations(target);
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return null;
        }
    }

    /**
     * Shortcut function to get all annotations which are valid on the specified target, regardless of the updates the
     * document and its annotations suffered from creation ('safe' or 'updated' state).
     * 
     * @param target the string serialized reference to the content for which to get the annotations
     * @return all annotations which are valid on the specified content, or {@code null} if an exception occurs and the
     *         exception is saved on the xwiki context
     * @see {@link org.xwiki.annotation.maintainer.AnnotationState}
     * @see AnnotationService#getValidAnnotations(String)
     */
    public Collection<Annotation> getValidAnnotations(String target)
    {
        if (!rightsService.canViewAnnotations(target, getCurrentUser())) {
            setAccessExceptionOnContext();
            return null;
        }
        try {
            return annotationService.getValidAnnotations(target);
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return null;
        }
    }

    /**
     * Remove an annotation given by its identifier, which should be unique among all annotations on the same target.
     * 
     * @param target the string serialized reference to the content on which the annotation is added
     * @param annotationID annotation identifier
     * @return {@code true} if removing succeeds, {@code false} if an exception occurs and the exception is saved on the
     *         xwiki context
     * @see AnnotationService#removeAnnotation(String, String)
     */
    public boolean removeAnnotation(String target, String annotationID)
    {
        if (!rightsService.canRemoveAnnotation(annotationID, target, getCurrentUser())) {
            setAccessExceptionOnContext();
            return false;
        }
        try {
            annotationService.removeAnnotation(target, annotationID);
            return true;
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return false;
        }
    }

    /**
     * Updates the passed annotation with the new values. Matching of the annotation is done by the annotation id field,
     * among all annotations on the same target.
     * 
     * @param target the string serialized reference to the content on which the annotation is added
     * @param annotation the new description of the annotation to update, with a valid id
     * @return {@code true} if update succeeds, {@code false} if an exception occurs and the exception is saved on the
     *         xwiki context
     * @see AnnotationService#updateAnnotation(String, Annotation)
     */
    public boolean updateAnnotation(String target, Annotation annotation)
    {
        if (!rightsService.canEditAnnotation(annotation.getId(), target, getCurrentUser())) {
            setAccessExceptionOnContext();
            return false;
        }
        try {
            annotationService.updateAnnotation(target, annotation);
            return true;
        } catch (AnnotationServiceException e) {
            setExceptionOnContext(e);
            return false;
        }
    }

    /**
     * Helper function to get the currently logged in user.
     * 
     * @return the currently logged in user
     */
    private String getCurrentUser()
    {
        return getXWikiContext().getUser();
    }

    /**
     * Helper function to get the XWiki Context.
     * 
     * @return the xwiki context
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }

    /**
     * Helper function to set an exception on the xwiki context, as the last exception.
     * 
     * @param exception the exception to set on the context
     */
    private void setExceptionOnContext(Exception exception)
    {
        getXWikiContext().put("lastexception", exception);
    }

    /**
     * Helper function to set an access exception on the xwiki context, as the last exception.
     */
    private void setAccessExceptionOnContext()
    {
        setExceptionOnContext(new XWikiException(XWikiException.MODULE_XWIKI_ACCESS,
            XWikiException.ERROR_XWIKI_ACCESS_DENIED, "You are not allowed to perform this action"));
    }
}
