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
package org.xwiki.annotation.internal.renderer;

import org.xwiki.annotation.renderer.AbstractAnnotationRenderer;
import org.xwiki.annotation.renderer.AnnotationChainingPrintRenderer;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.renderer.xhtml.XHTMLImageRenderer;
import org.xwiki.rendering.renderer.xhtml.XHTMLLinkRenderer;

/**
 * Renders annotations in the XHTML format.
 * 
 * @version $Id$
 */
@Component("annotations/xhtml/1.0")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class XHTMLAnnotationRenderer extends AbstractAnnotationRenderer
{
    /**
     * To render link events into XHTML. This is done so that it's pluggable because link rendering depends on how the
     * underlying system wants to handle it. For example for XWiki we check if the document exists, we get the document
     * URL, etc.
     */
    @Requirement
    private XHTMLLinkRenderer linkRenderer;

    /**
     * To render image events into XHTML. This is done so that it's pluggable because image rendering depends on how the
     * underlying system wants to handle it. For example for XWiki we check if the image exists as a document
     * attachments, we get its URL, etc.
     */
    @Requirement
    private XHTMLImageRenderer imageRenderer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.renderer.AbstractAnnotationRenderer#getAnnotationPrintRenderer(ListenerChain)
     */
    @Override
    public AnnotationChainingPrintRenderer getAnnotationPrintRenderer(ListenerChain chain)
    {
        return new XHTMLAnnotationChainingPrintRenderer(linkRenderer, imageRenderer, chain);
    }

}
