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

package org.xwiki.annotation.internal.maintainment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;
import org.xwiki.annotation.maintainment.XDelta;
import org.xwiki.component.annotation.Component;

/**
 * @version $Id$
 */
@Component()
public class JavaDiffBasedAnnotationMaintainer extends AbstractAnnotationMaintainer
{
    private Collection<XDelta> deltas = null;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.maintainment.AbstractAnnotationMaintainer
     *      #getDifferences(java.lang.CharSequence, java.lang.CharSequence)
     */
    @Override
    protected Collection<XDelta> getDifferences(CharSequence previous, CharSequence current)
    {
        deltas = new ArrayList<XDelta>();
        List<Character> previousContent = new ArrayList<Character>();
        for (int i = 0; i < previous.length(); ++i) {
            previousContent.add(previous.charAt(i));
        }
        List<Character> currentContent = new ArrayList<Character>();
        for (int i = 0; i < current.length(); ++i) {
            currentContent.add(current.charAt(i));
        }
        Diff<Character> diff = new Diff<Character>(previousContent, currentContent);
        for (Difference it : diff.diff()) {
            if (it.getAddedEnd() != Difference.NONE && it.getAddedStart() != Difference.NONE) {
                deltas.add(new XAddition(it.getAddedStart(), it.getAddedEnd() - it.getAddedStart() + 1));
            } else if (it.getDeletedEnd() != Difference.NONE && it.getDeletedStart() != Difference.NONE) {
                deltas.add(new XDeletion(it.getDeletedStart(), it.getDeletedEnd() - it.getDeletedStart() + 1));
            }
        }
        return deltas;
    }
}
