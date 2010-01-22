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

package org.xwiki.annotation.io.internal;

import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Default {@link IOTargetService} implementation, based on resolving XWiki documents and object properties as
 * annotations targets. The references manipulated by this implementation are XWiki references, such as xwiki:Space.Page
 * for documents or with an object and property reference if the target is an object property. Use the reference module
 * to generate the references passed to this module, so that they can be resolved to XWiki content back by this
 * implementation.
 * 
 * @version $Id$
 */
@Component
public class DefaultIOTargetService implements IOTargetService
{
    /**
     * Document access bridge to manipulate xwiki documents.
     */
    @Requirement
    private DocumentAccessBridge dab;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.internal.DefaultIOService#getSource(String)
     */
    public String getSource(String reference) throws IOServiceException
    {
        try {
            return dab.getDocumentContent(reference);
        } catch (Exception e) {
            throw new IOServiceException("An exception message has occurred while getting the source for " + reference,
                e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.io.IOTargetService#getSourceSyntax(java.lang.String)
     */
    public String getSourceSyntax(String reference) throws IOServiceException
    {
        try {
            return dab.getDocumentSyntaxId(reference);
        } catch (Exception e) {
            throw new IOServiceException("An exception has occurred while getting the syntax of the source for "
                + reference, e);
        }
    }
}
