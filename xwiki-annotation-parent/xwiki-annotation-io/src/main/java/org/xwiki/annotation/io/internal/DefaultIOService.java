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
 * Default {@link IOService} implementation.
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
     * The name of the field of the annotation object containing the length of the annotation.
     */
    private static final String LENGTH = "length";

    /**
     * The name of the field of the annotation object containing the offset of the annotation in the source.
     */
    private static final String OFFSET = "offset";

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
    private static final String SELECTION = "initialSelection";

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
    public void addAnnotation(String documentName, Annotation annotation) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            int id = document.createNewObject(ANNOTATION_CLASS_NAME, deprecatedContext);
            BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, id);
            object.set(LENGTH, annotation.getLength(), deprecatedContext);
            object.set(OFFSET, annotation.getOffset(), deprecatedContext);
            object.set(ANNOTATION_ID, Integer.valueOf(id), deprecatedContext);
            object.set(DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
            object.set(TARGET_ID, document.getName(), deprecatedContext);
            object.set(AUTHOR, annotation.getAuthor(), deprecatedContext);
            object.set(STATE, AnnotationState.SAFE.name(), deprecatedContext);
            object.set(ANNOTATION_TEXT, annotation.getAnnotation(), deprecatedContext);
            object.set(SELECTION, annotation.getInitialSelection(), deprecatedContext);
            object.set(CONTEXT, annotation.getSelectionContext(), deprecatedContext);
            deprecatedContext.getWiki().saveDocument(document, deprecatedContext);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception message has occurred while saving the annotation", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#getAnnotations(String)
     */
    public Collection<Annotation> getAnnotations(String documentName) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            List<BaseObject> objects = document.getObjects(ANNOTATION_CLASS_NAME);

            List<Annotation> result = new ArrayList<Annotation>();
            if (objects == null) {
                return Collections.<Annotation> emptySet();
            }
            for (BaseObject it : objects) {
                if (it == null) {
                    continue;
                }
                Annotation annotation =
                    new Annotation(it.get(TARGET_ID).toString(), it.getStringValue(AUTHOR), it.getStringValue(DATE),
                        AnnotationState.valueOf(it.getStringValue(STATE)), it.getStringValue(ANNOTATION_TEXT), it
                            .getStringValue(SELECTION), it.getStringValue(CONTEXT), it.getIntValue(ANNOTATION_ID), it
                            .getIntValue(OFFSET), it.getIntValue(LENGTH));
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
     * @see org.xwiki.annotation.io.IOService#getSafeAnnotations(String)
     */
    public Collection<Annotation> getSafeAnnotations(String documentName) throws IOServiceException
    {
        List<Annotation> result = new ArrayList<Annotation>();
        for (Annotation it : getAnnotations(documentName)) {
            if (it.getState().equals(AnnotationState.SAFE)) {
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
    public void removeAnnotation(String documentName, String annotationID) throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            document.removeObject(document.getObject(ANNOTATION_CLASS_NAME, Integer.valueOf(annotationID.toString())));
            deprecatedContext.getWiki().saveDocument(document, deprecatedContext);
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
    public void updateAnnotations(String documentName, Collection<Annotation> annotations)
        throws IOServiceException
    {
        try {
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            for (Annotation it : annotations) {
                BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, it.getId());
                object.set(LENGTH, Integer.valueOf(it.getLength()), deprecatedContext);
                object.set(OFFSET, Integer.valueOf(it.getOffset()), deprecatedContext);
                object.set(ANNOTATION_ID, Integer.valueOf(it.getId()), deprecatedContext);
                object.set(DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
                object.set(TARGET_ID, document.getName(), deprecatedContext);
                object.set(AUTHOR, it.getAuthor(), deprecatedContext);
                object.set(STATE, it.getState().name(), deprecatedContext);
                object.set(ANNOTATION_TEXT, it.getAnnotation(), deprecatedContext);
                object.set(SELECTION, it.getInitialSelection(), deprecatedContext);
                object.set(CONTEXT, it.getSelectionContext(), deprecatedContext);
            }
            deprecatedContext.getWiki().saveDocument(document, deprecatedContext);
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
