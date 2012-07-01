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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Script service for the batch file import to allow using the import from velocity / groovy.
 * 
 * @version $Id$
 */
@Component("batchimport")
public class BatchImportService implements ScriptService, BatchImport
{
    @Inject
    private BatchImport batchImport;

    @Inject
    private Execution execution;

    @Inject
    private ComponentManager cm;

    protected static final Logger LOGGER = LoggerFactory.getLogger(BatchImportService.class);

    private static final String MAPPING_PARAM_PREFIX = "batchimportmapping_";

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
     *         </ul>
     */
    @SuppressWarnings("unchecked")
    public BatchImportConfiguration readConfigurationFromRequest()
    {
        BatchImportConfiguration config = getConfiguration();

        XWikiRequest request = getRequest();

        String separatorValue = request.getParameter("batchimportseparator");
        // only take into account if it's not empty and it's exactly one
        if (!StringUtils.isEmpty(separatorValue) && separatorValue.length() == 1) {
            config.setCsvSeparator(new Character(separatorValue.charAt(0)));
        }

        try {
            String attachmentRef = request.getParameter("batchimportattachmentref");
            if (!StringUtils.isEmpty(attachmentRef)) {
                @SuppressWarnings("unchecked")
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
            return batchImport.getColumnHeaders(config);
        } catch (IOException e) {
            putExceptionInContext(e);
        }

        return null;
    }

    @Override
    public String doImport(BatchImportConfiguration config, boolean withFiles, boolean overwrite,
        boolean overwritefile, boolean simulation, boolean convertToUpperCase) throws IOException, XWikiException
    {
        try {
            return this.batchImport.doImport(config, withFiles, overwrite, overwritefile, simulation,
                convertToUpperCase);
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
            return this.batchImport.deleteExistingDocuments(className, wiki, space);
        } catch (XWikiException e) {
            LOGGER.error("Could not delete existing documents for wiki=" + wiki + ", space=" + space + ", className="
                + className, e);
            putExceptionInContext(e);
        }
        return null;
    }

    /**
     * TODO: fix this shit, exceptions are unreadable because velocity needs programming rights to get the internal
     * context / keys from internal context.
     * 
     * @param e
     */
    protected void putExceptionInContext(Exception e)
    {
        getXWikiContext().put(getXContextExceptionKey(), e);
    }

    public String getXContextExceptionKey()
    {
        return "batchimportexception";
    }
}
