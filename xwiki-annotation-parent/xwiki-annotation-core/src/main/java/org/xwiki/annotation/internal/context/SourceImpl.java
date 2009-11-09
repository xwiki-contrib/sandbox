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

package org.xwiki.annotation.internal.context;

/**
 * Default implementation for {@link Source} interface.
 * 
 * @version $Id$
 */
public class SourceImpl implements Source
{
    private final CharSequence content;

    /**
     * @param content
     */
    public SourceImpl(CharSequence content)
    {
        this.content = content;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.context.Source#getSource()
     */
    public CharSequence getSource()
    {
        return content;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Source) || obj == null) {
            return false;
        }

        if (getSource() == null && ((Source) obj).getSource() != null) {
            return false;
        }

        return getSource().toString().equals(((Source) obj).getSource().toString());
    }
}
