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
package org.xwiki.wikiimporter.internal.type;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.wikiimporter.importer.WikiImporter;
import org.xwiki.wikiimporter.importer.WikiImporterException;
import org.xwiki.wikiimporter.type.WikiImporterType;
import org.xwiki.wikiimporter.type.WikiImporterTypeFactory;
import org.xwiki.wikiimporter.type.WikiType;

/**
 * @version $Id$
 */
@Component
public class DefaultWikiImporterTypeFactory implements WikiImporterTypeFactory
{
    /**
     * Used to cut the syntax identifier into syntax name and syntax version.
     */
    private static final Pattern TYPE_PATTERN = Pattern.compile("(.*)\\/(.*)");

    /**
     * Used to lookup all the supported WikiImporter Types.
     */
    @Requirement
    private ComponentManager componentManager;

    /**
     * {@inheritDoc}
     * 
     * @throws WikiImporterException
     * @see org.xwiki.wikiimporter.type.WikiImporterTypeFactory#createTypeFromIdString(java.lang.String)
     */
    public WikiImporterType createTypeFromIdString(String typeIdAsString) throws WikiImporterException
    {

        Matcher matcher = TYPE_PATTERN.matcher(typeIdAsString);
        if (!matcher.matches()) {
            throw new WikiImporterException("Unsupporter Wiki Importer type  [" + typeIdAsString + "]");
        }

        String wikiTypeId = matcher.group(1);
        String dataFormat = matcher.group(2);

        WikiType wikiType = new WikiType(wikiTypeId, wikiTypeId);

        return new WikiImporterType(wikiType, dataFormat);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws WikiImporterException
     * @see org.xwiki.wikiimporter.type.WikiImporterTypeFactory#getAvailableTypes()
     */
    public List<WikiImporterType> getAvailableTypes() throws WikiImporterException
    {
        List<WikiImporterType> availableTypesList = new ArrayList<WikiImporterType>();
        List<WikiImporter> importers;

        try {
            importers = this.componentManager.lookupList(WikiImporter.class);
        } catch (ComponentLookupException e) {
            throw new WikiImporterException("Failed to lookup the list of available Wiki Importer Types.", e);
        }

        for (WikiImporter importer : importers) {
            availableTypesList.add(importer.getType());
        }

        return availableTypesList;
    }

}
