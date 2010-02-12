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
    /**
     * User Preferences are collected here
     */
    private List<UserPref> result;

    private static final String USER_PREF_ELEMENT_QNAME = "UserPref";

    private static final String USER_PREF_ATTRIBUTE_NAME_QNAME = "name";

    private static final String USER_PREF_ATTRIBUTE_DISPLAY_NAME_QNAME = "display_name";

    private static final String USER_PREF_ATTRIBUTE_URLPARAM_QNAME = "urlparam";

    private static final String USER_PREF_ATTRIBUTE_DATATYPE_QNAME = "datatype";

    private static final String USER_PREF_ATTRIBUTE_REQUIRED_QNAME = "required";

    private static final String USER_PREF_ATTRIBUTE_DEFAULT_VALUE_QNAME = "default_value";

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
     * Catches a User Preference XML tag. Creates a new UserPref object and attaches it to the results list
     * 
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        // catch UserPref tags
        if (USER_PREF_ELEMENT_QNAME.equals(qName)) {
            UserPref up = new UserPref();
            up.setName(atts.getValue(USER_PREF_ATTRIBUTE_NAME_QNAME));
            up.setDisplayName(atts.getValue(USER_PREF_ATTRIBUTE_DISPLAY_NAME_QNAME));
            up.setUrlparam(atts.getValue(USER_PREF_ATTRIBUTE_URLPARAM_QNAME));
            up.setDatatype(atts.getValue(USER_PREF_ATTRIBUTE_DATATYPE_QNAME));
            up.setRequired(atts.getValue(USER_PREF_ATTRIBUTE_REQUIRED_QNAME));
            up.setDefaultValue(atts.getValue(USER_PREF_ATTRIBUTE_DEFAULT_VALUE_QNAME));

            result.add(up);
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
