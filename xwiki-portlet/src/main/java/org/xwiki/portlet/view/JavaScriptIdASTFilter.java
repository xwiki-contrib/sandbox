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

import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;

/**
 * Name-spaces all occurrences of HTML element identifiers inside the AST tree.
 * 
 * @version $Id$
 */
public class JavaScriptIdASTFilter implements NodeVisitor
{
    /**
     * The list of JavaScript functions that accept only one argument, of type {@code String}, which is an HTML element
     * identifier. Calls to this functions are rewritten so that the passed parameter matches the rewritten HTML element
     * identifiers.
     */
    private static final List<String> ID_FUNCTION_NAMES = Arrays.asList("$", "getElementById", "ID");

    /**
     * The string used to name-space all occurrences of HTML element identifiers inside the JavaScript code.
     */
    private final String namespace;

    /**
     * Creates a new filter that uses the given string to name-space all occurrences of HTML element identifiers inside
     * the AST tree.
     * 
     * @param namespace the name-space
     */
    public JavaScriptIdASTFilter(String namespace)
    {
        this.namespace = namespace;
    }

    @Override
    public boolean visit(AstNode node)
    {
        if (node.getType() == Token.CALL) {
            FunctionCall call = (FunctionCall) node;
            if (call.getArguments().size() == 1 && ID_FUNCTION_NAMES.contains(getFunctionName(call))) {
                AstNode argument = call.getArguments().get(0);
                if (argument.getType() == Token.STRING) {
                    StringLiteral literal = (StringLiteral) argument;
                    literal.setValue(String.format("%s-%s", namespace, literal.getValue()));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param call a function call
     * @return the name of the called function
     */
    private String getFunctionName(FunctionCall call)
    {
        AstNode target = call.getTarget();
        if (target.getType() == Token.NAME) {
            return ((Name) target).getIdentifier();
        } else if (target.getType() == Token.GETPROP) {
            return ((PropertyGet) target).getProperty().getIdentifier();
        }
        return null;
    }
}
