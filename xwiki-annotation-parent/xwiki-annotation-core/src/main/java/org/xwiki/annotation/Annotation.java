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

import org.xwiki.annotation.maintainment.AnnotationState;

/**
 * This class wraps together the data needed to describe an annotation.
 * 
 * @version $Id$
 */
public class Annotation
{
    protected final CharSequence page;

    protected final CharSequence author;

    protected final Date date;

    protected AnnotationState state;

    protected final CharSequence annotation;

    protected final CharSequence initialSelection;

    protected final CharSequence selectionContext;

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
    public Annotation(CharSequence page, CharSequence author, Date date, AnnotationState state,
        CharSequence annotation, CharSequence initialSelection, CharSequence selectionContext, int id, int offset,
        int length)
    {
        this.page = page;
        this.author = author;
        this.date = date;
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
    public CharSequence getPage()
    {
        return page;
    }

    /**
     * @return author of annotation.
     */
    public CharSequence getAuthor()
    {
        return author;
    }

    /**
     * @return date of annotation
     */
    public Date getDate()
    {
        return date;
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
    public CharSequence getAnnotation()
    {
        return annotation;
    }

    /**
     * @return initial selection of selection
     */
    public CharSequence getInitialSelection()
    {
        return initialSelection;
    }

    /**
     * @return selection context of annotation
     */
    public CharSequence getSelectionContext()
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
        return (getAnnotation().toString() + getAuthor().toString() + Integer.toString(getOffset()) + Integer
            .toString(getLength())).hashCode();
    }
}
