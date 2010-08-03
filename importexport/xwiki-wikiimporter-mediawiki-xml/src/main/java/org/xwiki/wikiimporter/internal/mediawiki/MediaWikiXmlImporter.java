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
package org.xwiki.wikiimporter.internal.mediawiki;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.wikiimporter.bridge.WikiImporterDocumentBridge;
import org.xwiki.wikiimporter.importer.AbstractWikiImporter;
import org.xwiki.wikiimporter.importer.WikiImporterException;
import org.xwiki.wikiimporter.internal.importer.WikiImporterLogger;
import org.xwiki.wikiimporter.listener.WikiImporterListener;
import org.xwiki.wikiimporter.type.WikiImporterType;

/**
 * MediaWikiXmlImporter imports mediawiki pages from its exported xml dumps into XWiki.
 * 
 * @version $Id$
 */
@Component("mediawiki/xml")
public class MediaWikiXmlImporter extends AbstractWikiImporter
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Imports MediaWiki XML dump into XWiki.";

    @Requirement
    private ComponentManager componentManager;

    @Requirement
    private WikiImporterDocumentBridge docBridge;

    @Requirement
    private WikiImporterLogger logger;

    SAXParserFactory saxParserFactory;

    public MediaWikiXmlImporter()
    {
        super("MediaWiki XML", DESCRIPTION, MediaWikiImportParameters.class);

        this.saxParserFactory = SAXParserFactory.newInstance();
    }

    /**
     * Parses the document with given import parameters and fires events on given MediaWikiImporterListener.
     * 
     * @see org.xwiki.wikiimporter.importer.WikiImporter#importWiki(java.lang.Object,
     *      org.xwiki.wikiimporter.listener.WikiImporterListener)
     */
    public void importWiki(Map<String, ? > parameters, WikiImporterListener listener) throws WikiImporterException
    {
        // Populating MediaWikiParameters.
        MediaWikiImportParameters mxParams = populateParameterBean(parameters);

        importWiki(mxParams, listener);
    }

    /**
     * This uses default WikiImporterListener implemented for the given WikiImporter type. Populates the parameters into
     * MediaWikiImporter parameter bean class.
     * 
     * @see org.xwiki.wikiimporter.importer.WikiImporter#importWiki(java.util.Map)
     */
    public void importWiki(Map<String, ? > parameters) throws WikiImporterException
    {
        // Populating MediaWikiParameters.
        MediaWikiImportParameters mxParameters = populateParameterBean(parameters);

        try {
            MediaWikiImporterListener mwXmlListener =
                new MediaWikiImporterListener(this.componentManager, mxParameters);
            importWiki(mxParameters, mwXmlListener);
        } catch (ComponentLookupException e) {
            throw new WikiImporterException("Failed to create MediaWikiImporterListener", e);
        }
    }

    private void importWiki(MediaWikiImportParameters params, WikiImporterListener listener)
        throws WikiImporterException
    {
        this.logger.info("Import process started.", false);

        this.parseWikiDumpXml(params, listener);

        this.logger.info("Import process completed Successfully.", false);
        this.docBridge.log(this.logger.getAllLogsAsString());
        this.logger.clearAllLogs();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.wikiimporter.importer.WikiImporter#getType()
     */
    public WikiImporterType getType()
    {
        return WikiImporterType.MEDIAWIKI_XML;
    }

    private MediaWikiImportParameters populateParameterBean(Map<String, ? > paramsMap)
        throws MediaWikiImporterException
    {
        MediaWikiImportParameters params = new MediaWikiImportParameters();
        try {
            this.beanManager.populate(params, paramsMap);
        } catch (Exception e) {
            throw new MediaWikiImporterException("Unable to populate the parameter bean", e);
        }

        return params;
    }

    /**
     * Parses MediaWiki XML using a SAX Parser
     * 
     * @param xmlFilePath the absolute path to the XML file.
     * @param listener {@link WikiImporterListener} which listens to events generated by parser.
     * @throws MediaWikiImporterException in case of any errors parsing the XML file.
     */
    private void parseWikiDumpXml(MediaWikiImportParameters params, WikiImporterListener listener)
        throws MediaWikiImporterException
    {
        String xmlFilePath = params.getSrcPath();

        File file = new File(xmlFilePath);

        try {
            MediaWikiXmlHandler handler = new MediaWikiXmlHandler(this.componentManager, listener);
            SAXParser saxParser = this.saxParserFactory.newSAXParser();
            saxParser.parse(file, handler);
        } catch (Exception e) {
            throw new MediaWikiImporterException("Error while parsing the MediaWiki XML Dump File", e);
        }
    }
}
