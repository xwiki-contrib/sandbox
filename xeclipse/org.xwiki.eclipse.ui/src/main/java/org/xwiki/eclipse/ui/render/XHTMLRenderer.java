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
package org.xwiki.eclipse.ui.render;

import java.io.StringReader;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

public class XHTMLRenderer
{
    private EmbeddableComponentManager ecm;

    private Converter converter;

    private WikiPrinter printer;

    public XHTMLRenderer() throws Exception
    {
        ecm = new EmbeddableComponentManager();
        ecm.initialize(this.getClass().getClassLoader());
        converter = ecm.lookup(Converter.class);
        printer = new DefaultWikiPrinter();
    }

    public String XWIKI20toHTML(String test) throws Exception
    {
        converter.convert(new StringReader(test), Syntax.XWIKI_2_0, Syntax.XHTML_1_0, printer);
        return printer.toString();
    }
}
