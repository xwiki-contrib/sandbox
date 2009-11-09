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
package org.xwoot.thomasRuleEngine.mock;

import org.xwoot.thomasRuleEngine.core.Identifier;

/**
 * Mockup for the Indetifier interface.
 * 
 * @version $Id$
 */
public class MockIdentifier implements Identifier
{
    /** Unique ID used for serialization. */
    private static final long serialVersionUID = 4717183124930677075L;

    /**
     * @see #getPageName()
     */
    private String pageName;

    /** The ID of the metadata. */
    private String metaDataId;

    /**
     * @see #getId()
     */
    private String id;

    /**
     * Creates a new MDIdentifier object.
     * 
     * @param pageName the page name.
     * @param metaDataId the id of the metadata.
     */
    public MockIdentifier(String pageName, String metaDataId)
    {
        this.pageName = pageName;
        this.metaDataId = metaDataId;
        this.setId();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object with)
    {
        if (!(with instanceof Identifier)) {
            return false;
        }

        return this.id.equals(((MockIdentifier) with).getId());
    }

    /**
     * @return the computed id.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return the metadata ID.
     */
    public String getMetaDataId()
    {
        return this.metaDataId;
    }

    /** {@inheritDoc} */
    public String getPageName()
    {
        return this.pageName;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }

    /**
     * Computes the id.
     */
    private void setId()
    {
        this.id = this.pageName + "." + this.metaDataId;
    }

    /**
     * @param metaDataId the metaDataId to set.
     */
    public void setMetaDataId(String metaDataId)
    {
        this.metaDataId = metaDataId;
        this.setId();
    }

    /**
     * @param pageName the pageName to set.
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
        this.setId();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.id;
    }
}
