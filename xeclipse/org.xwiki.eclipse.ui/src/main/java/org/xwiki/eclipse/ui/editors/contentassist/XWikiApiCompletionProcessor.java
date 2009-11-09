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
 *
 */
package org.xwiki.eclipse.ui.editors.contentassist;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.xwiki.eclipse.ui.editors.XWikiApiType;
import org.xwiki.eclipse.ui.editors.utils.XWikiApiTemplateManager;

public class XWikiApiCompletionProcessor extends TemplateCompletionProcessor
{
    XWikiApiType xwikiApiType;

    public XWikiApiCompletionProcessor(XWikiApiType xwikiApiType)
    {
        super();
        this.xwikiApiType = xwikiApiType;
    }

    @Override
    protected TemplateContextType getContextType(ITextViewer arg0, IRegion arg1)
    {
        return new TemplateContextType("org.xwiki.eclipse.ui.editors.velocity.xwikiapi");
    }

    @Override
    protected Image getImage(Template arg0)
    {
        return null;
    }

    @Override
    protected Template[] getTemplates(String arg0)
    {
        Template[] result = XWikiApiTemplateManager.getDefault().getXWikiCompletionTemplates(xwikiApiType);
        if (result != null) {
            return result;
        }

        return new Template[0];
    }
}
