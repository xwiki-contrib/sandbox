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
package org.xwiki.portlet.view;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import net.sourceforge.htmlunit.corejs.javascript.CompilerEnvirons;
import net.sourceforge.htmlunit.corejs.javascript.Parser;
import net.sourceforge.htmlunit.corejs.javascript.ast.AstRoot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Synchronizes the JavaScript code with the rewritten HTML code. Precisely, updates all occurrences of element
 * identifiers after they have been modified to ensure their uniqueness.
 * 
 * @version $Id$
 */
public class JavaScriptStreamFilter implements StreamFilter
{
    /**
     * The logger instance.
     */
    private static final Log LOG = LogFactory.getLog(JavaScriptStreamFilter.class);

    /**
     * The object used to filter the abstract syntax tree computed from the JavaScript source code.
     */
    private final JavaScriptASTFilter astFilter;

    /**
     * Creates a new JavaScript stream filter that updates all occurrences or element identifiers inside the JavaScript
     * code.
     * 
     * @param namespace the string that was used to name-space all element identifiers
     */
    public JavaScriptStreamFilter(String namespace)
    {
        astFilter = new JavaScriptASTFilter(namespace);
    }

    /**
     * {@inheritDoc}
     * 
     * @see StreamFilter#filter(Reader, Writer)
     */
    public void filter(Reader reader, Writer writer)
    {
        try {
            CompilerEnvirons config = new CompilerEnvirons();
            // 'float' is otherwise considered a reserved keyword (usage: element.style.float = 'left').
            config.setReservedKeywordAsIdentifier(true);
            Parser parser = new Parser(config);
            AstRoot root = parser.parse(reader, null, 0);
            astFilter.filter(root);
            writer.write(root.toSource());
        } catch (IOException e) {
            LOG.error("Failed to rewrite JavaScript code.", e);
        }
    }
}
