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
package org.xwoot.xwootApp.core.tre;

import org.xwoot.thomasRuleEngine.core.Identifier;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class XWootObjectIdentifier implements Identifier
{
    /**  */
    private static final long serialVersionUID = 4717183124930677075L;

    private String id;

    /**
     * Creates a new MDIdentifier object.
     * 
     * @param pageName DOCUMENT ME!
     * @param metaDataId DOCUMENT ME!
     */
    public XWootObjectIdentifier(String id)
    {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param with DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    @Override
    public boolean equals(Object with)
    {
        if (!(with instanceof Identifier)) {
            return false;
        }
        return this.id.equals(((XWootObjectIdentifier) with).getId());
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param metaDataId DOCUMENT ME!
     */
    public void setId(String id)
    {
        this.id=id;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    @Override
    public String toString()
    {
        return this.id;
    }
}
