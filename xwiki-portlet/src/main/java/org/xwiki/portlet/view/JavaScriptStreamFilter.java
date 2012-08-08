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
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptStreamFilter.class);

    /**
     * The objects used to filter the abstract syntax tree computed from the JavaScript source code.
     */
    private final List<NodeVisitor> filters = new ArrayList<NodeVisitor>();

    /**
     * Creates a new JavaScript stream filter that updates all occurrences or element identifiers inside the JavaScript
     * code.
     * 
     * @param namespace the string that was used to name-space all element identifiers
     */
    public JavaScriptStreamFilter(String namespace)
    {
        filters.add(new JavaScriptIdASTFilter(namespace));
        filters.add(new JavaScriptNameASTFilter(namespace));
    }

    @Override
    public void filter(Reader reader, Writer writer)
    {
        try {
            CompilerEnvirons config = new CompilerEnvirons();
            // 'float' is otherwise considered a reserved keyword (usage: element.style.float = 'left').
            config.setReservedKeywordAsIdentifier(true);
            // Force the parser to build the parent scope chain.
            config.setIdeMode(true);
            Parser parser = new Parser(config);
            AstRoot root = parser.parse(reader, null, 0);

            // Filter the AST.
            for (NodeVisitor filter : filters) {
                root.visit(filter);
            }

            // Back to source.
            writer.write(root.toSource());
        } catch (IOException e) {
            LOGGER.error("Failed to rewrite JavaScript code.", e);
        }
    }
}
