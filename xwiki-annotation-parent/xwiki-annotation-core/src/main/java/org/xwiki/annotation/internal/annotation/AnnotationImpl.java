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

package org.xwiki.annotation.internal.annotation;

import org.xwiki.annotation.internal.maintainment.AnnotationState;

/**
 * Default implementation of {@link Annotation} interface.
 * 
 * @version $Id$
 */
public class AnnotationImpl implements Annotation
{
    protected final CharSequence page;

    protected final CharSequence author;

    protected final CharSequence date;

    protected AnnotationState state;

    protected final CharSequence annotation;

    protected final CharSequence initialSelection;

    protected final CharSequence selectionContext;

    protected final int id;

    protected int offset;

    protected int length;

    /**
     * @param page
     * @param author
     * @param date
     * @param state
     * @param annotation
     * @param initialSelection
     * @param selectionContext
     * @param id
     * @param offset
     * @param length
     */
    public AnnotationImpl(CharSequence page, CharSequence author, CharSequence date, AnnotationState state,
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
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getPage()
     */
    public CharSequence getPage()
    {
        return page;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getAuthor()
     */
    public CharSequence getAuthor()
    {
        return author;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getDate()
     */
    public CharSequence getDate()
    {
        return date;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getState()
     */
    public AnnotationState getState()
    {
        return state;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#setState(org.xwiki.annotation.internal.maintainment.AnnotationState)
     */
    public void setState(AnnotationState state)
    {
        this.state = state;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getAnnotation()
     */
    public CharSequence getAnnotation()
    {
        return annotation;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getInitialSelection()
     */
    public CharSequence getInitialSelection()
    {
        return initialSelection;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getSelectionContext()
     */
    public CharSequence getSelectionContext()
    {
        return selectionContext;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getId()
     */
    public int getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getOffset()
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#setOffset(int)
     */
    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.annotation.Annotation#getLength()
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
}
