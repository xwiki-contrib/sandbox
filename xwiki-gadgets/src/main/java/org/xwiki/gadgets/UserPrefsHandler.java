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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @version $Id$
 */
public class UserPrefsHandler extends DefaultHandler
{
    private static final String USER_PREF_ELEMENT_QNAME = "UserPref";

    private static final String USER_PREF_ATTRIBUTE_NAME_QNAME = "name";

    private static final String USER_PREF_ATTRIBUTE_DISPLAY_NAME_QNAME = "display_name";

    private static final String USER_PREF_ATTRIBUTE_URLPARAM_QNAME = "urlparam";

    private static final String USER_PREF_ATTRIBUTE_DATATYPE_QNAME = "datatype";

    private static final String USER_PREF_ATTRIBUTE_REQUIRED_QNAME = "required";

    private static final String USER_PREF_ATTRIBUTE_DEFAULT_VALUE_QNAME = "default_value";

    private static final String ENUM_VALUE_ELEMENT_QNAME = "EnumValue";

    private static final String ENUM_VALUE_ATTRIBUTE_VALUE_QNAME = "value";

    private static final String ENUM_VALUE_ATTRIBUTE_DISPLAY_VALUE_QNAME = "display_value";

    private static final String USER_PREF_ATTRIBUTE_DATATYPE_ENUM_VALUE = "enum";

    /**
     * User Preferences are collected here
     */
    private List<UserPref> result;

    /**
     * The currently parsed User Preference
     */
    private UserPref currentUserPref;

    /**
     * Creates a new instance
     */
    public UserPrefsHandler()
    {
        super();
    }

    /**
     * Initializes the list of User Preferences result
     * 
     * @see DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
        result = new ArrayList<UserPref>();
    }

    /**
     * Catches User Preference and EnumValue XML tags. When a UserPref tags is found, it creates a new UserPref object
     * as the currently parsed User Preference. When it catches EnumValue XML tag, and if a user pref is currently
     * parsed, it appends to its enum value list.
     * 
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        // catch UserPref tags
        if (USER_PREF_ELEMENT_QNAME.equals(qName)) {
            currentUserPref = new UserPref();
            currentUserPref.setName(atts.getValue(USER_PREF_ATTRIBUTE_NAME_QNAME));
            currentUserPref.setDisplayName(atts.getValue(USER_PREF_ATTRIBUTE_DISPLAY_NAME_QNAME));
            currentUserPref.setUrlparam(atts.getValue(USER_PREF_ATTRIBUTE_URLPARAM_QNAME));
            currentUserPref.setDatatype(atts.getValue(USER_PREF_ATTRIBUTE_DATATYPE_QNAME));
            currentUserPref.setRequired(atts.getValue(USER_PREF_ATTRIBUTE_REQUIRED_QNAME));
            currentUserPref.setDefaultValue(atts.getValue(USER_PREF_ATTRIBUTE_DEFAULT_VALUE_QNAME));

            // initialize enum values list, if datatype is of type "enum"
            if (USER_PREF_ATTRIBUTE_DATATYPE_ENUM_VALUE.equals(currentUserPref.getDatatype()))
                currentUserPref.setEnumValues(new ArrayList<EnumValue>());

        } else if (ENUM_VALUE_ELEMENT_QNAME.equals(qName)
            && USER_PREF_ATTRIBUTE_DATATYPE_ENUM_VALUE.equals(currentUserPref.getDatatype())) {
            // catch Enum Value tags, only if a User Pref is currently parsed
            if (currentUserPref != null) {
                EnumValue ev = new EnumValue();
                ev.setValue(atts.getValue(ENUM_VALUE_ATTRIBUTE_VALUE_QNAME));
                ev.setDisplayValue(atts.getValue(ENUM_VALUE_ATTRIBUTE_DISPLAY_VALUE_QNAME));

                currentUserPref.getEnumValues().add(ev);
            }
        }
    }

    /**
     * Catches the end tags for User Preferences, and appends the parsed preference to the results list.
     * 
     * @see DefaultHandler#endElement(String, String, String)
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        if (USER_PREF_ELEMENT_QNAME.equals(qName)) {
            if (currentUserPref != null) {
                result.add(currentUserPref);
                currentUserPref = null;
            }
        }
    }

    /**
     * @return the result list of User Preferences
     */
    public List<UserPref> getResult()
    {
        return result;
    }
}
