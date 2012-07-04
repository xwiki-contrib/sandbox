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
        debugMessage += message + "\n";
        result.append(message);
        result.append("\n");
        System.out.println(message);
    }

    public void debug(StringBuffer result, String message)
    {
        debugMessage += message + "\n";
        System.out.println(message);
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
            if (valueIndex >= 0) {
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

    public DocumentReference getPageName(Map<String, String> data, String wiki, String defaultSpace,
        String defaultPrefix, int rowIndex, List<DocumentReference> docNameList, boolean ignoreEmpty, boolean clearNames)
    {
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

        int counter = 0;
        // TODO: the behaviour here needs to depend on a configuration, we could configure it to overwrite with new
        // values than create a new document
        while (docNameList.contains(pageName)) {
            counter++;
            pageName = prepareDocumentReference(wiki, space, name + counter);
        }
        docNameList.add(pageName);
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
            if (debug) {
                System.out.println("Checking if file ${path} exists on disk");
            }
            File file = new File(path);
            return file.exists();
        } else {
            String newpath = path;
            ZipEntry zipentry = zipfile.getEntry(newpath);
            if (debug) {
                System.out.println("Checking if file ${newpath} exists in zip");
            }
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
            debug(new StringBuffer(), "Adding file " + file.getName());
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
                if (debug) {
                    System.out.println("Filename ${filename} already exists in " + newDoc.getPrefixedFullName() + ".");
                }
                return;
            }

            // TODO: I would be very surprised that this still works since we changed attachment manipulation in the
            // mean time
            XWikiAttachment attachment = new XWikiAttachment();
            newDoc.getAttachmentList().add(attachment);
            attachment.setContent(filedata);
            attachment.setFilename(filename);
            attachment.setAuthor(xcontext.getUser());
            // Add the attachment to the document
            attachment.setDoc(newDoc);
            newDoc.saveAttachmentContent(attachment, xcontext);
        } catch (Throwable e) {
            System.out.println("Filename ${filename} could not be attached because of Exception: " + e.getMessage());
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

    public String doImport(BatchImportConfiguration config, boolean withFiles, boolean overwrite,
        boolean overwritefile, boolean simulation) throws IOException, XWikiException
    {
        XWikiContext xcontext = getXWikiContext();
        XWiki xwiki = xcontext.getWiki();

        Document doc = new Document(xcontext.getDoc(), xcontext);

        StringBuffer result = new StringBuffer();

        // the excel file to import
        ImportFileIterator metadatafilename = null;
        try {
            metadatafilename = getImportFileIterator(config);
        } catch (ComponentLookupException e) {
            // TODO: log an exception here. Or, since the exception will be handled at the service level, we could throw
            // IOException directly from the getFileIterator method
            throw new IOException("Could not find an import file reader for the configuration: " + config.toString(), e);
        }
        // mapping from the class fields to source file columns
        Map<String, String> mapping = config.getFieldsMapping();

        // -------------------- Not transformed to config yet, will not work ---------------------//
        // attach files referred in the column _file to the document
        boolean fileupload =
            (Integer) doc.getValue("fileupload") == null || ((Integer) doc.getValue("fileupload")).equals(0) ? false
                : true;
        // use office importer to import the content from the column _file to the document content
        boolean fileimport =
            (Integer) doc.getValue("fileimport") == null || ((Integer) doc.getValue("fileimport")).equals(0) ? false
                : true;
        // directory or zip file where the referenced files are stored. Directory on disk.
        String datadir = (String) doc.getValue("datafilename");
        // path of the files inside the zip
        String datadirprefix = (String) doc.getValue("datafileprefix");
        // column in the xls that will turn into tags
        // TODO: this tags needs to be reimplemented, now it works only with xwiki fields in the list: so you can add
        // something in the tags only if you import it as well. You should be able to configure it to be a column in the
        // csv / xls and that column needs to be handled as a list with the list separator.
        List<String> fieldsfortags = getAsList((String) doc.getValue("fieldsfortags"), config.getListSeparator());
        // -------------------- ----------------------------- ---------------------//

        // default space where to put the documents if no space is specified in the mapping
        String defaultSpace = config.getDefaultSpace();
        // pagename prefix used to automatically generate page names, when _name is not provided
        String defaultPrefix = config.getEmptyDocNamePrefix();
        boolean ignoreEmpty = StringUtils.isEmpty(defaultPrefix);
        // class to map data to (objects of this class will be created)
        String defaultClassName = config.getMappingClassName();
        DocumentReference defaultClassReference =
            currentDocumentStringResolver.resolve(defaultClassName, StringUtils.isEmpty(config.getWiki()) ? null
                : new WikiReference(config.getWiki()));
        BaseClass defaultClass = xwiki.getXClass(defaultClassReference, xcontext);

        // separator for lists
        Character listseparator = config.getListSeparator();

        // default date format used to parse dates from the source file
        String defaultDateFormat = config.getDefaultDateFormat();
        if (StringUtils.isEmpty(defaultDateFormat)) {
            // get it from preferences, hoping that it's set
            defaultDateFormat = xcontext.getWiki().getXWikiPreference("dateformat", xcontext);
        }
        // whether this file has header row or not (whether first line needs to be imported or not)
        boolean hasHeaderRow = config.hasHeaderRow();

        // list of documents, used to check that a document was not already created.
        // TODO: put this in a configuration, the behaviour for documents that already exist
        List<DocumentReference> docNameList = new ArrayList<DocumentReference>();

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
                        System.out.println("Found zip entry: " + zipe.getName());
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

        if (debug) {
            debug(result, "Headers are: ${headers}");
            debug(result, "Mapping is: ${mapping}");
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

        while (currentLine != null) {
            System.out.println("Processing row " + currentLine.toString() + ".");

            Map<String, String> data = getData(currentLine, mapping, headers);
            if (data == null) {
                break;
            }

            if (debug) {
                debug(result, "Row " + currentLine.toString() + " data is: " + data.toString() + "");
            }
            DocumentReference pageName =
                getPageName(data, config.getWiki(), defaultSpace, defaultPrefix, rowIndex, docNameList, ignoreEmpty,
                    config.getClearName());
            // check if the documents with an empty _name need to be created
            if (pageName != null) {
                XWikiDocument newDoc = xwiki.getDocument(pageName, xcontext);
                if (newDoc.isNew() || overwrite) {
                    boolean withFile = false;
                    boolean fileOk = true;
                    String path = "";
                    BaseObject newDocObj = null;
                    if (defaultClassName != null && defaultClassName != "") {
                        newDocObj = newDoc.getXObject(defaultClassReference);
                        if (newDocObj == null) {
                            newDocObj = newDoc.newXObject(defaultClassReference, xcontext);
                        }
                    }
                    // if no object, don't continue
                    if (newDocObj == null) {
                        continue;
                    }

                    List<String> tagList = new ArrayList<String>();
                    for (String key : mapping.keySet()) {
                        String value = data.get(key);
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
                                    debug(result, "Found date " + value + " for key -" + key + "-");
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
                                            debug(result, "Failed to parse date " + value + " for key " + key);
                                        }
                                    }
                                    debug(result, "Date now is " + newDocObj.getDateValue(key) + " for key -" + key
                                        + "-");
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
                            withFile = true;
                            if (withFiles) {
                                path = getFilePath(datadir, datadirprefix, data.get("doc.file"));
                                fileOk = checkFile(zipfile, path);
                            }
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
                    // TODO: fix this test here, maybe it should depend on the overwrite parameter: for now we overwrite
                    // it even with an empty list, if the fields for tags is set to something and that something is
                    // void, maybe we shouldn't
                    if (fieldsfortags != null && fieldsfortags.size() > 0) {
                        BaseObject newTagsObject =
                            newDoc.getXObject(currentDocumentStringResolver.resolve("XWiki.TagClass",
                                newDoc.getDocumentReference()));
                        if (newTagsObject == null) {
                            newTagsObject =
                                newDoc.newXObject(
                                    currentDocumentStringResolver.resolve("XWiki.TagClass",
                                        newDoc.getDocumentReference()), xcontext);
                        }
                        newTagsObject.set("tags", tagList, xcontext);
                    }

                    // set a parent if a parent is empty after import
                    // TODO: make this a config parameter
                    if (newDoc.getParent() == "") {
                        // to the webHome of its space
                        newDoc.setParentReference(new EntityReference("WebHome", EntityType.DOCUMENT));
                    }

                    // TODO: I wonder why we do this here
                    if (newDoc.getContent().trim() == "") {
                        newDoc.setContent("");
                    }

                    if (withFile) {
                        if (withFiles) {
                            if (fileOk) {
                                if (debug || simulation) {
                                    log(result, "Ready to import row " + currentLine.toString() + "in page " + pageName
                                        + " and imported file is ok.");
                                }

                                // Need to import file
                                if (simulation == false) {
                                    // adding the file to the document
                                    String fname = getFileName(data.get("doc.file"));
                                    if (newDoc.getAttachment(fname) != null) {
                                        if (debug) {
                                            System.out.println("Filename " + fname + " already exists in "
                                                + newDoc.getPrefixedFullName());
                                        }

                                        // saving the document
                                        // TODO: why exactly are we saving here?
                                        // TODO: fix this because it's very ugly but apparently that function exists
                                        // like that only there
                                        new Document(newDoc, xcontext).save();
                                    } else {
                                        boolean isDirectory = isDirectory(zipfile, path);
                                        if (isDirectory) {
                                            addFiles(newDoc, path);

                                            // saving the document
                                            // TODO: fix this because it's very ugly but apparently that function exists
                                            // like that only there
                                            new Document(newDoc, xcontext).save();
                                        } else {
                                            byte[] filedata = getFileData(zipfile, path);
                                            if (filedata != null) {
                                                addFile(newDoc, filedata, fname);
                                                if (overwrite && overwritefile) {
                                                    newDoc.setContent("");
                                                }

                                                // saving the document
                                                // TODO: fix this because it's very ugly but apparently that function
                                                // exists like that only there
                                                new Document(newDoc, xcontext).save();

                                                // launching the openoffice conversion
                                                if (fileimport) {
                                                    if (!fname.toLowerCase().endsWith(".pdf")
                                                        && (newDoc.getContent().trim() == "" || (overwrite && overwritefile))) {
                                                        boolean importResult = false;

                                                        try {
                                                            OfficeImporterVelocityBridge officeimporter =
                                                                new OfficeImporterVelocityBridge(this.cm);
                                                            // import the attachment in the content of the document
                                                            InputStream fileInputStream =
                                                                new ByteArrayInputStream(filedata);
                                                            XDOMOfficeDocument xdomOfficeDoc =
                                                                officeimporter.officeToXDOM(fileInputStream, fname,
                                                                    newDoc.getPrefixedFullName(), true);
                                                            importResult =
                                                                officeimporter.save(xdomOfficeDoc, newDoc
                                                                    .getPrefixedFullName(), newDoc.getSyntax()
                                                                    .toIdString(), null, null, true);
                                                        } catch (OfficeImporterException e) {
                                                            LOGGER.warn("Failed to import content from office file "
                                                                + fname + " to document "
                                                                + newDoc.getPrefixedFullName());
                                                        }

                                                        if (!importResult) {
                                                            log(result, "Imported row " + currentLine.toString()
                                                                + " in page [[" + newDoc.getPrefixedFullName()
                                                                + "]] but failed importing office file " + path
                                                                + " into content.");
                                                        } else {
                                                            log(result, "Imported row " + currentLine.toString()
                                                                + " in page [[" + newDoc.getPrefixedFullName()
                                                                + "]] and imported office file " + path
                                                                + " into content.");
                                                        }

                                                        // in case import was unsuccesfull let's empty the content again
                                                        // to be able to detect it
                                                        XWikiDocument newDoc2 = xwiki.getDocument(pageName, xcontext);
                                                        if (newDoc2.getContent().trim() == "") {
                                                            newDoc2.setContent("");
                                                            // TODO: fix this because it's very ugly but apparently that
                                                            // function exists like that only there
                                                            new Document(newDoc, xcontext).save();
                                                        }
                                                        // cleaup oo temp files
                                                        // TODO: we didn't port this because there is no office importer
                                                        // cleanUp();
                                                    }
                                                } else {
                                                    log(result, "Imported row " + currentLine.toString() + " in page "
                                                        + pageName + " and did not need to import the office file.");
                                                }
                                            } else {
                                                log(result, "Imported row " + currentLine.toString() + " in page [["
                                                    + pageName + "]] and failed to read the office file.");
                                            }
                                        }
                                    }
                                }
                            } else {
                                String fname = data.get("doc.file");
                                log(result, "Cannot import row " + currentLine.toString() + " in page " + pageName
                                    + " because imported file ${path} does not exist.");
                            }
                        } else {
                            if (debug || simulation) {
                                log(result, "Ready to import row " + currentLine.toString() + " in page " + pageName
                                    + " without file.");
                            }

                            // we should save the data
                            if (simulation == false) {
                                // TODO: fix this because it's very ugly but apparently that function exists like that
                                // only there
                                new Document(newDoc, xcontext).save();
                                log(result, "Imported row " + currentLine.toString() + " in page [[" + pageName + "]].");
                            }
                        }
                    } else {
                        if (debug || simulation) {
                            log(result, "Ready to import row " + currentLine.toString() + " in page " + pageName
                                + " (no file attached).");
                        }

                        // we should save the data
                        if (simulation == false) {
                            // TODO: fix this because it's very ugly but apparently that function exists like that only
                            // there
                            new Document(newDoc, xcontext).save();
                            log(result, "Imported row " + currentLine.toString() + " in page [[" + pageName + "]].");
                        }
                    }
                } else {
                    log(result, "Cannot import row " + currentLine.toString() + " because page " + pageName
                        + " already exists.");
                }
            } else {
                log(result, "Ignore " + currentLine.toString() + " because page name is empty.");
            }

            currentLine = metadatafilename.readNextLine();
            rowIndex++;
        }

        log(result, "Processing finished.");

        return result.toString();
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
