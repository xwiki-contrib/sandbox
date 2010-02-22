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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.gadgets.GadgetService;
import org.xwiki.gadgets.ModulePrefs;
import org.xwiki.gadgets.ModulePrefsHandler;
import org.xwiki.gadgets.UserPref;
import org.xwiki.gadgets.UserPrefsHandler;
import org.xwiki.xml.XMLReaderFactory;

/**
 * @version $Id$
 */
@Component("google")
public class GoogleGadgetService implements GadgetService
{
    /**
     * Default XWiki logger to report errors correctly
     */
    private static final Log LOG = LogFactory.getLog(GoogleGadgetService.class);

    /**
     * The XML reader factory used to create XML readers
     */
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
        } catch (Exception e) {
            LOG.error(String.format("Exception while parsing User Preferences from gadget XML at location %s.",
                gadgetUri), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see GadgetService#parseModulePrefs(String)
     */
    public ModulePrefs parseModulePrefs(String gadgetUri)
    {
        try {
            XMLReader xr = xmlReaderFactory.createXMLReader();
            ModulePrefsHandler mpHandler = new ModulePrefsHandler();
            xr.setContentHandler(mpHandler);
            xr.parse(new InputSource(gadgetUri));

            return mpHandler.getResult();
        } catch (Exception e) {
            LOG.error(String.format("Exception while parsing Module Preferences from gadget XML at location %s.",
                gadgetUri), e);
            return null;
        }
    }
}
