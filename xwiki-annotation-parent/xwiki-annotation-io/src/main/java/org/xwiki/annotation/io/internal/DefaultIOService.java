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
import org.xwiki.annotation.reference.TypedStringEntityReferenceResolver;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

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
     * The name of the field of the annotation object containing the length of the annotation.
     */
    private static final String DATE = "date";

    /**
     * The name of the field of the annotation object containing the reference of the content on which the annotation is
     * added.
     */
    private static final String TARGET = "target";

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
     * Entity reference handler to resolve the reference target.
     */
    @Requirement
    private TypedStringEntityReferenceResolver referenceResolver;

    /**
     * Default entity reference serializer to create document full names.
     */
    @Requirement
    private EntityReferenceSerializer<String> serializer;

    /**
     * Local entity reference serializer, to create references which are robust to import / export.
     */
    @Requirement("local")
    private EntityReferenceSerializer<String> localSerializer;

    /**
     * {@inheritDoc} <br />
     * This implementation saves the added annotation in the document where the target of the annotation is.
     * 
     * @see org.xwiki.annotation.io.IOService#addAnnotation(String, org.xwiki.annotation.Annotation)
     */
    public void addAnnotation(String target, Annotation annotation) throws IOServiceException
    {
        try {
            // extract the document name from the passed target
            // by default the fullname is the passed target
            String documentFullName = target;
            EntityReference targetReference = referenceResolver.resolve(target, EntityType.DOCUMENT);
            // try to get a document reference from the passed target reference
            EntityReference docRef = targetReference.extractReference(EntityType.DOCUMENT);
            if (docRef != null) {
                documentFullName = serializer.serialize(docRef);
            }
            // now get the document with that name
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(documentFullName, deprecatedContext);
            // create a new object in this document to hold the annotation
            int id = document.createNewObject(ANNOTATION_CLASS_NAME, deprecatedContext);
            BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, id);
            object.set(DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
            // store the target of this annotation, serialized with a local serializer, to be exportable and importable
            // in a different wiki
            // TODO: figure out if this is the best idea in terms of target serialization
            // 1/ the good part is that it is a fixed value that can be searched with a query in all objects in the wiki
            // 2/ the bad part is that copying a document to another space will not also update its annotation targets
            // 3/ if annotations are stored in the same document they annotate, the targets are only required for object
            // fields
            // ftm don't store the type of the reference since we only need to recognize the field, not to also read it.
            if (targetReference.getType() == EntityType.OBJECT_PROPERTY
                || targetReference.getType() == EntityType.DOCUMENT) {
                object.set(TARGET, localSerializer.serialize(targetReference), deprecatedContext);
            } else {
                object.set(TARGET, target, deprecatedContext);
            }
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
     * {@inheritDoc} <br />
     * This implementation retrieves all the objects of the annotation class in the document where target points to, and
     * which have the target set to {@code target}.
     * 
     * @see org.xwiki.annotation.io.IOService#getAnnotations(String)
     */
    public Collection<Annotation> getAnnotations(String target) throws IOServiceException
    {
        try {
            // parse the target and extract the local reference serialized from it, by the same rules
            EntityReference targetReference = referenceResolver.resolve(target, EntityType.DOCUMENT);
            // build the target identifier for the annotation
            String localTargetId = target;
            // and the name of the document where it should be stored
            String docName = target;
            if (targetReference.getType() == EntityType.DOCUMENT
                || targetReference.getType() == EntityType.OBJECT_PROPERTY) {
                localTargetId = localSerializer.serialize(targetReference);
                docName = serializer.serialize(targetReference.extractReference(EntityType.DOCUMENT));
            }
            // get the document
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(docName, deprecatedContext);
            // and the annotation class objects in it
            List<BaseObject> objects = document.getObjects(ANNOTATION_CLASS_NAME);
            // and build a list of Annotation objects
            List<Annotation> result = new ArrayList<Annotation>();
            if (objects == null) {
                return Collections.<Annotation> emptySet();
            }
            for (BaseObject object : objects) {
                // if it's not on the required target, ignore it
                if (object == null || !localTargetId.equals(object.getStringValue(TARGET))) {
                    continue;
                }
                // use the object number as annotation id
                Annotation annotation =
                    new Annotation(object.getStringValue(TARGET), object.getStringValue(AUTHOR), object
                        .getStringValue(ANNOTATION_TEXT), object.getStringValue(SELECTION), object
                        .getStringValue(CONTEXT), object.getNumber() + "");
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
     * {@inheritDoc} <br />
     * This implementation deletes the annotation object with the object number indicated by {@code annotationID} from
     * the document indicated by {@code target}, if its stored target matches the passed target.
     * 
     * @see org.xwiki.annotation.io.IOService#removeAnnotation(String, String)
     */
    public void removeAnnotation(String target, String annotationID) throws IOServiceException
    {
        try {
            EntityReference targetReference = referenceResolver.resolve(target, EntityType.DOCUMENT);
            // get the target identifier and the document name from the parsed reference
            String localTargetId = target;
            String docName = target;
            if (targetReference.getType() == EntityType.DOCUMENT
                || targetReference.getType() == EntityType.OBJECT_PROPERTY) {
                localTargetId = localSerializer.serialize(targetReference);
                docName = serializer.serialize(targetReference.extractReference(EntityType.DOCUMENT));
            }
            // get the document
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(docName, deprecatedContext);
            if (document.isNew()) {
                // if the document doesn't exist already skip it
                return;
            }
            // and the document object on it
            BaseObject annotationObject =
                document.getObject(ANNOTATION_CLASS_NAME, Integer.valueOf(annotationID.toString()));

            // if object exists and its target matches the requested target, delete it
            if (annotationObject != null && localTargetId.equals(annotationObject.getStringValue(TARGET))) {
                document.removeObject(annotationObject);
                deprecatedContext.getWiki().saveDocument(document, "Deleted annotation " + annotationID,
                    deprecatedContext);
            }
        } catch (NumberFormatException e) {
            throw new IOServiceException("An exception has occurred while parsing the annotation id", e);
        } catch (XWikiException e) {
            throw new IOServiceException("An exception has occurred while removing the annotation", e);
        }
    }

    /**
     * {@inheritDoc} <br />
     * Implementation which gets all the annotation class objects in the document pointed by the target, and matches
     * their ids against the ids in the passed collection of annotations. If they match, they are updated with the new
     * data in the annotations in annotation.
     * 
     * @see org.xwiki.annotation.io.IOService#updateAnnotations(String, java.util.Collection)
     */
    public void updateAnnotations(String target, Collection<Annotation> annotations) throws IOServiceException
    {
        try {
            EntityReference targetReference = referenceResolver.resolve(target, EntityType.DOCUMENT);
            // get the document name from the parsed reference
            String docName = target;
            if (targetReference.getType() == EntityType.DOCUMENT
                || targetReference.getType() == EntityType.OBJECT_PROPERTY) {
                docName = serializer.serialize(targetReference.extractReference(EntityType.DOCUMENT));
            }
            // get the document pointed to by the target
            XWikiContext deprecatedContext = getXWikiContext();
            XWikiDocument document = deprecatedContext.getWiki().getDocument(docName, deprecatedContext);
            boolean updated = false;
            for (Annotation annotation : annotations) {
                // parse annotation id as string. If cannot parse, then ignore annotation, is not valid
                int annId = 0;
                try {
                    annId = Integer.parseInt(annotation.getId());
                } catch (NumberFormatException e) {
                    continue;
                }
                BaseObject object = document.getObject(ANNOTATION_CLASS_NAME, annId);
                if (object == null) {
                    continue;
                }
                updated = true;
                // should we check the target or update it?
                object.set(DATE, new SimpleDateFormat(DATE_FORMAT).format(new Date()), deprecatedContext);
                object.set(AUTHOR, annotation.getAuthor(), deprecatedContext);
                object.set(STATE, annotation.getState().name(), deprecatedContext);
                object.set(ANNOTATION_TEXT, annotation.getAnnotation(), deprecatedContext);
                object.set(SELECTION, annotation.getSelection(), deprecatedContext);
                if (annotation.getOriginalSelection() != null) {
                    object.set(ORIGINAL_SELECTION, annotation.getOriginalSelection(), deprecatedContext);
                }
                object.set(CONTEXT, annotation.getSelectionContext(), deprecatedContext);
            }
            if (updated) {
                deprecatedContext.getWiki().saveDocument(document, "Updated annotations", deprecatedContext);
            }
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
