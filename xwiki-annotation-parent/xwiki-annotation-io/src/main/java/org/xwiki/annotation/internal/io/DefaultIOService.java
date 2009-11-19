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

package org.xwiki.annotation.internal.io;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.maintainment.AnnotationState;
import org.xwiki.component.annotation.Component;

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
    static final String ANNOTATION_CLASS_NAME = "XWiki.AnnotationClass";

    /**
     * The date format used to store the annotation date in the XWiki objects.
     */
    static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#addAnnotation(java.lang.CharSequence, org.xwiki.annotation.Annotation,
     *      com.xpn.xwiki.XWikiContext)
     */
    public void addAnnotation(CharSequence documentName, Annotation annotation, XWikiContext deprecatedContext)
        throws IOServiceException
    {
        try {
            XWikiDocument document =
                deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
            synchronized (documentName) {
                int id = document.createNewObject(ANNOTATION_CLASS_NAME, deprecatedContext);
                BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, id);
                object.set("length", annotation.getLength(), deprecatedContext);
                object.set("offset", annotation.getOffset(), deprecatedContext);
                object.set("annotationID", Integer.valueOf(id), deprecatedContext);
                object.set("date", new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
                object.set("pageID", document.getName(), deprecatedContext);
                object.set("author", annotation.getAuthor(), deprecatedContext);
                object.set("state", AnnotationState.SAFE.name(), deprecatedContext);
                object.set("annotation", annotation.getAnnotation(), deprecatedContext);
                object.set("initialSelection", annotation.getInitialSelection(), deprecatedContext);
                object.set("selectionContext", annotation.getSelectionContext(), deprecatedContext);
                deprecatedContext.getWiki().saveDocument(document, deprecatedContext);
            }
        } catch (XWikiException e) {
            throw new IOServiceException("An exception message has occurred while saving the annotation", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#getAnnotations(java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public Collection<Annotation> getAnnotations(CharSequence documentName, XWikiContext deprecatedContext)
        throws IOServiceException
    {
        try {
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
                    new Annotation(it.get("pageID").toString(), it.getStringValue("author"), it.getStringValue("date"),
                        AnnotationState.forName(it.getStringValue("state")), it.getStringValue("annotation"), it
                            .getStringValue("initialSelection"), it.getStringValue("selectionContext"), it
                            .getIntValue("annotationID"), it.getIntValue("offset"), it.getIntValue("length"));
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
     * @see org.xwiki.annotation.io.IOService#getSafeAnnotations(java.lang.CharSequence, com.xpn.xwiki.XWikiContext)
     */
    public Collection<Annotation> getSafeAnnotations(CharSequence documentName, XWikiContext deprecatedContext)
        throws IOServiceException
    {
        List<Annotation> result = new ArrayList<Annotation>();
        for (Annotation it : getAnnotations(documentName, deprecatedContext)) {
            if (it.getState().equals(AnnotationState.SAFE)) {
                result.add(it);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#removeAnnotation(java.lang.CharSequence, java.lang.CharSequence,
     *      com.xpn.xwiki.XWikiContext)
     */
    public void removeAnnotation(CharSequence documentName, CharSequence annotationID, XWikiContext deprecatedContext)
        throws IOServiceException
    {
        try {
            synchronized (documentName) {
                XWikiDocument document =
                    deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
                document.removeObject(document.getObject(ANNOTATION_CLASS_NAME, Integer
                    .valueOf(annotationID.toString())));
                deprecatedContext.getWiki().saveDocument(document, deprecatedContext);
            }
        } catch (NumberFormatException e) {
            throw new IOServiceException("An exception has occurred while parsing the annotation to remove", e);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while removing the annotation", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOService#updateAnnotations(java.lang.CharSequence, java.util.Collection,
     *      com.xpn.xwiki.XWikiContext)
     */
    public void updateAnnotations(CharSequence documentName, Collection<Annotation> annotations,
        XWikiContext deprecatedContext) throws IOServiceException
    {
        try {
            synchronized (documentName) {
                XWikiDocument document =
                    deprecatedContext.getWiki().getDocument(documentName.toString(), deprecatedContext);
                for (Annotation it : annotations) {
                    BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, it.getId());
                    object.set("length", Integer.valueOf(it.getLength()), deprecatedContext);
                    object.set("offset", Integer.valueOf(it.getOffset()), deprecatedContext);
                    object.set("annotationID", Integer.valueOf(it.getId()), deprecatedContext);
                    object.set("date", new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
                    object.set("pageID", document.getName(), deprecatedContext);
                    object.set("author", it.getAuthor(), deprecatedContext);
                    object.set("state", it.getState().name(), deprecatedContext);
                    object.set("annotation", it.getAnnotation(), deprecatedContext);
                    object.set("initialSelection", it.getInitialSelection(), deprecatedContext);
                    object.set("selectionContext", it.getSelectionContext(), deprecatedContext);
                }
                deprecatedContext.getWiki().saveDocument(document, deprecatedContext);
            }
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while updating the annotation", e);
        }
    }
}
