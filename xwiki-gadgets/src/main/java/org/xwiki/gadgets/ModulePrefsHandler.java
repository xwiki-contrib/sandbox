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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @version $Id$
 */
public class ModulePrefsHandler extends DefaultHandler
{
    /**
     * The XML Module Preference tag name.
     */
    private static final String MODULE_PREFS_ELEMENT_QNAME = "ModulePrefs";

    /**
     * Module Preferences are saved here.
     */
    private ModulePrefs result;

    /**
     * Creates a new instance.
     */
    public ModulePrefsHandler()
    {
        super();
    }

    /**
     * Initializes the Module Preferences object. {@inheritDoc}
     * 
     * @throws SAXException any SAX exception, possibly wrapping another exception
     * @see DefaultHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
        result = new ModulePrefs();
    }

    /**
     * Catches the Module Preferences XML tag. {@inheritDoc}
     * 
     * @see DefaultHandler#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        // catch ModulePrefs tags
        if (MODULE_PREFS_ELEMENT_QNAME.equals(qName)) {
            for (int i = 0; i < atts.getLength(); i++) {
                String qname = atts.getQName(i);
                String value = atts.getValue(i);

                result.set(qname, value);
            }
        }
    }

    /**
     * @return the Module Preferences
     * @see {@link ModulePrefs}
     */
    public ModulePrefs getResult()
    {
        return result;
    }
}
