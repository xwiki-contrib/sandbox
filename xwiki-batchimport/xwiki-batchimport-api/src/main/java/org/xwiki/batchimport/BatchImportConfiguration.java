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
package org.xwiki.batchimport;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.xwiki.model.reference.AttachmentReference;

/**
 * Holds the configuration of an import of a file (csv, xls, etc) in xwiki objects in documents. Used to replace a
 * loong, loong list of parameters that the import function should get, and to handle part of the defaults (such as
 * separator, default space, has header, encoding, etc).
 * 
 * @version $Id$
 */
public class BatchImportConfiguration extends HashMap<Object, Object>
{
    private static final long serialVersionUID = 5281163601013173290L;

    protected static final String DEFAULT_SPACE = "Main";

    protected static final Character DEFAULT_SEPARATOR = ',';

    protected static final Character DEFAULT_TEXT_DELIMITER = '"';

    protected static final Character DEFAULT_LIST_SEPARATOR = ',';

    // TODO: maybe this should be read from the wiki encoding
    protected static final String DEFAULT_ENCODING = "UTF-8";

    public static enum Overwrite
    {
        /**
         * Don't replace existing data, skip duplicates. Note that when set as document name deduplication strategy,
         * this causes the duplicate row to be skipped even if the first row was not saved because of errors, for
         * example.
         */
        SKIP,
        /**
         * "Stack" rows together, as if they were the same row. When used as deduplication strategy, if there are
         * multiple values set on the same column (by different rows), the last one will win.
         */
        UPDATE,
        /**
         * Replace the old document with the new one, that is, clear object, attachments, etc. <br />
         * Does not apply as document name deduplication strategy, as it would mean the same thing as {@link #UPDATE},
         * actually the setter is converting it to {@link #UPDATE}.
         */
        REPLACE,
        /**
         * Auto-generate a new document name every time an existing name is found. As doc name deduplication strategy,
         * this will generate the same names for subsequent imports of the same file, as overwrite option, this will
         * generate different names for subsequent imports, depending on the actual data on the wiki.<br />
         */
        GENERATE_NEW;
    }

    public AttachmentReference getAttachmentReference()
    {
        return (AttachmentReference) this.get("attachmentref");
    }

    public void setAttachmentReference(AttachmentReference reference)
    {
        this.put("attachmentref", reference);
    }

    /**
     * @return the input stream to the file to import from, if any configured. If both input stream and attachment
     *         reference are configured ( {@link #getAttachmentReference()} ), input stream has priority.
     */
    public InputStream getFileInputStream()
    {
        return (InputStream) this.get("inputstream");
    }

    /**
     * @param stream the input stream to the file to import from, if any configured. If both input stream and attachment
     *            reference are configured ( {@link #getAttachmentReference()} ), input stream has priority.
     */
    public void setFileInputStream(InputStream stream)
    {
        this.put("inputstream", stream);
    }

    public Character getCsvSeparator()
    {
        Character separator = (Character) this.get("separator");

        return separator == null ? DEFAULT_SEPARATOR : separator;
    }

    public void setCsvSeparator(Character separator)
    {
        this.put("separator", separator);
    }

    public Character getCsvTextDelimiter()
    {
        Character textDelimiter = (Character) this.get("textdelimiter");

        return textDelimiter == null ? DEFAULT_TEXT_DELIMITER : textDelimiter;
    }

    public void setCsvTextDelimiter(Character textDelimiter)
    {
        this.put("textdelimiter", textDelimiter);
    }

    /**
     * @return whether the first row in the file should be handled as a header row (containing column names) or not
     */
    public boolean hasHeaderRow()
    {
        Boolean hasheader = (Boolean) this.get("hasheader");

        return hasheader == null ? false : hasheader;
    }

    /**
     * @param hasheader whether the first row in the file should be handled as a header row (containing column names) or
     *            not
     */
    public void setHeaderRow(boolean hasheader)
    {
        this.put("hasheader", hasheader);
    }

    /**
     * @return the name of the xwiki classname of which type objects will be created and data from the file saved,
     *         according to the correspondences returned by {@link #getFieldsMapping()}
     */
    public String getMappingClassName()
    {
        return (String) this.get("mappingclass");
    }

    /**
     * @param className the name of the xwiki classname of which type objects will be created and data from the file
     *            saved, according to the correspondences returned by {@link #getFieldsMapping()}
     */
    public void setMappingClassName(String className)
    {
        this.put("mappingclass", className);
    }

    /**
     * @return a map of correspondence between a field name from the mapping class (as returned by
     *         {@link #getMappingClassName()}) and a column in the source file. Note that the column should be
     *         interpreted depending on the result of {@link #hasHeaderRow()}: if it has header row, the source file
     *         column name will be the name of the column in the header row, otherwise it should be a string containing
     *         the number of the column, to be parsed as integer. This is so that columns mappings can be reused for
     *         multiple imports, regardless of whether the columns order change or not. <br />
     *         There are a few special mapping fields, to configure mapping for various document metadata:
     *         <ul>
     *         <li>doc.title : the title of the page</li>
     *         <li>doc.name : the name of the page (URL of the page in the space)</li>
     *         <li>doc.space : the name of the space</li>
     *         <li>doc.parent : parent field</li>
     *         <li>doc.author : author of the document in XWiki format</li>
     *         <li>doc.creator : creator of the document in XWiki format</li>
     *         <li>doc.content : the content of the page. Something useful is in the source file to set a column named
     *         "content" and put in that column "{{include document='CODESpace.MyClassSheet'}}".</li>
     *         <li>doc.file : declares a list of files attached to the page</li> </li>
     *         </ul>
     *         <b>Note that this function returns a clone of the mapping, so putting directly in the map returned by
     *         this function will not alter the configuration's mapping. Use {@link #addAllFieldsMapping(Map)} and
     *         {@link #addFieldMapping(String, String)} to add mappings.</b>
     */
    public Map<String, String> getFieldsMapping()
    {
        // return a clone so that the mapping cannot be changed with this getter, must be put through the add methods
        Map<String, String> result = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) this.get("fieldsmapping");

        if (mapping != null) {
            result.putAll(mapping);
        }

        return result;
    }

    /**
     * Adds a new field mapping to the existing mapping.
     * 
     * @param fieldName the field name in the xwiki class
     * @param column the column name in the source file. This value should be interpreted as described by
     *            {@link #getFieldsMapping()}
     */
    public void addFieldMapping(String fieldName, String column)
    {
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) this.get("fieldsmapping");
        if (mapping == null) {
            mapping = new HashMap<String, String>();
            this.put("fieldsmapping", mapping);
        }

        mapping.put(fieldName, column);
    }

    /**
     * @param fieldName the name of the field for which the mapping is to be removed
     */
    public void removeFieldMapping(String fieldName)
    {
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) this.get("fieldsmapping");
        if (mapping != null) {
            mapping.remove(fieldName);
        }
    }

    /**
     * @param newMappings the list of mappings to add to the existing mappings, shortcut for repeated
     *            {@link #addFieldMapping(String, String)}
     */
    public void addAllFieldsMapping(Map<String, String> newMappings)
    {
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) this.get("fieldsmapping");
        if (mapping == null) {
            mapping = new HashMap<String, String>();
            this.put("fieldsmapping", mapping);
        }

        mapping.putAll(newMappings);
    }

    /**
     * Clears existing field mapping.
     */
    public void clearFieldsMapping()
    {
        @SuppressWarnings("unchecked")
        Map<String, String> mapping = (Map<String, String>) this.get("fieldsmapping");
        if (mapping != null) {
            mapping.clear();
        }
    }

    /**
     * @return default space to create documents in, if no space mapping is set
     */
    public String getDefaultSpace()
    {
        String defaultSpace = (String) this.get("defaultspace");

        return defaultSpace == null ? DEFAULT_SPACE : defaultSpace;
    }

    /**
     * @param space default space to create documents in, if no space mapping is set
     */
    public void setDefaultSpace(String space)
    {
        this.put("defaultspace", space);
    }

    /**
     * @return The wiki to which the documents should be imported: if empty, the current wiki will be used.
     */
    public String getWiki()
    {
        return (String) this.get("defaultwiki");
    }

    /**
     * @param wikiName The wiki to which the documents should be imported: if empty, the current wiki will be used.
     */
    public void setWiki(String wikiName)
    {
        this.put("defaultwiki", wikiName);
    }

    public String getEncoding()
    {
        String encoding = (String) this.get("encoding");
        return encoding == null ? DEFAULT_ENCODING : encoding;
    }

    public void setEncoding(String enconding)
    {
        this.put("encoding", enconding);
    }

    /**
     * @return The import type, that is, how the input source (stream or attachment) is to be used by the importer. This
     *         will be used as a hint to lookup an importfileiterator, so if you use a custom importfileiterator, just
     *         pass the appropriate hint in here as the type of import.
     */
    public String getType()
    {
        return (String) this.get("importtype");
    }

    /**
     * @param type The import type, that is, how the input source (stream or attachment) is to be used by the importer.
     *            This will be used as a hint to lookup an importfileiterator, so if you use a custom
     *            importfileiterator, just pass the appropriate hint in here as the type of import.
     */
    public void setType(String type)
    {
        this.put("importtype", type);
    }

    /**
     * @return the list separator used to separate values of lists in the mapping and in the source file, for
     *         multiselect lists. Defaults to ','.
     */
    public Character getListSeparator()
    {
        Character listSeparator = (Character) this.get("listseparator");
        return listSeparator == null ? DEFAULT_LIST_SEPARATOR : listSeparator;
    }

    /**
     * @param separator the list separator used to separate values of lists in the mapping and in the source file, for
     *            multiselect lists. Defaults to ','.
     */
    public void setListSeparator(Character separator)
    {
        this.put("listseparator", separator);
    }

    /**
     * @return prefix to create pagenames for rows for which the mapping to doc.name is an empty value (or there is no
     *         mapping). If unset, such rows will be ignored, if set, they won't, so it also serves as a flag. Empty
     *         string will be considered "unset".
     */
    public String getEmptyDocNamePrefix()
    {
        return (String) this.get("emptydocdameprefix");
    }

    /**
     * @param docNamePrefix prefix to create pagenames for rows for which the mapping to doc.name is an empty value (or
     *            there is no mapping). If unset, such rows will be ignored, if set, they won't, so it also serves as a
     *            flag. Empty string will be considered "unset".
     */
    public void setEmptyDocNamePrefix(String docNamePrefix)
    {
        this.put("emptydocdameprefix", docNamePrefix);
    }

    /**
     * @return the default date format, to be used if the columns in the source file mapped to a field of type date in
     *         the xwiki class cannot be formatted using the format set in the field in xwiki.
     * @see {@link SimpleDateFormat}
     */
    public String getDefaultDateFormat()
    {
        return (String) this.get("defaultdateformat");
    }

    /**
     * @param defaultDateFormat the default date format, to be used if the columns in the source file mapped to a field
     *            of type date in the xwiki class cannot be formatted using the format set in the field in xwiki.
     * @see {@link SimpleDateFormat}
     */
    public void setDefaultDateFormat(String defaultDateFormat)
    {
        this.put("defaultdateformat", defaultDateFormat);
    }

    /**
     * @return whether names read from source file (doc.name and doc.space) should be passed through clearName before
     *         being used. False by default. This does not apply to {@link #getDefaultSpace()} and
     *         {@link #getEmptyDocNamePrefix()}, if these need to be cleared, they need to be cleared when passed to the
     *         importer.
     */
    public boolean getClearName()
    {
        Boolean clearName = (Boolean) this.get("clearname");

        return clearName == null ? false : clearName;
    }

    /**
     * @param clearName whether names read from source file (doc.name and doc.space) should be passed through clearName
     *            before being used. False by default. This does not apply to {@link #getDefaultSpace()} and
     *            {@link #getEmptyDocNamePrefix()}, if these need to be cleared, they need to be cleared when passed to
     *            the importer.
     */
    public void setClearName(boolean clearName)
    {
        this.put("clearname", clearName);
    }

    /**
     * @return How should new data be compared to data existing in the wiki, based on doc.name.
     */
    public Overwrite getOverwrite()
    {
        Overwrite outerOverwrite = (Overwrite) this.get("outeroverwrite");
        if (outerOverwrite == null) {
            return Overwrite.SKIP;
        } else {
            return outerOverwrite;
        }
    }

    /**
     * @return the strategy for document name deduplication, in case the chosen column mapped on doc.name is not unique.
     *         Possible values are {@link Overwrite#SKIP} (to ignore the second row with the same doc.name),
     *         {@link Overwrite#UPDATE} (to add data from the second row to the same document as the data from the first
     *         row was added) and {@link Overwrite#GENERATE_NEW} (to generate a new name, unique per imported file --
     *         but not necessary unique in the wiki).
     */
    public Overwrite getDocNameDeduplication()
    {
        Overwrite innerOverwrite = (Overwrite) this.get("inneroverwrite");
        if (innerOverwrite == null) {
            return Overwrite.SKIP;
        } else {
            return innerOverwrite;
        }
    }

    public void setOverwrite(Overwrite overwrite)
    {
        this.put("outeroverwrite", overwrite);
    }

    public void setOverwrite(String overwrite)
    {
        try {
            this.setOverwrite(Overwrite.valueOf(overwrite == null ? null : overwrite.toUpperCase()));
        } catch (IllegalArgumentException e) {
            this.setOverwrite(Overwrite.SKIP);
        }
    }

    public void setDocNameDeduplication(Overwrite overwrite)
    {
        Overwrite normalizedValue = overwrite;
        if (overwrite == Overwrite.REPLACE) {
            normalizedValue = Overwrite.UPDATE;
        }
        this.put("inneroverwrite", normalizedValue);
    }

    public void setDocNameDeduplication(String overwrite)
    {
        try {
            this.setDocNameDeduplication(Overwrite.valueOf(overwrite == null ? null : overwrite.toUpperCase()));
        } catch (IllegalArgumentException e) {
            this.setDocNameDeduplication(Overwrite.SKIP);
        }
    }
}
