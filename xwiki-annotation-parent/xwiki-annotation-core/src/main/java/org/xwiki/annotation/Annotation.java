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

import org.xwiki.annotation.maintainer.AnnotationState;

/**
 * This class wraps together the data needed to describe an annotation.
 * 
 * @version $Id$
 */
public class Annotation
{
    /**
     * The target on which this annotation is added, the target content of the annotation. <br />
     * TODO: is this really needed here?
     */
    protected final String target;

    /**
     * The username of the author of this annotation, e.g. xwiki:XWiki.Admin.
     */
    protected final String author;

    /**
     * The addition date to display for this annotation.
     */
    protected String displayDate;

    /**
     * The metadata associated with this annotation.
     */
    protected String annotation;

    /**
     * The text on which this annotation is added.
     */
    protected String selection;

    /**
     * The context of the selection of this annotation. Should be used to uniquely localize an annotation on the content
     * where is added. Or, if the context appears twice, semantically speaking it shouldn't make any difference if the
     * annotation is displayed & handled in one or other of the occurrences.
     */
    protected String selectionContext;

    /**
     * The state of this annotation, with respect to the content evolution. <br />
     * TODO: find out if it's the right place to put this, as it's a maintainer particular information.
     */
    protected AnnotationState state;

    /**
     * The original selection of this annotation, if it's an automatically updated annotation (its state is
     * {@link AnnotationState#UPDATED}). <br />
     * TODO: find out if it's the right place to put this, as it's a maintainer particular information.
     */
    protected String originalSelection;

    /**
     * The unique identifier of this annotation, which should be unique among all the annotations on the same target.
     */
    protected final String id;

    /**
     * The offset of this annotation in the content on which it is added.
     * 
     * @deprecated this field doesn't hold correct information any longer, selection and selectionContext should be used
     *             to map the annotation on the source if needed
     */
    protected int offset = -1;

    /**
     * The length of this annotation in the content on which it is added.
     * 
     * @deprecated this field doesn't hold correct information any longer, selection and selectionContext should be used
     *             to map the annotation on the source if needed
     */
    protected int length = -1;

    /**
     * @param target the document where the annotation is added
     * @param author the author of the annotation
     * @param date the date of the annotation
     * @param state the state of the annotation, whether it was safely updated upon further edits of the document or not
     * @param annotation the text of the annotation
     * @param initialSelection the initial selection of the annotation
     * @param selectionContext the context of the annotation selection
     * @param id the id of the annotation
     * @param offset the offset of the annotation inside the context
     * @param length the length of the selection of this annotation
     * @deprecated use the {@link Annotation#Annotation(String, String, String, String, String, String)} constructor
     */
    public Annotation(String target, String author, String date, AnnotationState state, String annotation,
        String initialSelection, String selectionContext, String id, int offset, int length)
    {
        this.target = target;
        this.author = author;
        this.displayDate = date;
        this.state = state;
        this.annotation = annotation;
        this.selection = initialSelection;
        this.selectionContext = selectionContext;
        this.id = id;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Build an annotation from the selection and selection context with invalid values for the offset and length which
     * are deprecated.
     * 
     * @param target the document where the annotation is added
     * @param author the author of the annotation
     * @param annotation the text of the annotation
     * @param initialSelection the initial selection of the annotation
     * @param selectionContext the context of the annotation selection
     * @param id the id of the annotation
     */
    public Annotation(String target, String author, String annotation, String initialSelection,
        String selectionContext, String id)
    {
        this.target = target;
        this.author = author;
        this.annotation = annotation;
        this.selection = initialSelection;
        this.selectionContext = selectionContext;
        this.id = id;
        this.state = AnnotationState.SAFE;
        this.displayDate = "";
    }

    /**
     * @return target of annotation.
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * @return author of annotation.
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @return date of annotation
     */
    public String getDisplayDate()
    {
        return displayDate;
    }

    /**
     * @return state of annotation
     */
    public AnnotationState getState()
    {
        return state;
    }

    /**
     * @param state to set
     */
    public void setState(AnnotationState state)
    {
        this.state = state;
    }

    /**
     * @return annotation content.
     */
    public String getAnnotation()
    {
        return annotation;
    }

    /**
     * @return initial selection of selection
     */
    public String getSelection()
    {
        return selection;
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
        this.selectionContext = selectionContext;
        this.selection = newSelection;
    }

    /**
     * @return selection context of annotation
     */
    public String getSelectionContext()
    {
        return selectionContext;
    }

    /**
     * @return id of annotation
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return offset of annotation's selection
     * @deprecated this will be removed, don't use it. Use the selection and selection context of the annotation to
     *             compute offset if needed
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * modify offset of annotation's selection.
     * 
     * @param offset to set
     * @deprecated this will be removed, don't use it. Use the selection and selection context of the annotation to
     *             compute offset if needed
     */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    /**
     * @return length of annotation's selection.
     * @deprecated this will be removed, don't use it. Use the selection and selection context of the annotation to
     *             compute length if needed
     */
    public int getLength()
    {
        return length;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "annotation: " + getAnnotation() + " | author: " + getAuthor() + " | selection: " + getSelection()
            + " | selection context: " + getSelectionContext();
    }

    /**
     * {@inheritDoc} <br />
     * TODO: fix the implementation of the equals function, it should test id, author, target and selection or smth, not
     * the offsets since they are totally irrelevant now.
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
        // compare serializations, it's easier to test nulls
        return ("" + getAnnotation() + " " + getAuthor() + " " + getSelectionContext()).equals(""
            + other.getAnnotation() + " " + other.getAuthor() + " " + other.getSelectionContext());
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (getAnnotation() + getAuthor() + getSelectionContext()).hashCode();
    }

    /**
     * @param displayDate the displayDate to set
     */
    public void setDisplayDate(String displayDate)
    {
        this.displayDate = displayDate;
    }

    /**
     * @return the originalSelection
     */
    public String getOriginalSelection()
    {
        return originalSelection;
    }

    /**
     * @param originalSelection the originalSelection to set
     */
    public void setOriginalSelection(String originalSelection)
    {
        this.originalSelection = originalSelection;
    }
}
