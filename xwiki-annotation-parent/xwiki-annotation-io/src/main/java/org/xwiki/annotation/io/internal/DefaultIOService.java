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

package org.xwiki.annotation.io.internal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.maintainer.AnnotationState;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default {@link IOService} implementation, based on storing annotations in XWiki Objects in XWiki documents. The
 * targets manipulated by this implementation are XWiki references, such as xwiki:Space.Page for documents or with an
 * object and property reference if the target is an object property. Use the reference module to generate the
 * references passed to this module, so that they can be resolved to XWiki content back by this implementation.
 * 
 * @version $Id$
 */
@Component
public class DefaultIOService implements IOService
{
    /**
     * The XWiki class of the stored annotation objects.
     */
    private static final String ANNOTATION_CLASS_NAME = "XWiki.AnnotationClass";

    /**
     * The name of the field of the annotation object containing the id of the annotation.
     */
    private static final String ANNOTATION_ID = "annotationID";

    /**
     * The name of the field of the annotation object containing the length of the annotation.
     */
    private static final String DATE = "date";

    /**
     * The name of the field of the annotation object containing the reference of the content on which the annotation is
     * added.
     */
    private static final String TARGET_ID = "pageID";

    /**
     * The name of the field of the annotation object containing the author of the annotation.
     */
    private static final String AUTHOR = "author";

    /**
     * The name of the field of the annotation object containing the state of the annotation.
     * 
     * @see AnnotationState
     */
    private static final String STATE = "state";

    /**
     * The name of the field of the annotation object containing the text of the annotation, the actual user inserted
     * text to annotate the selected content.
     */
    private static final String ANNOTATION_TEXT = "annotation";

    /**
     * The name of the field of the annotation object containing the selected text of the annotation.
     */
    private static final String SELECTION = "selection";

    /**
     * The name of the field of the annotation object containing the original selected text of the annotation, for the
     * annotations that were updated.
     */
    private static final String ORIGINAL_SELECTION = "originalSelection";

    /**
     * The name of the field of the annotation object containing the context of selected text of the annotation, that
     * makes it unique inside the document.
     */
    private static final String CONTEXT = "selectionContext";

    /**
     * The date format used to store the annotation date in the XWiki objects.
     */
    private static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    /**
     * The execution used to get the deprecated XWikiContext.
     */
    @Requirement
    private Execution execution;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#addAnnotation(String, org.xwiki.annotation.Annotation)
     */
    public void addAnnotation(String target, Annotation annotation) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(target, deprecatedContext);
            int id = document.createNewObject(ANNOTATION_CLASS_NAME, deprecatedContext);
            BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, id);
            // FIXME: why exactly are we storing the ID since the ID is the object index?, and we interpret it as such
            object.set(ANNOTATION_ID, Integer.valueOf(id), deprecatedContext);
            object.set(DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
            object.set(TARGET_ID, document.getName(), deprecatedContext);
            object.set(AUTHOR, annotation.getAuthor(), deprecatedContext);
            object.set(STATE, AnnotationState.SAFE.name(), deprecatedContext);
            object.set(ANNOTATION_TEXT, annotation.getAnnotation(), deprecatedContext);
            object.set(SELECTION, annotation.getSelection(), deprecatedContext);
            object.set(CONTEXT, annotation.getSelectionContext(), deprecatedContext);
            if (annotation.getOriginalSelection() != null) {
                object.set(ORIGINAL_SELECTION, annotation.getOriginalSelection(), deprecatedContext);
            }
            deprecatedContext.getWiki().saveDocument(document,
                "Added annotation \"" + annotation.getAnnotation() + "\"", deprecatedContext);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception message has occurred while saving the annotation", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#getAnnotations(String)
     */
    public Collection<Annotation> getAnnotations(String target) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(target, deprecatedContext);
            List<BaseObject> objects = document.getObjects(ANNOTATION_CLASS_NAME);

            List<Annotation> result = new ArrayList<Annotation>();
            if (objects == null) {
                return Collections.<Annotation> emptySet();
            }
            for (BaseObject object : objects) {
                if (object == null) {
                    continue;
                }
                Annotation annotation =
                    new Annotation(object.getStringValue(TARGET_ID), object.getStringValue(AUTHOR), object
                        .getStringValue(ANNOTATION_TEXT), object.getStringValue(SELECTION), object
                        .getStringValue(CONTEXT), object.getStringValue(ANNOTATION_ID));
                annotation.setDisplayDate(object.getStringValue(DATE));
                annotation.setState(AnnotationState.valueOf(object.getStringValue(STATE)));
                String originalSelection = object.getStringValue(ORIGINAL_SELECTION);
                if (originalSelection != null && originalSelection.length() > 0) {
                    annotation.setOriginalSelection(originalSelection);
                }
                result.add(annotation);
            }
            return result;
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while loading the annotations", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#getValidAnnotations(String)
     */
    public Collection<Annotation> getValidAnnotations(String target) throws IOServiceException
    {
        List<Annotation> result = new ArrayList<Annotation>();
        for (Annotation it : getAnnotations(target)) {
            if (it.getState() == AnnotationState.SAFE || it.getState() == AnnotationState.UPDATED) {
                result.add(it);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#removeAnnotation(String, String)
     */
    public void removeAnnotation(String target, String annotationID) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(target, deprecatedContext);
            document.removeObject(document.getObject(ANNOTATION_CLASS_NAME, Integer.valueOf(annotationID.toString())));
            deprecatedContext.getWiki().saveDocument(document, "Deleted annotation " + annotationID, deprecatedContext);
        } catch (NumberFormatException e) {
            throw new IOServiceException("An exception has occurred while parsing the annotation to remove", e);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while removing the annotation", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#updateAnnotations(String, java.util.Collection)
     */
    public void updateAnnotations(String target, Collection<Annotation> annotations) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(target, deprecatedContext);
            for (Annotation annotation : annotations) {
                // parse annotation id as string. If cannot parse, then ignore it
                // TODO: add a decent unique identifier. Potentially look it up with a query?
                int annId = 0;
                try {
                    annId = Integer.parseInt(annotation.getId());
                } catch (NumberFormatException e) {
                    continue;
                }
                BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, annId);
                // FIXME: why exactly are we storing the ID since the ID is the object index?, and we interpret it as
                // such
                object.set(ANNOTATION_ID, Integer.valueOf(annotation.getId()), deprecatedContext);
                object.set(DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
                object.set(TARGET_ID, document.getName(), deprecatedContext);
                object.set(AUTHOR, annotation.getAuthor(), deprecatedContext);
                object.set(STATE, annotation.getState().name(), deprecatedContext);
                object.set(ANNOTATION_TEXT, annotation.getAnnotation(), deprecatedContext);
                object.set(SELECTION, annotation.getSelection(), deprecatedContext);
                if (annotation.getOriginalSelection() != null) {
                    object.set(ORIGINAL_SELECTION, annotation.getOriginalSelection(), deprecatedContext);
                }
                object.set(CONTEXT, annotation.getSelectionContext(), deprecatedContext);
            }
            deprecatedContext.getWiki().saveDocument(document, "Updated annotations", deprecatedContext);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while updating the annotation", e);
        }
    }

    /**
     * @return the deprecated xwiki context used to manipulate xwiki objects
     */
    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
