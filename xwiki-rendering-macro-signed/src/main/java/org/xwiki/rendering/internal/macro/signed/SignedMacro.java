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
package org.xwiki.rendering.internal.macro.signed;

import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.signedscripts.ScriptSigner;
import org.xwiki.signedscripts.SignedScript;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Macro containing a signed script.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("signed")
public class SignedMacro extends AbstractMacro<Object>
{
    /** The description of the macro. */
    private static final String DESCRIPTION = "Executes a signed script with high privileges.";

    /** The description of the macro content. */
    private static final String CONTENT_DESCRIPTION = "the signature and a script macro";

    /** Used to get the parser for the syntax of the current document. */
    @Requirement
    private ComponentManager componentManager;

    /** Create and verify signed scripts. */
    @Requirement
    private ScriptSigner scriptSigner;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public SignedMacro()
    {
        super("Signed", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION));
        setDefaultCategory(DEFAULT_CATEGORY_DEVELOPMENT);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.rendering.macro.Macro#supportsInlineMode()
     */
    public boolean supportsInlineMode()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.rendering.macro.Macro#execute(java.lang.Object, java.lang.String, org.xwiki.rendering.transformation.MacroTransformationContext)
     */
    public List<Block> execute(Object parameters, String content, MacroTransformationContext context)
        throws MacroExecutionException
    {
        try {
            SignedScript script = scriptSigner.getVerifiedScript(content);
            System .out.println("  XXXXX " + script);
            // TODO check that the result contains one script macro? (macros are evaluated during parsing though)
            return evaluate(script.getCode(), context);
        } catch (GeneralSecurityException exception) {
            throw new MacroExecutionException("Code verification failed: " + exception.getMessage(), exception);
        }
    }

    /**
     * Parse and evaluate the signed script using the current document syntax.
     * 
     * @param code the script to evaluate
     * @param context current transformation context
     * @return resulting list of blocks returned by the parser
     * @throws MacroExecutionException on errors
     */
    private List<Block> evaluate(String code, MacroTransformationContext context) throws MacroExecutionException
    {
        try {
            Parser parser = componentManager.lookup(Parser.class, context.getSyntax().toIdString());
            return parser.parse(new StringReader(code)).getChildren();
        } catch (Exception exception) {
            throw new MacroExecutionException("Evaluation of a signed script failed. ", exception);
        }
    }
}
