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

package org.xwiki.xoo.xwikilib.cleaner.internal;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.officeimporter.internal.filter.AnchorFilter;
import org.xwiki.officeimporter.internal.filter.ImageFilter;
import org.xwiki.officeimporter.internal.filter.LineBreakFilter;
import org.xwiki.officeimporter.internal.filter.ListFilter;
import org.xwiki.officeimporter.internal.filter.ParagraphFilter;
import org.xwiki.officeimporter.internal.filter.RedundancyFilter;
import org.xwiki.officeimporter.internal.filter.StripperFilter;
import org.xwiki.officeimporter.internal.filter.StyleFilter;
import org.xwiki.officeimporter.internal.filter.TableFilter;

import org.xwiki.xoo.xwikilib.cleaner.XOOHTMLCleaner;

import org.w3c.dom.Document;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLUtils;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.filter.HTMLFilter;

/**
 * Default HTML cleaner for the OpenOffice output.
 * 
 * @version $Id$
 * @since 1.0 M
 */

public class DefaultXOOHTMLCleaner implements XOOHTMLCleaner
{

    private EmbeddableComponentManager ecm;

    /**
     * Constructor.
     * 
     * @param cm componentManager
     */
    public DefaultXOOHTMLCleaner(EmbeddableComponentManager cm)
    {
        this.ecm = cm;
    }

    /**
     * {@inheritDoc}
     */
    public String clean(String dirtyHTML)
    {
        try {

            HTMLCleaner cleaner = (HTMLCleaner) ecm.lookup(HTMLCleaner.class);

            HTMLCleanerConfiguration config = cleaner.getDefaultConfiguration();

            List<HTMLFilter> filters = new ArrayList<HTMLFilter>();
            filters.add(new StripperFilter());
            filters.add(new StyleFilter());
            filters.add(new RedundancyFilter());
            filters.add(new ParagraphFilter());
           // filters.add(new ImageFilter());
            filters.add(new AnchorFilter());
            filters.add(new ListFilter());
            filters.add(new TableFilter());
            filters.add(new LineBreakFilter());
           
            config.setFilters(filters);

            Document document = cleaner.clean(new StringReader(dirtyHTML), config);
            return HTMLUtils.toString(document);

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Exception while cleaning", t);
        }
    }
}
