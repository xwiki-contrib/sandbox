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

package org.xwiki.annotation;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.xwiki.annotation.maintainer.AnnotationState;

/**
 * This class wraps together the data needed to describe an annotation.
 * 
 * @version $Id$
 */
public class Annotation
{
    /**
     * The name of the field of this annotation selection. <br />
     * The text on which this annotation is added.
     */
    public static final String SELECTION_FIELD = "selection";

    /**
     * The name of the field of this annotation selection context. <br />
     * The context of the selection is used to uniquely localize an annotation on the content where is added. Or, if the
     * context appears twice, semantically speaking it shouldn't make any difference if the annotation is displayed &
     * handled in one or other of the occurrences.
     */
    public static final String SELECTION_CONTEXT_FIELD = "selectionContext";

    /**
     * The name of the field of this annotation state. <br />
     * TODO: find out if it's the right place to put the state information, as it's a maintainer particular information.
     */
    public static final String STATE_FIELD = "state";

    /**
     * The name of the field of this annotation original selection.
     */
    public static final String ORIGINAL_SELECTION_FIELD = "originalSelection";

    /**
     * The name of the field of this annotation author.
     */
    public static final String AUTHOR_FIELD = "author";

    /**
     * The name of the field of this annotation serialized date.
     */
    public static final String DATE_FIELD = "date";

    /**
     * The unique identifier of this annotation, which should be unique among all the annotations on the same target.
     */
    protected final String id;

    /**
     * The values of the fields of this annotation.
     */
    protected Map<String, Object> fields = new HashMap<String, Object>();

    /**
     * Builds an annotation description for the annotation with the passed id: used for annotation updates where only a
     * part of the fields my need to be set.
     * 
     * @param id the id of this annotation
     */
    public Annotation(String id)
    {
        this.id = id;
    }

    /**
     * Builds an annotation stub for the annotation with the passed values, to load it afterwards with the rest of the
     * data. Used when loading annotations from the storage.
     * 
     * @param id the id of this annotation
     * @param initialSelection the selected text of this annotation
     * @param selectionContext the context of the selection, which makes the selection identifiable in the content on
     *            which this annotation is added
     */
    public Annotation(String id, String initialSelection, String selectionContext)
    {
        this.id = id;
        fields.put(SELECTION_FIELD, initialSelection);
        fields.put(SELECTION_CONTEXT_FIELD, selectionContext);
        // also initialize the state of this annotation to safe
        fields.put(STATE_FIELD, AnnotationState.SAFE);
    }

    /**
     * Builds an annotation for the passed selection in the context, used to pass an annotation to be added (which does
     * not have an id yet since it hasn't been stored yet).
     * 
     * @param initialSelection the selected text of this annotation
     * @param selectionContext the context of the selection, which makes the selection identifiable in the content on
     *            which this annotation is added
     */
    public Annotation(String initialSelection, String selectionContext)
    {
        this(null, initialSelection, selectionContext);
    }

    /**
     * @return author of annotation.
     */
    public String getAuthor()
    {
        return (String) fields.get(AUTHOR_FIELD);
    }

    /**
     * Sets the author of this annotation.
     * 
     * @param author the author of this annotation.
     */
    public void setAuthor(String author)
    {
        fields.put(AUTHOR_FIELD, author);
    }

    /**
     * @return date of annotation
     */
    public Date getDate()
    {
        return (Date) fields.get(DATE_FIELD);
    }

    /**
     * @param date the serialized date to set
     */
    public void setDate(Date date)
    {
        fields.put(DATE_FIELD, date);
    }

    /**
     * @return state of annotation
     */
    public AnnotationState getState()
    {
        return (AnnotationState) fields.get(STATE_FIELD);
    }

    /**
     * @param state to set
     */
    public void setState(AnnotationState state)
    {
        fields.put(STATE_FIELD, state);
    }

    /**
     * @return selected text of this annotation
     */
    public String getSelection()
    {
        return (String) fields.get(SELECTION_FIELD);
    }

    /**
     * Sets the selection of this annotation through its context, the offset of the selection inside the context and its
     * length. Setting this value is done in this manner to make sure it's kept consistent.
     * 
     * @param selectionContext the context of the selection
     * @param selectionOffset the offset of the actual selection in the context
     * @param selectionLength the length of the selection in the context. Note that context must be at least offset +
     *            length in size, otherwise the selection will be set to the rest of the context starting from the
     *            specified offset
     */
    public void setSelection(String selectionContext, int selectionOffset, int selectionLength)
    {
        String newSelection;
        if (selectionOffset < 0 || selectionOffset > selectionContext.length()) {
            newSelection = selectionContext;
        } else if (selectionLength < 0 || selectionOffset + selectionLength > selectionContext.length()) {
            newSelection = selectionContext.substring(selectionOffset);
        } else {
            newSelection = selectionContext.substring(selectionOffset, selectionOffset + selectionLength);
        }
        fields.put(SELECTION_CONTEXT_FIELD, selectionContext);
        fields.put(SELECTION_FIELD, newSelection);
    }

    /**
     * @return selection context of annotation
     */
    public String getSelectionContext()
    {
        return (String) fields.get(SELECTION_CONTEXT_FIELD);
    }

    /**
     * @return id of annotation
     */
    public String getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "id: " + getId() + " | author: " + getAuthor() + " | selection: " + getSelection()
            + " | selection context: " + getSelectionContext();
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Annotation)) {
            return false;
        }
        Annotation other = (Annotation) obj;
        if (other.getId() != null || getId() != null) {
            // if they have ids, compare ids
            return ("" + getId()).equals(other.getId());
        } else {
            // else compare selection and selection context
            return ("" + getSelection()).equals(other.getSelection())
                && ("" + getSelectionContext()).equals(other.getSelectionContext());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (getId() != null ? getId() : getSelection() + getSelectionContext()).hashCode();
    }

    /**
     * @return the originalSelection
     */
    public String getOriginalSelection()
    {
        return (String) fields.get(ORIGINAL_SELECTION_FIELD);
    }

    /**
     * @param originalSelection the originalSelection to set
     */
    public void setOriginalSelection(String originalSelection)
    {
        fields.put(ORIGINAL_SELECTION_FIELD, originalSelection);
    }

    /**
     * @param key the key of the field to get
     * @return the value of the field
     */
    public Object get(String key)
    {
        return fields.get(key);
    }

    /**
     * Sets / adds a value in the fields of this annotation.
     * 
     * @param key the key of the field
     * @param value the value to set for the field
     * @return the old value of this field, or null if none was set
     */
    public Object set(String key, Object value)
    {
        return fields.put(key, value);
    }

    /**
     * @return a set of names of the fields set in this annotation object
     */
    public Set<String> getFieldNames()
    {
        return fields.keySet();
    }
}
