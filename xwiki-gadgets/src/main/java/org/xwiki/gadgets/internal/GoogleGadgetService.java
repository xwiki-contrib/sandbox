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
package org.xwiki.gadgets.internal;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.gadgets.GadgetService;
import org.xwiki.gadgets.UserPref;
import org.xwiki.gadgets.UserPrefsHandler;
import org.xwiki.xml.XMLReaderFactory;

/**
 * @version $Id$
 */
@Component("google")
public class GoogleGadgetService implements GadgetService
{
    @Requirement
    private XMLReaderFactory xmlReaderFactory;

    /**
     * {@inheritDoc}
     * 
     * @see GadgetService#parseUserPrefs(String)
     */
    public List<UserPref> parseUserPrefs(String gadgetUri)
    {
        try {
            XMLReader xr = xmlReaderFactory.createXMLReader();
            UserPrefsHandler upHandler = new UserPrefsHandler();
            xr.setContentHandler(upHandler);
            xr.parse(new InputSource(gadgetUri));

            return upHandler.getResult();
        } catch (SAXException e) {
            // TODO: log error
        } catch (ParserConfigurationException e) {
            // TODO: log error
        } catch (IOException e) {
            // TODO: log parse error
        }
        return null;
    }
}
