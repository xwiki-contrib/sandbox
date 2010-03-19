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
package org.xwiki.spaces.internal;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.spaces.SpaceManagerConfiguration;

/**
 * Default implementation of the space manager configuration.
 * 
 * @version $Id$
 */
public class DefaultSpaceManagerConfiguration implements SpaceManagerConfiguration
{

    /** The prefix to use for all spaces related configuration keys. */
    private static final String CONFIGURATION_PREFIX = "spaces.";

    /** The default validation regex to validate space name against. */
    private static final String DEFAULT_VALIDATION_REGEX = "^\\w{3}\\w+$";

    /** The fullname of the default document to include in a space home page. */
    private static final String DEFAULT_SPACE_INCLUDE = "XWiki.SpaceClassSheet";

    /** Our source of configuration to retrieve properties against. */
    @Requirement
    private ConfigurationSource source;

    /**
     * {@inheritDoc}
     * 
     * @see SpaceManagerConfiguration#getDefaultSpaceType()
     */
    public String getDefaultSpaceType()
    {
        // Default is no space type.
        return source.getProperty(CONFIGURATION_PREFIX + "defaultSpaceType", "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see SpaceManagerConfiguration#getSpaceManagersAccessLevels()
     */
    public String getSpaceManagersAccessLevels()
    {
        return source.getProperty(CONFIGURATION_PREFIX + "managersAccessLevels", "view, edit, comment, delete, admin");
    }

    /**
     * {@inheritDoc}
     * 
     * @see SpaceManagerConfiguration#getSpaceMembersAccessLevels()
     */
    public String getSpaceMembersAccessLevels()
    {
        return source.getProperty(CONFIGURATION_PREFIX + "membersAccessLevels", "view, edit, comment");
    }

    /**
     * {@inheritDoc}
     * 
     * @see SpaceManagerConfiguration#getSpaceNameValidationRegex()
     */
    public String getSpaceNameValidationRegex()
    {
        // Default is only alphanumeric chars and at least 3 of them.
        return source.getProperty(CONFIGURATION_PREFIX + "nameValidationRegex", DEFAULT_VALIDATION_REGEX);
    }

    /**
     * {@inheritDoc}
     * 
     * @see SpaceManagerConfiguration#getSpaceHomeInclude()
     */
    public String getSpaceHomeInclude()
    {
        return source.getProperty(CONFIGURATION_PREFIX + "spaceHomeInclude", DEFAULT_SPACE_INCLUDE);
    }

}
