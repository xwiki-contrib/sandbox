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
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Scope;
import org.mozilla.javascript.ast.Symbol;

/**
 * Name-spaces all global variable names.
 * 
 * @version $Id$
 */
public class JavaScriptNameASTFilter implements NodeVisitor
{
    /**
     * The list of predefined names that shouldn't be name-spaced.
     */
    private static final List<String> PREDEFINED_NAMES =
        Arrays.asList("Boolean", "Number", "String", "Array", "Object", "Function", "RegExp", "Date", "Error",
            "EvalError", "RangeError", "ReferenceError", "SyntaxError", "TypeError", "URIError", "decodeURI",
            "decodeURIComponent", "encodeURI", "encodeURIComponent", "eval", "isFinite", "isNaN", "parseFloat",
            "parseInt", "Infinity", "Math", "NaN", "undefined", "window", "document", "navigator", "arguments", "Node",
            "Element", "HTMLElement", "Document", "Window", "Event", "Selection", "Range", "XPathResult",
            "XMLHttpRequest", "ActiveXObject", "self", "console", "setTimeout", "clearTimeout", "location");

    /**
     * The string used to name-space all global variable names.
     */
    private final String namespace;

    /**
     * Creates a new filter that uses the given string to name-space all global variable names.
     * 
     * @param namespace the name-space
     */
    public JavaScriptNameASTFilter(String namespace)
    {
        this.namespace = namespace;
    }

    @Override
    public boolean visit(AstNode node)
    {
        if (node.getType() != Token.NAME) {
            return true;
        }
        Name name = (Name) node;
        if (isProperty(name)) {
            return false;
        }
        Scope scope = name.getDefiningScope();
        if (scope == null) {
            name.setIdentifier(namespace(name.getIdentifier()));
        } else if (scope.getParentScope() == null) {
            String nsIdentifier = namespace(name.getIdentifier());
            Symbol symbol = scope.getSymbolTable().remove(name.getIdentifier());
            symbol.setName(nsIdentifier);
            scope.putSymbol(symbol);
            name.setIdentifier(nsIdentifier);
        }
        return false;
    }

    /**
     * @param name an AST name node
     * @return {@code true} if the given name represents a JavaScript object property
     */
    private boolean isProperty(Name name)
    {
        AstNode parent = name.getParent();
        switch (parent.getType()) {
            case Token.COLON:
                return name == ((ObjectProperty) parent).getLeft();
            case Token.GETPROP:
                return name == ((PropertyGet) parent).getProperty();
            default:
                return false;
        }
    }

    /**
     * Name-spaces the given variable name.
     * 
     * @param name the variable name
     * @return the name-spaced name
     */
    private String namespace(String name)
    {
        // NOTE: We don't name-space predefined names even if they are declared because there are predefined names that
        // are not available on all browsers and so they need to be declared in order to have a cross-browser behavior.
        if (PREDEFINED_NAMES.contains(name)) {
            return name;
        } else {
            return namespace + name;
        }
    }
}
