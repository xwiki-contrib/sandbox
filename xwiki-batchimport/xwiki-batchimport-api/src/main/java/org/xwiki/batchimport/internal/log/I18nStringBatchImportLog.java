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
package org.xwiki.batchimport.internal.log;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiMessageTool;

/**
 * String Batch import log which generates i18n messages based on the xwiki message tool.
 * 
 * @version $Id$
 */
@Component("i18n")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class I18nStringBatchImportLog extends StringBatchImportLog
{
    @Inject
    protected Execution execution;

    protected XWikiMessageTool messageTool;

    protected final static String PREFIX = "batchimport.log.";

    @Override
    protected String getPrettyMessage(String messageKey, Object... parameters)
    {
        return this.getMessageTool().get(PREFIX + messageKey, parameters);
    }

    /**
     * Lazy initialize message tool.
     * 
     * @return
     */
    protected XWikiMessageTool getMessageTool()
    {
        if (this.messageTool == null) {
            ExecutionContext ec = execution.getContext();
            XWikiContext xwikicontext = (XWikiContext) ec.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
            this.messageTool = xwikicontext.getMessageTool();
        }

        return this.messageTool;
    }
}
