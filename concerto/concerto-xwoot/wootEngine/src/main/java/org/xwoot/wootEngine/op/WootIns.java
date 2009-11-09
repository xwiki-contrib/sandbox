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
package org.xwoot.wootEngine.op;

import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootContent;
import org.xwoot.wootEngine.core.WootRow;

import java.io.Serializable;

/**
 * Provides the Woot operation "Insert". It is able to insert a WootRow in a content between two other rows.
 * 
 * @version $Id$
 */
public class WootIns extends AbstractWootOp implements Serializable
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = -4385801023236126147L;

    /** The new row that will be inserted in the content. */
    private WootRow rowToInsert;

    /** The id of the row located before the position where the new row will be inserted. */
    private WootId idOfPreviousRow;

    /** The id of the row locate after the position where the new row will be inserted. */
    private WootId idOfNextRow;

    /**
     * Creates a new WootIns object.
     * 
     * @param rowToInsert the new row that will be inserted in the content.
     * @param idOfPreviousRow the id of the row positioned before the newly inserted row.
     * @param idOfNextRow the id of the row positioned after the newly inserted row.
     */
    public WootIns(WootRow rowToInsert, WootId idOfPreviousRow, WootId idOfNextRow)
    {
        this.idOfPreviousRow = idOfPreviousRow;
        this.idOfNextRow = idOfNextRow;
        this.rowToInsert = rowToInsert;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(WootContent content)
    {
        int indexOfPreviousRow = content.indexOfId(this.idOfPreviousRow);
        int indexOfNextRow = content.indexOfIdAfter(indexOfPreviousRow, this.idOfNextRow);

        WootId idOfRowToInsert = this.rowToInsert.getWootId();

        while (indexOfPreviousRow < (indexOfNextRow - 1)) {
            int degree = content.elementAt(indexOfPreviousRow + 1).getDegree();

            for (int i = indexOfPreviousRow + 2; i < indexOfNextRow; ++i) {
                int d = content.elementAt(i).getDegree();

                if (d < degree) {
                    degree = d;
                }
            }

            for (int i = indexOfPreviousRow + 1; i < indexOfNextRow; ++i) {
                if (content.elementAt(i).getDegree() == degree) {
                    int comparisonResult = content.elementAt(i).getWootId().compareTo(idOfRowToInsert);

                    if (comparisonResult < 0) {
                        indexOfPreviousRow = i;
                    } else {
                        indexOfNextRow = i;
                    }
                }
            }
        }

        content.insert(this.rowToInsert, indexOfPreviousRow);
    }

    /**
     * @return the new row that will be inserted in the content.
     */
    public WootRow getRowToInsert()
    {
        return this.rowToInsert;
    }

    /**
     * @param rowToInsert the rowToInsert to set.
     * @see #getRowToInsert()
     */
    public void setRowToInsert(WootRow rowToInsert)
    {
        this.rowToInsert = rowToInsert;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canExecute(WootContent content)
    {
        return (content.containsById(this.idOfPreviousRow) && content.containsById(this.idOfNextRow) && !(content
            .containsById(this.rowToInsert.getWootId())));
    }

    /**
     * @param content the content affected by this operation.
     * @return an array containing on the first position the index of the previous row and on the second position the
     *         index of the next row, relative to the position of the newly inserted row.
     * @see WootOp#getAffectedRowIndexes(WootContent)
     */
    public int[] getAffectedRowIndexes(WootContent content)
    {
        int indexOfPreviousRow = content.indexOfId(this.getIdOfPreviousRow());

        if (indexOfPreviousRow < 0) {
            return null;
        }

        int indexOfNextRow = content.indexOfIdAfter(indexOfPreviousRow, this.getIdOfNextRow());

        if (indexOfNextRow < 0) {
            return null;
        }

        int[] indexs = new int[2];
        indexs[0] = indexOfPreviousRow;
        indexs[1] = indexOfNextRow;

        return indexs;
    }

    /**
     * @return the id of the row locate after the position where the new row will be inserted.
     */
    public WootId getIdOfNextRow()
    {
        return this.idOfNextRow;
    }

    /**
     * @param idOfNextRow the idOfNextRow to set.
     * @see #getIdOfNextRow()
     */
    public void setIdOfNextRow(WootId idOfNextRow)
    {
        this.idOfNextRow = idOfNextRow;
    }

    /**
     * @return the id of the row located before the position where the new row will be inserted.
     */
    public WootId getIdOfPreviousRow()
    {
        return this.idOfPreviousRow;
    }

    /**
     * @param idOfPreviousRow the idOfPreviousRow to set.
     * @see #getIdOfPreviousRow()
     */
    public void setIdOfPreviousRow(WootId idOfPreviousRow)
    {
        this.idOfPreviousRow = idOfPreviousRow;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        String separator = ", ";
        return super.toString() + " insert(" + this.getRowToInsert() + separator + this.getIdOfPreviousRow()
            + separator + this.getIdOfNextRow() + ")";
    }
}
