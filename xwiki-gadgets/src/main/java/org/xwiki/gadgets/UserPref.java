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
package org.xwiki.gadgets;

import java.util.List;

/**
 * A Google Gadget User Preference. It is used by the GadgetsService when parsing a gadget XML for the UserPrefs and
 * when importing a Google Gadget as a XWiki Wiki Macro in Velocity.
 * 
 * @see <a href="http://code.google.com/apis/gadgets/docs/reference.html#Userprefs_Ref"> Gadgets XML Reference - User
 *      Preferences</a>
 * @version $Id$
 */
public class UserPref
{
    /**
     * Required "symbolic" name of the user preference; displayed to the user during editing if no display_name is
     * defined. Must contain only letters, number and underscores, i.e. the regular expression ^[a-zA-Z0-9_]+$. Must be
     * unique.
     */
    private String name;

    /**
     * Optional string to display alongside the user preferences in the edit window. Must be unique.
     */
    private String displayName;

    /**
     * Optional string to pass as the parameter name for content type="url".
     */
    private String urlparam;

    /**
     * Optional string that indicates the data type of this attribute. Can be string, bool, enum, hidden (a string that
     * is not visible or user editable), or list (dynamic array generated from user input). The default is string.
     */
    private String datatype = "string";

    /**
     * Optional boolean argument (true or false) indicating whether this user preference is required. The default is
     * false.
     */
    private String required = "false";

    /**
     * Optional string that indicates a user preference's default value.
     */
    private String defaultValue;

    /**
     * If the value for the datatype attribute is enum, the enum data type is presented in the user interface as a menu
     * of choices. You specify the contents of the menu using {@link EnumValue}.
     */
    private List<EnumValue> enumValues;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getUrlparam()
    {
        return urlparam;
    }

    public void setUrlparam(String urlparam)
    {
        this.urlparam = urlparam;
    }

    public String getDatatype()
    {
        return datatype;
    }

    public void setDatatype(String datatype)
    {
        this.datatype = datatype;
    }

    public String getRequired()
    {
        return required;
    }

    public void setRequired(String required)
    {
        this.required = required;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public List<EnumValue> getEnumValues()
    {
        return enumValues;
    }

    public void setEnumValues(List<EnumValue> enumValues)
    {
        this.enumValues = enumValues;
    }
}
