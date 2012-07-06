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
package org.xwiki.batchimport.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.batchimport.BatchImport;
import org.xwiki.batchimport.BatchImportConfiguration;
import org.xwiki.batchimport.BatchImportConfiguration.Overwrite;
import org.xwiki.batchimport.ImportFileIterator;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.officeimporter.OfficeImporterException;
import org.xwiki.officeimporter.OfficeImporterVelocityBridge;
import org.xwiki.officeimporter.document.XDOMOfficeDocument;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.DateClass;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.util.Util;

/**
 * Default Batch import implementation, uses {@link ImportFileIterator}s to process the files to import, according to
 * the {@link BatchImportConfiguration#getType()} setting. If you need to import from a new format, register a new
 * {@link ImportFileIterator} implementation with a hint which you then pass in the {@link BatchImportConfiguration}.
 * 
 * @version $Id$
 */
@Component
public class DefaultBatchImport implements BatchImport
{
    @Inject
    protected Execution execution;

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultBatchImport.class);

    @Inject
    protected ComponentManager cm;

    protected String debugMessage = "";

    protected boolean debug = true;

    @Inject
    @Named("current/reference")
    protected DocumentReferenceResolver<EntityReference> currentDocumentEntityReferenceResolver;

    @Inject
    @Named("current")
    protected DocumentReferenceResolver<String> currentDocumentStringResolver;

    @Inject
    protected EntityReferenceSerializer<String> entityReferenceSerializer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.batchimport.BatchImport#getColumnHeaders(org.xwiki.batchimport.BatchImportConfiguration)
     */
    @Override
    public List<String> getColumnHeaders(BatchImportConfiguration config) throws IOException
    {
        // get an iterator from the config and reset its file
        ImportFileIterator iterator = null;
        try {
            iterator = getImportFileIterator(config);
        } catch (Exception e) {
            throw new IOException("Could not find an import file reader for the configuration: " + config.toString(), e);
        }
        iterator.resetFile(config);

        // and pass it to the internal function
        return getColumnHeaders(iterator, config.hasHeaderRow());
    }

    /**
     * @param iterator the file iterator to read header from
     * @param hasHeaderRow whether the file has header row or not, to know how to return the values for the first row
     * @return the column headers of the file to process, that is the values from the first line from the file. If has
     *         header row is true, values will be returned as they are, otherwise they will be processed by adding the
     *         "(<column number>)' string at the end of the value. The returned list preserves order so the actual index
     *         of the column should be taken from the index in this list.
     * @throws IOException if the file cannot be read
     */
    protected List<String> getColumnHeaders(ImportFileIterator iterator, boolean hasHeaderRow) throws IOException
    {
        List<String> columns;

        List<String> headerLine = iterator.readNextLine();
        if (headerLine == null) {
            return null;
        }

        columns = new ArrayList<String>();
        for (int i = 0; i < headerLine.size(); i++) {
            String header = headerLine.get(i);
            // handle this line differently depending on whether this file has header or not
            if (hasHeaderRow) {
                columns.add(header);
            } else {
                // print column number first and then value in brackets. The returned list preserves order so the
                // actual index of the column should be taken from the index in the list
                columns.add(String.format("%d (%s)", i, header));
            }
        }

        return columns;
    }

    protected List<String> getAsList(String fields, Character separator)
    {
        List<String> list = new ArrayList<String>();
        if (fields == null)
            return list;
        for (String item : fields.split(Pattern.quote(separator.toString()))) {
            if (!StringUtils.isEmpty(item.trim())) {
                list.add(item.trim());
            }
        }
        return list;
    }

    public void log(StringBuffer result, String message)
    {
        result.append(message);
        result.append("\n");
        debug(message);
    }

    public void debug(String message)
    {
        if (this.debug) {
            debugMessage += message + "\n";
            // yeah, debug with info here because debug are billions of billions and we cannot really understand
            // anything from it
            LOGGER.info(message);
        }
    }

    public Map<String, String> getDataOriginal(List<String> row, Map<String, List<String>> reverseMapping,
        List<String> headers)
    {
        // this map seems to be like xwiki field -> value
        Map<String, String> map = new HashMap<String, String>();

        // TODO: in the original code, a test was made here for the first cell in the row, to check if it has content,
        // why?
        for (int i = 0; i < row.size(); i++) {
            String currentHeader = headers.get(i);
            List<String> xwikiFields = reverseMapping.get(currentHeader);
            if (xwikiFields != null) {
                for (String xwikiField : xwikiFields) {
                    map.put(xwikiField, row.get(i));
                }
            }
        }
        return map;
    }

    public Map<String, String> getData(List<String> row, Map<String, String> mapping, List<String> headers)
    {
        // this map seems to be like xwiki field -> value
        Map<String, String> map = new HashMap<String, String>();
        // TODO: in the original code, a test was made here for the first cell in the row, to check if it has content,
        // why?

        // get the fields in the mapping
        for (Map.Entry<String, String> fieldMapping : mapping.entrySet()) {
            String xwikiField = fieldMapping.getKey();
            String header = fieldMapping.getValue();
            int valueIndex = headers.indexOf(header);
            if (valueIndex >= 0 && valueIndex < row.size()) {
                map.put(xwikiField, row.get(valueIndex));
            }
        }

        return map;
    }

    public String getSpace(Map<String, String> data, String defaultSpace, boolean clearNames)
    {
        String space = data.get("doc.space");
        if (space == null || space == "") {
            space = defaultSpace;
        }

        if (clearNames) {
            XWikiContext xcontext = getXWikiContext();
            space = xcontext.getWiki().clearName(space, xcontext);
        }
        return space;
    }

    public DocumentReference getPageName(Map<String, String> data, int rowIndex, BatchImportConfiguration config,
        List<DocumentReference> docNameList)
    {
        // pagename prefix used to automatically generate page names, when _name is not provided
        String defaultPrefix = config.getEmptyDocNamePrefix();
        boolean ignoreEmpty = StringUtils.isEmpty(defaultPrefix);
        // the default space to add pages in
        String defaultSpace = config.getDefaultSpace();
        // whether values in column doc.name should be passed through clearName before
        boolean clearNames = config.getClearName();
        // the wiki to add pages in
        String wiki = config.getWiki();

        // TODO: in the original code the space code was copy-pasted here, not used from the function
        String space = getSpace(data, defaultSpace, clearNames);

        String name = data.get("doc.name");
        if (name == null || name == "") {
            if (ignoreEmpty) {
                return null;
            } else {
                name = defaultPrefix + rowIndex;
            }
        }

        if (clearNames) {
            XWikiContext xcontext = getXWikiContext();
            name = xcontext.getWiki().clearName(name, xcontext);
        }

        DocumentReference pageName = prepareDocumentReference(wiki, space, name);

        // prepare the document name if it's duplicate and needs to be deduplicated
        if (config.getDocNameDeduplication() == Overwrite.GENERATE_NEW && docNameList.contains(pageName)) {
            String initialName = pageName.getName();
            int counter = 0;
            while (docNameList.contains(pageName)) {
                counter++;
                pageName = prepareDocumentReference(wiki, space, initialName + counter);
            }
        }

        return pageName;
    }

    /**
     * Deduplicate page name amongst the documents that are on the same wiki.
     * 
     * @return the potentially deduplicated page name, according to the parameters in the config. Note that it can also
     *         return the very same {@code pageName} parameter.
     */
    public DocumentReference maybeDeduplicatePageNameInWiki(DocumentReference pageName,
        BatchImportConfiguration config, List<DocumentReference> savedDocuments, XWikiContext xcontext)
    {
        if (pageName == null) {
            return pageName;
        }

        String wiki = pageName.getWikiReference().getName();
        String space = pageName.getLastSpaceReference().getName();

        // verify if it should be unique in the wiki and if it is
        if (config.getOverwrite() == Overwrite.GENERATE_NEW) {
            String deduplicatedName = pageName.getName();
            int counter = 0;
            // if the document exists already, generate a new name
            while (xcontext.getWiki().exists(pageName, xcontext)) {
                if (savedDocuments.contains(pageName) && config.getDocNameDeduplication() == Overwrite.UPDATE) {
                    // if the document exists because it was saved this round and we're using deduplication strategy
                    // update, leave this name, it's good
                    break;
                }
                counter++;
                pageName = prepareDocumentReference(wiki, space, deduplicatedName + "_" + counter);
            }
        }

        return pageName;
    }

    protected DocumentReference prepareDocumentReference(String wiki, String space, String name)
    {
        if (!StringUtils.isEmpty(wiki)) {
            // specified wiki, put it in there
            return new DocumentReference(wiki, space, name);
        } else {
            // current wiki, build the reference relative to current wiki
            return currentDocumentEntityReferenceResolver.resolve(new EntityReference(name, EntityType.DOCUMENT,
                new EntityReference(space, EntityType.SPACE)));
        }
    }

    public String getFilePath(String datadir, String datadirprefix, String filename)
    {
        if (datadir == null || datadir == "" || datadir == ".") {
            datadir = "";
        } else if (!datadir.endsWith("/")) {
            datadir = datadir + "/";
        }

        if (datadirprefix == null || datadirprefix == "") {
            datadirprefix = "";
        } else if (!datadirprefix.endsWith("/")) {
            datadirprefix = datadirprefix + "/";
        }
        if (filename.startsWith("./")) {
            filename = filename.substring(2);
        }

        String path = datadir + datadirprefix + filename;
        return path;
    }

    public boolean checkFile(ZipFile zipfile, String path)
    {
        if (zipfile == null) {
            debug("Checking if file " + path + " exists on disk");

            File file = new File(path);
            return file.exists();
        } else {
            String newpath = path;
            ZipEntry zipentry = zipfile.getEntry(newpath);

            debug("Checking if file " + newpath + " exists in zip");
            return (zipentry != null);
        }
    }

    public String getFileName(String filename)
    {
        if (filename.startsWith("./")) {
            filename = filename.substring(2);
        }
        XWikiContext xcontext = getXWikiContext();
        filename = xcontext.getWiki().clearName(filename, false, true, xcontext);
        return filename;
    }

    public boolean isDirectory(ZipFile zipfile, String path)
    {
        if (zipfile == null) {
            return new File(path).isDirectory();
        } else {
            return zipfile.getEntry(path).isDirectory();
        }
    }

    public void addFiles(XWikiDocument newDoc, String path) throws IOException
    {
        File dirFile = new File(path);
        for (File file : dirFile.listFiles()) {
            debug("Adding file " + file.getName());
            byte[] filedata = Util.getFileContentAsBytes(file);
            addFile(newDoc, filedata, file.getName());
        }
    }

    public void addFile(XWikiDocument newDoc, byte[] filedata, String filename)
    {
        try {
            if (filename.startsWith("./")) {
                filename = filename.substring(2);
            }
            XWikiContext xcontext = getXWikiContext();
            filename = xcontext.getWiki().clearName(filename, false, true, xcontext);

            if (newDoc.getAttachment(filename) != null) {
                debug("Filename " + filename + " already exists in " + newDoc.getPrefixedFullName() + ".");
                return;
            }

            // this is saving the document at this point. I don't know if it was like this when the code was written,
            // but now it's like this.
            XWikiAttachment attachment = new XWikiAttachment();
            newDoc.getAttachmentList().add(attachment);
            attachment.setContent(filedata);
            attachment.setFilename(filename);
            attachment.setAuthor(xcontext.getUser());
            // Add the attachment to the document
            attachment.setDoc(newDoc);
            newDoc.saveAttachmentContent(attachment, xcontext);
        } catch (Throwable e) {
            debug("Filename " + filename + " could not be attached because of Exception: " + e.getMessage());
        }
    }

    public byte[] getFileData(ZipFile zipfile, String path) throws ZipException, IOException
    {
        if (zipfile == null) {
            return Util.getFileContentAsBytes(new File(path));
        } else {
            String newpath = path;
            ZipEntry zipentry = zipfile.getEntry(newpath);
            if (zipentry == null) {
                return null;
            }
            InputStream is = zipfile.getInputStream(zipentry);
            if (is == null) {
                return null;
            }
            return Util.getFileContentAsBytes(is);
        }
    }

    /**
     * Cleans up tmp folder after the work of office importer.
     */
    public void cleanUp()
    {
        File tmpdir = new File("/tmp/");
        for (File tmpsubdir : tmpdir.listFiles()) {
            if (tmpsubdir.getName().startsWith("sv") && tmpsubdir.getName().endsWith(".tmp")) {
                for (File file : tmpsubdir.listFiles()) {
                    file.delete();
                }
                tmpsubdir.delete();
            }
        }
    }

    public String doImport(BatchImportConfiguration config, boolean withFiles, boolean overwritefile, boolean simulation)
        throws IOException, XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWiki xwiki = xcontext.getWiki();

        try {
            StringBuffer result = new StringBuffer();

            // the file to import
            ImportFileIterator metadatafilename = null;
            try {
                metadatafilename = getImportFileIterator(config);
            } catch (ComponentLookupException e) {
                // IOException directly from the getFileIterator method
                throw new IOException("Could not find an import file reader for the configuration: "
                    + config.toString(), e);
            }
            // mapping from the class fields to source file columns
            Map<String, String> mapping = config.getFieldsMapping();

            // -------------------- Not transformed to config yet, will not work ---------------------//
            Document doc = new Document(xcontext.getDoc(), xcontext);
            // attach files referred in the column _file to the document
            boolean fileupload =
                (Integer) doc.getValue("fileupload") == null || ((Integer) doc.getValue("fileupload")).equals(0)
                    ? false : true;
            // use office importer to import the content from the column _file to the document content
            boolean fileimport =
                (Integer) doc.getValue("fileimport") == null || ((Integer) doc.getValue("fileimport")).equals(0)
                    ? false : true;
            // directory or zip file where the referenced files are stored. Directory on disk.
            String datadir = (String) doc.getValue("datafilename");
            // path of the files inside the zip
            String datadirprefix = (String) doc.getValue("datafileprefix");
            // column in the xls that will turn into tags
            // TODO: this tags needs to be reimplemented, now it works only with xwiki fields in the list: so you can
            // add something in the tags only if you import it as well. You should be able to configure it to be a
            // column in the csv / xls and that column needs to be handled as a list with the list separator.
            List<String> fieldsfortags = getAsList((String) doc.getValue("fieldsfortags"), config.getListSeparator());
            // -------------------- ----------------------------- ---------------------//

            // class to map data to (objects of this class will be created)
            BaseClass defaultClass =
                xwiki.getXClass(
                    currentDocumentStringResolver.resolve(config.getMappingClassName(),
                        StringUtils.isEmpty(config.getWiki()) ? null : new WikiReference(config.getWiki())), xcontext);
            // default date format used to parse dates from the source file
            String defaultDateFormat = config.getDefaultDateFormat();
            if (StringUtils.isEmpty(defaultDateFormat)) {
                // get it from preferences, hoping that it's set
                defaultDateFormat = xcontext.getWiki().getXWikiPreference("dateformat", xcontext);
            }
            // whether this file has header row or not (whether first line needs to be imported or not)
            boolean hasHeaderRow = config.hasHeaderRow();

            // list of document names, used to remember what are the document names that were generated from the
            // document. Note that for multiple imports from the same document, this list should be identical.
            List<DocumentReference> docNameList = new ArrayList<DocumentReference>();

            // list of documents that were actually saved during this import, to know how to make proper replacements.
            // Basically it serves to know if a document which is not new was saved before during this import or it was
            // there before the import started. This prevents "replace" from deleting twice (if multiple rows with the
            // same name are supposed to update each other) and allows to save multiple rows in the same document if
            // overwrite is set to skip and the document is created during this import (in which case duplicate rows
            // should not "skip" but "update").
            List<DocumentReference> savedDocuments = new ArrayList<DocumentReference>();

            ZipFile zipfile = null;
            if (!fileupload || datadir == "") {
                withFiles = false;
            }

            // check if the files in the datadir can be properly read
            if (withFiles) {
                // if it's a zip, try to read the zip
                if (datadir.endsWith(".zip")) {
                    log(result, "Checking zip file ${datadir}");
                    zipfile = new ZipFile(new File(datadir), "cp437");
                    // TODO: what the hell is this, why are we putting it on empty?
                    datadir = "";
                    if (zipfile == null) {
                        log(result, "Could not open zip file ${datadir}");
                        return result.toString();
                    }

                    if (debug) {
                        Enumeration<ZipEntry> zipFileEntries = zipfile.getEntries();
                        while (zipFileEntries.hasMoreElements()) {
                            ZipEntry zipe = zipFileEntries.nextElement();
                            debug("Found zip entry: " + zipe.getName());
                        }
                    }
                } else {
                    // checking it as a directory
                    log(result, "Checking data directory ${datadir}");
                    File datad = new File(datadir);
                    if (datad == null || !datad.isDirectory()) {
                        log(result, "Could not open data directory ${datadir}");
                        return result.toString();
                    }
                }
            }

            // start reading the rows and process them one by one
            metadatafilename.resetFile(config);
            List<String> currentLine = null;
            int rowIndex = 0;
            List<String> headers = null;
            // if there is no header row the headers are the numbers of the columns as strings
            if (hasHeaderRow) {
                headers = getColumnHeaders(metadatafilename, hasHeaderRow);
                currentLine = metadatafilename.readNextLine();
                rowIndex = 1;
            } else {
                currentLine = metadatafilename.readNextLine();
                headers = new ArrayList<String>();
                for (int i = 0; i < currentLine.size(); i++) {
                    headers.add(Integer.toString(i));
                }
            }

            debug("Headers are: " + headers);
            debug("Mapping is: " + mapping);

            while (currentLine != null) {
                debug("Processing row " + currentLine.toString() + ".");

                Map<String, String> data = getData(currentLine, mapping, headers);
                if (data == null) {
                    break;
                }

                debug("Row " + currentLine.toString() + " data is: " + data.toString() + "");
                // generate page name
                DocumentReference generatedDocName = getPageName(data, rowIndex, config, docNameList);
                // process the row
                if (generatedDocName != null) {
                    // check if it's duplicated name
                    boolean isDuplicateName = docNameList.contains(generatedDocName);
                    if (!isDuplicateName) {
                        docNameList.add(generatedDocName);
                    }
                    // check that this pageName should be used from the pov of the already generated file names
                    if (!(isDuplicateName && config.getDocNameDeduplication() == Overwrite.SKIP)) {
                        // potentially deduplicate it on the wiki, if needed
                        DocumentReference pageName =
                            maybeDeduplicatePageNameInWiki(generatedDocName, config, savedDocuments, xcontext);
                        // marshal data to the document objects (this is creating the document and handling overwrites)
                        XWikiDocument newDoc =
                            this.marshalDataToDocumentObjects(pageName, data, currentLine, defaultClass,
                                isDuplicateName, savedDocuments.contains(pageName), config, xcontext, fieldsfortags,
                                defaultDateFormat, result, simulation);
                        // if a new document was created and filled, valid, with the proper overwrite
                        if (newDoc != null) {
                            // save the document ...
                            if (withFiles) {
                                // ... either with its files. Saving is done in the same function as files saving
                                // there are reasons to do multiple saves when saving attachments and importing office
                                // documents, so we rely completely on files for saving.
                                // TODO: fix the overwrite parameter, for now pass false if it's set to anything else
                                // besides skip
                                saveDocumentWithFiles(newDoc, data, currentLine, config, xcontext,
                                    config.getOverwrite() != Overwrite.SKIP, simulation, overwritefile, fileimport,
                                    datadir, datadirprefix, zipfile, savedDocuments, result);
                            } else {
                                // ... or just save it: no files handling it, we save it here manually
                                String serializedPageName = entityReferenceSerializer.serialize(pageName);
                                if (!simulation) {
                                    new Document(newDoc, xcontext).save();
                                    log(result, "Imported row " + currentLine.toString() + " in page [["
                                        + serializedPageName + "]].");
                                } else {
                                    // NOTE: when used with overwrite=GENERATE_NEW, this line here can yield results a
                                    // bit different from the actual results during the import, since, if a document
                                    // fails to save with an exception, the simulation thinks it actually saved, while
                                    // the actual import knows it didn't.
                                    log(result, "Ready to import row " + currentLine.toString() + " in page "
                                        + serializedPageName + " without file.");
                                }
                                savedDocuments.add(newDoc.getDocumentReference());
                            }
                        } else {
                            // newDoc is null
                            // validation error during page generation, page generation and validation is responsible to
                            // log
                        }
                    } else {
                        // pageName exists and the config is set to ignore
                        log(result, "Ignore " + currentLine.toString() + " because page name was already used in this "
                            + "import and configuration is set to skip used names.");
                    }
                } else {
                    // pageName is null
                    log(result, "Ignore " + currentLine.toString()
                        + " because page name is empty or could not be built.");
                }

                // go to next line
                currentLine = metadatafilename.readNextLine();
                rowIndex++;
            }

            log(result, "Processing finished.");

            return result.toString();
        } finally {
            // flush the cache because cache is an ugly bitch, preserving data between simulation and actual run, which
            // then gets to be saved in the actual run
            xwiki.flushCache(xcontext);
        }
    }

    @Override
    public String deleteExistingDocuments(String className, String wiki, String space) throws XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWiki xwiki = xcontext.getWiki();

        StringBuffer result = new StringBuffer();

        String originalDatabase = xcontext.getDatabase();
        try {
            if (!StringUtils.isEmpty(wiki)) {
                xcontext.setDatabase(wiki);
            }

            // get the documents
            String searchQuery =
                "select doc.fullName from XWikiDocument doc, BaseObject obj "
                    + "where doc.fullName = obj.name and obj.className = ? and doc.fullName != ? and doc.fullName != ?";
            List<String> parameterValues = new ArrayList<String>();
            parameterValues.add(className);
            parameterValues.add(className + "Template");
            parameterValues.add(className.substring(0, className.indexOf("Class") >= 0 ? className.indexOf("Class")
                : className.length())
                + "Template");
            if (!StringUtils.isEmpty(space)) {
                // add space condition
                searchQuery += " and doc.space = ?";
                parameterValues.add(space);
            }

            List<String> results = xwiki.getStore().search(searchQuery, 0, 0, parameterValues, xcontext);
            for (String docToDelete : results) {
                try {

                    DocumentReference docToDeleteRef =
                        currentDocumentStringResolver.resolve(docToDelete, StringUtils.isEmpty(wiki) ? null
                            : new WikiReference(wiki));
                    // this is the way to delete with the proper user (current user that is), as if the delete occurred
                    // from page
                    new Document(xwiki.getDocument(docToDeleteRef, xcontext), xcontext).delete();
                    this.log(result, "Deleted document " + docToDelete + " from wiki " + wiki);
                } catch (XWikiException e) {
                    this.log(result, "Could not delete document " + docToDelete + " from wiki " + wiki + " because: "
                        + e.getMessage());
                    LOGGER.warn("Could not delete document " + docToDelete + " from wiki " + wiki, e);
                }
            }

            // flush the version cache for the documents to work properly after
            xcontext.flushArchiveCache();
            return result.toString();
        } finally {
            if (!StringUtils.isEmpty(wiki)) {
                xcontext.setDatabase(originalDatabase);
            }
        }
    }

    /**
     * TODO: implement me
     * 
     * @return {@code true} if the data can be marshaled in the specified document, {@code false} otherwise
     */
    public boolean validatePageData(XWikiDocument newDoc, Map<String, String> data, BaseClass defaultClass,
        String defaultDateFormat, boolean simulation, StringBuffer result)
    {
        return true;
    }

    public XWikiDocument marshalDataToDocumentObjects(DocumentReference pageName, Map<String, String> data,
        List<String> currentLine, BaseClass defaultClass, boolean isRowUpdate, boolean wasAlreadySaved,
        BatchImportConfiguration config, XWikiContext xcontext, List<String> fieldsfortags, String defaultDateFormat,
        StringBuffer result, boolean simulation) throws XWikiException, IOException
    {
        XWiki xwiki = xcontext.getWiki();
        String defaultClassName = config.getMappingClassName();
        Map<String, String> mapping = config.getFieldsMapping();
        DocumentReference defaultClassReference = defaultClass.getReference();
        Character listseparator = config.getListSeparator();
        Overwrite overwrite = config.getOverwrite();

        String fullName = entityReferenceSerializer.serialize(pageName);

        XWikiDocument newDoc = xwiki.getDocument(pageName, xcontext);
        // if either the document is new
        // or is not new but we're not supposed to skip
        // or it's existing, we're supposed to skip but it was saved during this import and this is an update row
        if (newDoc.isNew() || overwrite != Overwrite.SKIP || (wasAlreadySaved && isRowUpdate)) {

            // validate the data to marshal in this page
            boolean validationResult =
                validatePageData(newDoc, data, defaultClass, defaultDateFormat, simulation, result);
            if (!validationResult) {
                return null;
            }

            // if document is not new and we're in replace mode, and the document was not already saved during this
            // import, we remove it
            if (!newDoc.isNew() && overwrite == Overwrite.REPLACE && !wasAlreadySaved) {
                if (!simulation) {
                    // delete, by getting a new document
                    XWikiDocument newDoc2 = xwiki.getDocument(pageName, xcontext);
                    new Document(newDoc2, xcontext).delete();
                    // flush archive cache otherwise we cannot really re-save the document after
                    xcontext.flushArchiveCache();
                    // reload the reference so that it doesn't keep a reference to the old document
                    newDoc = xwiki.getDocument(pageName, xcontext);

                    log(result, "Removed document " + fullName + " to replace with line " + currentLine);
                } else {
                    log(result, "Removing document " + fullName + " to replace with line " + currentLine);
                }
            }

            BaseObject newDocObj = null;
            if (defaultClassName != null && defaultClassName != "") {
                newDocObj = newDoc.getXObject(defaultClassReference);
                if (newDocObj == null) {
                    newDocObj = newDoc.newXObject(defaultClassReference, xcontext);
                }
            }
            // if no object, don't continue but it's kind of hard to have this happening here, since we would actually
            // be validating this in the validate function. There is only one problem, namely when overwrite is REPLACE,
            // and document would have already be deleted by here and won't be replaced by anything.
            if (newDocObj == null) {
                return null;
            }

            List<String> tagList = new ArrayList<String>();
            for (String key : mapping.keySet()) {
                String value = data.get(key);
                // TODO: implement proper handling of empty values, for now the test if value is empty is done only for
                // object properties, but not for document metadata. This needs to depend on a parameter.
                if (!key.startsWith("doc.")) {
                    PropertyInterface prop = defaultClass.get(key);

                    if (!StringUtils.isEmpty(value)) {
                        boolean addtotags = false;
                        if (fieldsfortags.contains(key) || fieldsfortags.contains("ALL")) {
                            addtotags = true;
                        }
                        if (prop instanceof ListClass && (((ListClass) prop).isMultiSelect())) {
                            List<String> vallist = new ArrayList<String>();
                            for (String listItem : getAsList(value, listseparator)) {
                                vallist.add(listItem);
                                if (addtotags) {
                                    tagList.add(listItem);
                                }
                            }
                            newDocObj.set(key, vallist, xcontext);
                        } else if (prop instanceof DateClass) {
                            debug("Found date " + value + " for key -" + key + "-");
                            SimpleDateFormat sdf = new SimpleDateFormat(((DateClass) prop).getDateFormat());
                            try {
                                newDocObj.set(key, sdf.parse(value), xcontext);
                            } catch (ParseException exc) {
                                // try to parse with the default date then
                                sdf = new SimpleDateFormat(defaultDateFormat);
                                try {
                                    newDocObj.set(key, sdf.parse(value), xcontext);
                                } catch (ParseException e) {
                                    // now we cannot do much more
                                    debug("Failed to parse date " + value + " for key " + key);
                                }
                            }
                            debug("Date now is " + newDocObj.getDateValue(key) + " for key -" + key + "-");
                            if (addtotags) {
                                tagList.add(value.trim());
                            }
                        } else {
                            newDocObj.set(key, value, xcontext);

                            if (addtotags) {
                                tagList.add(value.trim());
                            }
                        }
                    }
                } else if (key.equals("doc.file")) {
                    // ignore, will be handled by the file function
                } else if (key.equals("doc.author")) {
                    newDoc.setAuthor(value);
                } else if (key.equals("doc.title")) {
                    if (value.length() > 255) {
                        newDoc.setTitle(value.substring(0, 255));
                    } else {
                        newDoc.setTitle(value);
                    }
                } else if (key.equals("doc.parent")) {
                    newDoc.setParent(value);
                } else if (key.equals("doc.content")) {
                    newDoc.setContent(value);
                } else if (key.equals("doc.creator")) {
                    // support for the creator field
                    newDoc.setCreator(value);
                }
            }

            // set tags, only if needed.
            // TODO: fix this test here, it should depend on an "empty overwrites" parameter, which should say whether
            // an empty value is considered significant or not, which should also apply to properties: for now we
            // overwrite it even with an empty tagList, if the fields for tags is set to something and that something is
            // void, maybe we shouldn't
            if (fieldsfortags != null && fieldsfortags.size() > 0) {
                BaseObject newTagsObject =
                    newDoc.getXObject(currentDocumentStringResolver.resolve("XWiki.TagClass",
                        newDoc.getDocumentReference()));
                if (newTagsObject == null) {
                    newTagsObject =
                        newDoc.newXObject(
                            currentDocumentStringResolver.resolve("XWiki.TagClass", newDoc.getDocumentReference()),
                            xcontext);
                }
                newTagsObject.set("tags", tagList, xcontext);
            }

            // set a parent if a parent is empty after import
            // TODO: make this a config parameter
            if (newDoc.getParent() == "") {
                // to the webHome of its space
                newDoc.setParentReference(new EntityReference("WebHome", EntityType.DOCUMENT));
            }

            // polish a bit the content of the document
            if (newDoc.getContent().trim() == "") {
                newDoc.setContent("");
            }

        } else {
            log(result, "Cannot import row " + currentLine.toString() + " because page " + fullName
                + " already exists.");
            return null;
        }

        return newDoc;
    }

    public void saveDocumentWithFiles(XWikiDocument newDoc, Map<String, String> data, List<String> currentLine,
        BatchImportConfiguration config, XWikiContext xcontext, boolean overwrite, boolean simulation,
        boolean overwritefile, boolean fileimport, String datadir, String datadirprefix, ZipFile zipfile,
        List<DocumentReference> savedDocuments, StringBuffer result) throws XWikiException, ZipException, IOException
    {
        String fullName = entityReferenceSerializer.serialize(newDoc.getDocumentReference());

        boolean withFile = false;
        boolean fileOk = false;
        String path = "";

        // if there is a mapping for the doc.file field
        String fileMapping = config.getFieldsMapping().get("doc.file");
        if (fileMapping != null) {
            withFile = true;
            path = getFilePath(datadir, datadirprefix, data.get("doc.file"));
            fileOk = checkFile(zipfile, path);
        }

        if (withFile) {
            if (fileOk) {
                if (debug || simulation) {
                    log(result, "Ready to import row " + currentLine.toString() + "in page " + fullName
                        + " and imported file is ok.");
                    // if we're simulating, pretend that we're saving this document here since normally it would be
                    // saved in the block under which happens only in non-simulation mode
                    if (simulation) {
                        savedDocuments.add(newDoc.getDocumentReference());
                    }
                }

                // Need to import file
                if (simulation == false) {
                    // adding the file to the document
                    String fname = getFileName(data.get("doc.file"));
                    if (newDoc.getAttachment(fname) != null) {
                        debug("Filename " + fname + " already exists in " + fullName);

                        // done here
                        // FIXME: this should depend on the overwrite file parameter or at least on the overwrite
                        // one (since overwritefile seems to be about the behavior of the document content when
                        // there are files attached)
                        new Document(newDoc, xcontext).save();
                        savedDocuments.add(newDoc.getDocumentReference());
                    } else {
                        boolean isDirectory = isDirectory(zipfile, path);
                        if (isDirectory) {
                            addFiles(newDoc, path);

                            // done here, we save pointed files in the file and we're done
                            new Document(newDoc, xcontext).save();
                            savedDocuments.add(newDoc.getDocumentReference());
                        } else {
                            byte[] filedata = getFileData(zipfile, path);
                            if (filedata != null) {
                                addFile(newDoc, filedata, fname);
                                // TODO: why the hell are we doing this here?
                                if (overwrite && overwritefile) {
                                    newDoc.setContent("");
                                }

                                // saving the document, in order to be able to do the import properly after
                                new Document(newDoc, xcontext).save();
                                savedDocuments.add(newDoc.getDocumentReference());

                                // launching the openoffice conversion
                                if (fileimport) {
                                    if (!fname.toLowerCase().endsWith(".pdf")
                                        && (newDoc.getContent().trim() == "" || (overwrite && overwritefile))) {
                                        boolean importResult = false;

                                        try {
                                            OfficeImporterVelocityBridge officeimporter =
                                                new OfficeImporterVelocityBridge(this.cm);
                                            // import the attachment in the content of the document
                                            InputStream fileInputStream = new ByteArrayInputStream(filedata);
                                            XDOMOfficeDocument xdomOfficeDoc =
                                                officeimporter.officeToXDOM(fileInputStream, fname, fullName, true);
                                            importResult =
                                                officeimporter.save(xdomOfficeDoc, fullName, newDoc.getSyntax()
                                                    .toIdString(), null, null, true);
                                        } catch (OfficeImporterException e) {
                                            LOGGER.warn("Failed to import content from office file " + fname
                                                + " to document " + fullName);
                                        }

                                        if (!importResult) {
                                            log(result, "Imported row " + currentLine.toString() + " in page [["
                                                + fullName + "]] but failed importing office file " + path
                                                + " into content.");
                                        } else {
                                            log(result, "Imported row " + currentLine.toString() + " in page [["
                                                + fullName + "]] and imported office file " + path + " into content.");
                                        }

                                        // in case import was unsuccessful let's empty the content again
                                        // to be able to detect it
                                        XWikiDocument newDoc2 =
                                            xcontext.getWiki().getDocument(newDoc.getDocumentReference(), xcontext);
                                        if (newDoc2.getContent().trim() == "") {
                                            newDoc2.setContent("");
                                            new Document(newDoc2, xcontext).save();
                                        }
                                        // clean up open office temporary files
                                        cleanUp();
                                    }
                                } else {
                                    log(result, "Imported row " + currentLine.toString() + " in page " + fullName
                                        + " and did not need to import the office file.");
                                }
                            } else {
                                log(result, "Imported row " + currentLine.toString() + " in page [[" + fullName
                                    + "]] and failed to read the office file.");
                            }
                        }
                    }
                }
            } else {
                log(result, "Cannot import row " + currentLine.toString() + " in page " + fullName
                    + " because imported file " + path + " does not exist.");
                // TODO: this will leave the document unsaved because of the inexistent file, which impacts the data set
                // with the marshalDataToDocumentObjects function (because of the way doImport is written), so maybe
                // this should be configured by an error handling setting (skip row or skip value, for example), the
                // same as we should have for document data
            }
        } else {
            if (debug || simulation) {
                log(result, "Ready to import row " + currentLine.toString() + " in page " + fullName
                    + " (no file attached).");
            }

            // we should save the data
            if (simulation == false) {
                new Document(newDoc, xcontext).save();
                log(result, "Imported row " + currentLine.toString() + " in page [[" + fullName + "]].");
            }
            savedDocuments.add(newDoc.getDocumentReference());
        }
    }

    /**
     * @param config the batch import configuration
     * @return the import file iterator corresponding to the type defined in the configuration.
     */
    protected ImportFileIterator getImportFileIterator(BatchImportConfiguration config) throws ComponentLookupException
    {
        String iteratorHint = config.getType();
        if (StringUtils.isEmpty(iteratorHint)) {
            return cm.lookup(ImportFileIterator.class);
        } else {
            return cm.lookup(ImportFileIterator.class, iteratorHint);
        }
    }

    protected XWikiContext getXWikiContext()
    {
        ExecutionContext ec = execution.getContext();
        XWikiContext xwikicontext = (XWikiContext) ec.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        return xwikicontext;
    }
}
