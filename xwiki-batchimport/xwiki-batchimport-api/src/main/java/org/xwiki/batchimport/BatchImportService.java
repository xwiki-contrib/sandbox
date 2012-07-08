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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.batchimport.log.BatchImportLog;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.api.Property;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Script service for the batch file import to allow using the import from velocity / groovy.
 * 
 * @version $Id$
 */
@Component("batchimport")
public class BatchImportService implements ScriptService, BatchImport
{
    /**
     * Not injected since we want to be able to lookup this at runtime, changing the implementation dynamically (e.g.
     * from groovy).
     */
    private BatchImport batchImport;

    @Inject
    private Execution execution;

    @Inject
    private ComponentManager cm;

    protected static final Logger LOGGER = LoggerFactory.getLogger(BatchImportService.class);

    protected static final String MAPPING_PARAM_PREFIX = "batchimportmapping_";

    @Inject
    @Named("current")
    protected AttachmentReferenceResolver<String> currentAttachmentStringResolver;

    /**
     * @return a brand new empty import configuration, to fill in and pass as parameters.
     */
    public BatchImportConfiguration getConfiguration()
    {
        return new BatchImportConfiguration();
    }

    /**
     * @return an import configuration read from the request, using the following convention for the parameters:
     *         <ul>
     *         <li><tt>batchimportseparator</tt> for {@link BatchImportConfiguration#getCsvSeparator()}</li>
     *         <li><tt>batchimportattachmentref</tt> for {@link BatchImportConfiguration#getAttachmentReference()}</li>
     *         <li><tt>batchimporttype</tt> for {@link BatchImportConfiguration#getType()}</li>
     *         <li><tt>batchimporthasheader</tt> for {@link BatchImportConfiguration#hasHeaderRow()}</li>
     *         <li><tt>batchimportmappingclass</tt> for {@link BatchImportConfiguration#getMappingClassName()}</li>
     *         <li><tt>batchimportmapping_&lt;fieldname&gt;</tt> for all field configurations in
     *         {@link BatchImportConfiguration#getFieldsMapping()}.</li> *
     *         <li><tt>batchimportdefaultspace</tt> for {@link BatchImportConfiguration#getDefaultSpace()}</li>
     *         <li><tt>batchimportemptydocnameprefix</tt> for {@link BatchImportConfiguration#getEmptyDocNamePrefix()}</li>
     *         <li><tt>batchimportdefaultdateformat</tt> for {@link BatchImportConfiguration#getDefaultDateFormat()}</li>
     *         <li><tt>batchimportoverwrite</tt> for {@link BatchImportConfiguration#getOverwrite()}</li>
     *         <li><tt>batchimportdocnamededuplication</tt> for
     *         {@link BatchImportConfiguration#getDocNameDeduplication()}</li>
     *         </ul>
     *         TODO: this method misses some parameters of the config (that might or might not be passable as request
     *         parameters).
     */
    public BatchImportConfiguration readConfigurationFromRequest()
    {
        return readConfigurationFromRequest(getConfiguration());
    }

    /**
     * Fills in the passed configuration from the request. Used to be able to chain read methods on the same
     * configuration, only overwriting the settings contained in the request (leaving the rest unchanged).
     * 
     * @see #readConfigurationFromRequest()
     */
    @SuppressWarnings("unchecked")
    public BatchImportConfiguration readConfigurationFromRequest(BatchImportConfiguration config)
    {
        XWikiRequest request = getRequest();

        String separatorValue = request.getParameter("batchimportseparator");
        // only take into account if it's not empty and it's exactly one
        if (!StringUtils.isEmpty(separatorValue) && separatorValue.length() == 1) {
            config.setCsvSeparator(new Character(separatorValue.charAt(0)));
        }

        try {
            String attachmentRef = request.getParameter("batchimportattachmentref");
            if (!StringUtils.isEmpty(attachmentRef)) {
                AttachmentReferenceResolver<String> refResolver =
                    cm.lookup(AttachmentReferenceResolver.class, "current");
                config.setAttachmentReference(refResolver.resolve(attachmentRef));
            }
        } catch (ComponentLookupException cle) {
            LOGGER.warn("Could not parse attachment reference from request parameters", cle);
        }

        String type = request.getParameter("batchimporttype");
        if (!StringUtils.isEmpty(type)) {
            config.setType(type);
        }

        String hasheader = request.getParameter("batchimporthasheader");
        if (!StringUtils.isEmpty(hasheader)) {
            config.setHeaderRow(Boolean.parseBoolean(hasheader));
        }

        String mappingClass = request.getParameter("batchimportmappingclass");
        if (!StringUtils.isEmpty(mappingClass)) {
            config.setMappingClassName(mappingClass);
        }

        for (Map.Entry<String, String[]> parameter : ((Map<String, String[]>) request.getParameterMap()).entrySet()) {
            if (parameter.getKey().startsWith(MAPPING_PARAM_PREFIX) && parameter.getValue().length > 0
                && !StringUtils.isEmpty(parameter.getValue()[0])) {
                config.addFieldMapping(parameter.getKey().substring(MAPPING_PARAM_PREFIX.length()),
                    parameter.getValue()[0]);
            }
        }

        String defaultSpace = request.getParameter("batchimportdefaultspace");
        if (!StringUtils.isEmpty(defaultSpace)) {
            config.setDefaultSpace(defaultSpace);
        }

        String emptyDocNamePrefix = request.getParameter("batchimportemptydocnameprefix");
        if (!StringUtils.isEmpty(emptyDocNamePrefix)) {
            config.setEmptyDocNamePrefix(emptyDocNamePrefix);
        }

        String defaultDateFormat = request.getParameter("batchimportdefaultdateformat");
        if (!StringUtils.isEmpty(emptyDocNamePrefix)) {
            config.setDefaultDateFormat(defaultDateFormat);
        }

        String overwrite = request.getParameter("batchimportoverwrite");
        if (!StringUtils.isEmpty(overwrite)) {
            config.setOverwrite(overwrite);
        }

        String docNameDeduplication = request.getParameter("batchimportdocnamededuplication");
        if (!StringUtils.isEmpty(docNameDeduplication)) {
            config.setDocNameDeduplication(docNameDeduplication);
        }

        return config;
    }

    /**
     * Reads the batch import configuration from an xwiki object in a document. The class description is
     * BatchImport.BatchImportClass.
     * 
     * @param document the document to read object from
     * @param className the name of the class for the object
     * @return an instance of {@link BatchImportConfiguration}, filled in with data from the object, if any. Otherwise,
     *         an empty batch import configuration.
     */
    public BatchImportConfiguration readConfigurationFromObject(Document document, String className)
    {
        return readConfigurationFromObject(document, className, getConfiguration());
    }

    /**
     * Fills in the passed configuration from the object. Used to be able to chain read methods on the same
     * configuration, only overwriting the settings contained in the object (leaving the rest unchanged).
     * 
     * @see #readConfigurationFromObject(Document, String)
     */
    public BatchImportConfiguration readConfigurationFromObject(Document document, String className,
        BatchImportConfiguration config)
    {
        Object configObject = document.getObject(className);

        if (configObject != null) {
            // fill it in
            Property sourceFileNameProp = configObject.getProperty("metadatafilename");
            String sourceFileName = sourceFileNameProp != null ? (String) sourceFileNameProp.getValue() : null;
            if (!StringUtils.isEmpty(sourceFileName)) {
                // use as attachment, relative to the document storing the object
                config.setAttachmentReference(currentAttachmentStringResolver.resolve(sourceFileName,
                    document.getDocumentReference()));
            }

            Property mappingClassnameProp = configObject.getProperty("classname");
            String mappingClassname = mappingClassnameProp != null ? (String) mappingClassnameProp.getValue() : null;
            if (!StringUtils.isEmpty(mappingClassname)) {
                config.setMappingClassName(mappingClassname);
            }

            Property defaultSpaceProp = configObject.getProperty("space");
            String defaultSpace = defaultSpaceProp != null ? (String) defaultSpaceProp.getValue() : null;
            if (!StringUtils.isEmpty(defaultSpace)) {
                config.setDefaultSpace(defaultSpace);
            }

            Property emptyDocnamePagePrefixProp = configObject.getProperty("pageprefix");
            String emptyDocnamePagePrefix =
                emptyDocnamePagePrefixProp != null ? (String) emptyDocnamePagePrefixProp.getValue() : null;
            if (!StringUtils.isEmpty(emptyDocnamePagePrefix)) {
                config.setEmptyDocNamePrefix(emptyDocnamePagePrefix);
            }

            Property listSeparatorProp = configObject.getProperty("listseparator");
            String listSeparatorString = listSeparatorProp != null ? (String) listSeparatorProp.getValue() : null;
            if (!StringUtils.isEmpty(listSeparatorString)) {
                config.setListSeparator(listSeparatorString.charAt(0));
            }

            Property mappingProperty = configObject.getProperty("mapping");
            String mappingString = mappingProperty != null ? (String) mappingProperty.getValue() : null;
            if (!StringUtils.isEmpty(mappingString)) {
                for (String item : mappingString.split("\n")) {
                    String[] res = item.split("=");
                    if (res.length == 2) {
                        // the other way around since the class expects pairs (column, xwiki field) in its field and the
                        // config uses the pairs the other way around
                        // if multiple occurrences of a xwiki field are found (multiple mappings), the last one will be
                        // used
                        config.addFieldMapping(res[1].trim(), res[0].trim());
                    } else {
                        LOGGER.debug("Mapping in the wrong format: " + item + " in document "
                            + document.getPrefixedFullName());
                    }
                }
            }

            // TODO:
            // datafilename (datafilename: String)
            // datafileprefix (datafileprefix: String)
            // fieldsfortags (fieldsfortags: String)
            // fileupload (fileupload: Boolean)
            // fileimport (fileimport: Boolean)
        }

        return config;
    }

    protected XWikiRequest getRequest()
    {
        ExecutionContext ec = execution.getContext();
        XWikiContext xwikicontext = (XWikiContext) ec.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        return xwikicontext.getRequest();
    }

    protected XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
    }

    @Override
    public List<String> getColumnHeaders(BatchImportConfiguration config)
    {
        try {
            return getBatchImport().getColumnHeaders(config);
        } catch (IOException e) {
            LOGGER.error("Cannot get column headers for config: " + config.toString(), e);
            putExceptionInContext(e);
        }

        return null;
    }

    @Override
    public BatchImportLog doImport(BatchImportConfiguration config, boolean withFiles, boolean overwritefile, boolean simulation)
    {
        try {
            return this.getBatchImport().doImport(config, withFiles, overwritefile, simulation);
        } catch (IOException e) {
            LOGGER.error("Could not execute import for config " + config.toString(), e);
            putExceptionInContext(e);
        } catch (XWikiException e) {
            LOGGER.error("Could not execute import for config " + config.toString(), e);
            putExceptionInContext(e);
        }

        return null;
    }

    @Override
    public String deleteExistingDocuments(String className, String wiki, String space)
    {
        try {
            return this.getBatchImport().deleteExistingDocuments(className, wiki, space);
        } catch (XWikiException e) {
            LOGGER.error("Could not delete existing documents for wiki=" + wiki + ", space=" + space + ", className="
                + className, e);
            putExceptionInContext(e);
        }
        return null;
    }

    protected BatchImport getBatchImport()
    {
        if (this.batchImport == null) {
            try {
                this.batchImport = cm.lookup(BatchImport.class);
            } catch (ComponentLookupException e) {
                LOGGER.error("Could not find batch import implementation", e);
            }
        }

        return this.batchImport;
    }

    /**
     * Resets the internal implementation of the batch import, useful when changing the implementation from groovy, to
     * be able to force this to reload its inner implementation.
     */
    public void resetBatchImportImplementation()
    {
        // nullify it, it will be loaded lazily on next call to #getBatchImport()
        this.batchImport = null;
    }

    /**
     * Put an exception in the xwiki context, to be read after from velocity to display error.
     * 
     * @param e the exception to put in the context
     */
    protected void putExceptionInContext(Exception e)
    {
        getXWikiContext().put(getXContextExceptionKey(), e);
    }

    /**
     * Since otherwise we need programming rights to get the exception from context.
     * 
     * @return the last exception put in context
     */
    public Exception getExceptionFromContext()
    {
        return (Exception) getXWikiContext().get(getXContextExceptionKey());
    }

    public String getXContextExceptionKey()
    {
        return "batchimportexception";
    }
}
