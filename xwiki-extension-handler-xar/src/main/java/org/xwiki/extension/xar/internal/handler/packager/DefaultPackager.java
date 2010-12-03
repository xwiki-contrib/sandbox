package org.xwiki.extension.xar.internal.handler.packager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.xar.internal.handler.packager.xml.DocumentHandler;

public class DefaultPackager
{
    @Requirement
    private ComponentManager componentManager;

    private SAXParserFactory parserFactory;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.component.phase.Initializable#initialize()
     */
    public void initialize() throws InitializationException
    {
        this.parserFactory = SAXParserFactory.newInstance();
    }

    public ImportResult importXAR(File xarFile, String wiki) throws IOException
    {
        ImportResult importResult = new ImportResult();

        FileInputStream fis = new FileInputStream(xarFile);
        ZipInputStream zis = new ZipInputStream(fis);

        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            try {
                SAXParser saxParser = this.parserFactory.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();

                DocumentHandler handler = new DocumentHandler(componentManager);
                xmlReader.setContentHandler(handler);

                xmlReader.parse(new InputSource(zis));
            } catch (Exception e) {
                importResult.error("Failed to parse document [" + entry.getName() + "]", e);
            }
        }

        return importResult;
    }
}
