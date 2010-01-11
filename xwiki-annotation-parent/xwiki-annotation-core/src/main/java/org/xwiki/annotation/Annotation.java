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
    protected final String page;

    protected final String author;

    protected final String displayDate;

    protected AnnotationState state;

    protected final String annotation;

    protected String initialSelection;

    protected String selectionContext;

    protected final int id;

    protected int offset;

    protected int length;

    /**
     * @param page the document where the annotation is added
     * @param author the author of the annotation
     * @param date the date of the annotation
     * @param state the state of the annotation, whether it was safely updated upon further edits of the document or not
     * @param annotation the text of the annotation
     * @param initialSelection the initial selection of the annotation
     * @param selectionContext the context of the annotation selection
     * @param id the id of the annotation
     * @param offset the offset of the annotation inside the context
     * @param length the length of the selection of this annotation
     */
    public Annotation(String page, String author, String date, AnnotationState state, String annotation,
        String initialSelection, String selectionContext, int id, int offset, int length)
    {
        this.page = page;
        this.author = author;
        this.displayDate = date;
        this.state = state;
        this.annotation = annotation;
        this.initialSelection = initialSelection;
        this.selectionContext = selectionContext;
        this.id = id;
        this.offset = offset;
        this.length = length;
    }

    /**
     * @return page of annotation.
     */
    public String getPage()
    {
        return page;
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
    public String getInitialSelection()
    {
        return initialSelection;
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
        String selection;
        if (selectionOffset < 0 || selectionOffset > selectionContext.length()) {
            selection = selectionContext;
        } else if (selectionLength < 0 || selectionOffset + selectionLength > selectionContext.length()) {
            selection = selectionContext.substring(selectionOffset);
        } else {
            selection = selectionContext.substring(selectionOffset, selectionOffset + selectionLength);
        }
        this.selectionContext = selectionContext;
        this.initialSelection = selection;
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
    public int getId()
    {
        return id;
    }

    /**
     * @return offset of annotation's selection
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * modify offset of annotation's selection.
     * 
     * @param offset to set
     */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    /**
     * @return length of annotation's selection.
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
        return "annotation : " + getAnnotation() + " | author : " + getAuthor() + " | offset : " + getOffset()
            + " | length : " + getLength();
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
        return (other.getAnnotation() == getAnnotation() && other.getAuthor() == getAuthor()
            && other.getOffset() == getOffset() && other.getLength() == getLength());
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (getAnnotation() + getAuthor() + Integer.toString(getOffset()) + Integer.toString(getLength()))
            .hashCode();
    }
}
