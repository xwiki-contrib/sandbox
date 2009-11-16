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

package org.xwiki.annotation.internal.target;

import org.xwiki.annotation.ContentAlterer;
import org.xwiki.annotation.IOService;
import org.xwiki.annotation.IOTargetService;
import org.xwiki.annotation.SelectionService;
import org.xwiki.annotation.SourceAlterer;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

/**
 * Defines components used for XWiki document target.
 * 
 * @version $Id$
 */
@Component(hints = { "documentContent", "default" })
public class DefaultDocumentContentTarget extends AbstractDocumentContentTarget
{
    @Requirement
    private static IOService ioService;

    @Requirement
    private static SelectionService selectionService;

    @Requirement
    private static IOTargetService documentContentTargetService;

    @Requirement("DOCUMENTCONTENT")
    private static SourceAlterer documentSourceAlterer;

    @Requirement("DOCUMENTCONTENT")
    private static ContentAlterer documentContentAlterer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.target.AbstractAnnotationTarget#getContentAlterer()
     */
    @Override
    protected ContentAlterer getContentAlterer()
    {
        return documentContentAlterer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.target.AbstractAnnotationTarget#getSourceAlterer()
     */
    @Override
    protected SourceAlterer getSourceAlterer()
    {
        return documentSourceAlterer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.target.AbstractAnnotationTarget#getIOService()
     */
    @Override
    protected IOService getIOService()
    {
        return ioService;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.target.AbstractAnnotationTarget#getIOTargetService()
     */
    @Override
    protected IOTargetService getIOTargetService()
    {
        return documentContentTargetService;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.internal.target.AbstractAnnotationTarget#getSelectionService()
     */
    @Override
    protected SelectionService getSelectionService()
    {
        return selectionService;
    }
}
