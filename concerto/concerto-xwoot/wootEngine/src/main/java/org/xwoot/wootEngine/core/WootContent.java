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
package org.xwoot.wootEngine.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles a list of WootRow elements describing a content.
 * 
 * @version $Id$
 */
public class WootContent implements Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -7726342430092268721L;

    /** List of all the rows contained by this content. */
    private List<WootRow> rows = new ArrayList<WootRow>();

    /** id of the described content. */
    private ContentId contentId;

    /**
     * Creates a new WootContent object. A wootContent is a sub content of a {@link WootFile}. A {@link WootFile} is
     * represented by a pageName.
     * 
     * @param addStartEndMakers whether to add or not the default first and last WootRow.
     */
    public WootContent(boolean addStartEndMakers)
    {
        if (addStartEndMakers) {
            this.getRows().add(WootRow.FIRST_WOOT_ROW);
            this.getRows().add(WootRow.LAST_WOOT_ROW);
        }
    }

    /**
     * Creates a new WootContent instance, adding the default start and end WootRows.
     * 
     * @param contentId the id of the newly content.
     * @throws IllegalArgumentException if the name is not valid.
     * @see #WootContent(boolean)
     */
    public WootContent(ContentId contentId) throws IllegalArgumentException
    {
        this(true);
        this.setContentId(contentId);
    }

    /**
     * @param wootRow the row to check for.
     * @return true if the content contains the row.
     */
    public boolean contains(WootRow wootRow)
    {
        return this.rows.contains(wootRow);
    }

    /**
     * @param id the ID of the row to check for.
     * @return true if the content contains the WootRow identified by the specified ID.
     */
    public boolean containsById(WootId id)
    {
        return this.indexOfId(id) >= 0;
    }

    /**
     * @param index the index to check for relative to all the rows in the content, visible or not.
     * @return the wootRow at the specified index.
     */
    public WootRow elementAt(int index)
    {
        return this.rows.get(index);
    }

    /**
     * @param visiblePosition the position, relative only to visible rows.
     * @return the visible row at the specified position.
     */
    public WootRow visibleElementAt(int visiblePosition)
    {
        return this.elementAt(indexOfVisible(visiblePosition));
    }

    /**
     * @param wootRow the wootRow to check.
     * @return the index of the wootRow relative to all the rows in the content or -1 if the row is not found.
     */
    public int indexOf(WootRow wootRow)
    {
        return this.getRows().indexOf(wootRow) - 1;
    }

    /**
     * Equivalent to {@link #indexOfIdAfter(int, WootId) indexOfIdFrom(-1, id)}.
     * 
     * @param id the id to look for.
     * @return the index, relative to all the rows, of the WootRow object having the specified id, relative to all the
     *         rows in the content, or -1 if it does not exist.
     */
    public int indexOfId(WootId id)
    {
        return indexOfIdAfter(-1, id);
    }

    /**
     * @param after the index, relative to all the rows, after which to start looking.
     * @param id the id of the row to look for.
     * @return the index, relative to all the rows, of the row having the specified id or -1 if it is not found.
     */
    public int indexOfIdAfter(int after, WootId id)
    {
        for (int i = after + 1; i < this.rows.size(); ++i) {
            if (this.rows.get(i).getWootId().equals(id)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @param index relative only to the visible rows.
     * @return the index relative to all the rows, visible or not.
     */
    public int indexOfVisible(int index)
    {
        if ((index >= this.getRows().size()) || (index < 0)) {
            return -1;
        }

        int visibleIndex = 0;
        int actualIndex = 0;

        for (WootRow row : this.rows) {
            if (row.isVisible()) {
                if (visibleIndex == index) {
                    return actualIndex;
                }

                visibleIndex++;
            }

            actualIndex++;
        }

        return -1;
    }

    /**
     * @param after the index, relative to all the rows, after which to look.
     * @return the index of the first visible row.
     */
    public int indexOfVisibleNext(int after)
    {
        int n = this.rows.size();

        if ((after < 0) || (after >= n)) {
            return -1;
        }

        for (int i = after + 1; i < n; ++i) {
            if (this.rows.get(i).isVisible()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @param wootRow the row to insert.
     * @param afterPosition the position, relative to all the rows, after which to insert the row.
     */
    public void insert(WootRow wootRow, int afterPosition)
    {
        this.rows.add(afterPosition + 1, wootRow);
    }

    /**
     * @return true if there are no rows, except from the default ones, in the content.
     */
    public boolean isEmpty()
    {
        return this.size() == 0;
    }

    /**
     * @return the number of rows in the content, excluding the default ones.
     */
    public int size()
    {
        return this.rows.size() - 2;
    }

    /**
     * @return the number of visible rows in the content, excluding the default ones.
     */
    public int sizeOfVisible()
    {
        int result = 0;
        for (WootRow row : getRows()) {
            if (row.isVisible()) {
                result++;
            }
        }

        return result - 2;
    }

    /**
     * @return a list of all the rows contained by this content.
     */
    public List<WootRow> getRows()
    {
        return this.rows;
    }

    /**
     * @param rows the rows to set.
     * @throws NullPointerException if rows is null.
     * @see #getRows()
     */
    public void setRows(List<WootRow> rows) throws NullPointerException
    {
        if (rows == null) {
            throw new NullPointerException("Null rows are not allowed.");
        }

        this.rows = rows;
    }

    /**
     * @return the content of the visible rows, each on a new line.
     */
    public String toHumanString()
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 1; i < (this.rows.size() - 1); i++) {
            WootRow r = this.rows.get(i);

            if (r.isVisible()) {
                sb.append(r.getContent());
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * @return the content of all the rows on this content.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        Iterator<WootRow> i = this.getRows().iterator();

        while (i.hasNext()) {
            WootRow r = i.next();
            sb.append(r.getContent());
        }

        return sb.toString();
    }

    /**
     * @return the content of all the visible rows in this content, including the default rows.
     */
    public String toVisibleString()
    {
        StringBuffer sb = new StringBuffer();

        for (WootRow row : this.getRows()) {
            if (row.isVisible()) {
                sb.append(row.getContent());
            }
        }

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if ((o == null) || (o.getClass() != this.getClass())) {
            return false;
        }

        WootContent other = (WootContent) o;

        return (this.contentId.equals(other.getContentId()) && this.rows.equals(other.getRows()));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (this.contentId == null ? 0 : this.contentId.hashCode());
        hash = 31 * hash + (this.rows == null ? 0 : this.rows.hashCode());
        return hash;
    }

    /**
     * @return the content Id.
     */
    public ContentId getContentId()
    {
        return this.contentId;
    }

    /**
     * @param contentId the content Id to set.
     */
    public void setContentId(ContentId contentId)
    {
        this.contentId = contentId;
    }
}
