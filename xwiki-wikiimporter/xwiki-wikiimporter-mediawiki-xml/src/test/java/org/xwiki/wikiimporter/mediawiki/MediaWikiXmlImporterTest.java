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
package org.xwiki.wikiimporter.mediawiki;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.test.AbstractComponentTestCase;
import org.xwiki.wikiimporter.importer.WikiImporter;
import org.xwiki.wikiimporter.importer.WikiImporterException;
import org.xwiki.wikiimporter.listener.WikiImporterListener;

/**
 * @version $Id$
 */
public class MediaWikiXmlImporterTest extends AbstractComponentTestCase
{
    private WikiImporter wikiimporter;

    private WikiImporterListener listener;

    private String dumpPath;
    
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // listener = new MediaWikiImporterListener();
        this.dumpPath = this.getClass().getResource("/MediaWikiXML.xml").getPath();

        this.wikiimporter = getComponentManager().lookup(WikiImporter.class, "mediawiki/xml");
        this.listener = getComponentManager().lookup(WikiImporterListener.class, "mediawiki/xml");
    }

    @Test
    public void testWikiImporter() throws Exception
    {
        Assert.assertNotNull(this.wikiimporter);
        Assert.assertNotNull(this.listener);
        Assert.assertEquals("mediawiki/xml", this.wikiimporter.getType().toIdString());
    }

    @Test
    public void testImportWikiWithMapParams() throws WikiImporterException
    {
        Assert.assertNotNull(this.dumpPath);

        // Create Map of Parameters
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("srcPath", this.dumpPath);
    }
}
