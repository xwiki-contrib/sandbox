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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.crypto.KeyManager;
import org.xwiki.crypto.ScriptSigner;
import org.xwiki.crypto.data.SignedScript;
import org.xwiki.crypto.data.XWikiCertificate;
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

    /** Key manager. */
    @Requirement
    private KeyManager keyManager;

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
        String code = "{{groovy}}\nprintln(\"Hello world\");\n{{/groovy}}\n";
        try {
            SignedScript script = scriptSigner.getVerifiedCode(content);
            System .out.println("  XXXXX " + script);
            code = script.getCode();
            // TODO check that the result contains one script macro? (macros are evaluated during parsing though)
        } catch (GeneralSecurityException exception) {
            // DEBUG
//            throw new MacroExecutionException("Code verification failed", exception);
        }
        List<Block> result = evaluate(code, context);

        // DEBUG
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        try {
            String fp = keyManager.createKeyPair("Test", null);
            XWikiCertificate c = keyManager.getCertificate(fp);
            System .out.println("   NEW NEW NEW\n" + c.toString());

            SignedScript signed = scriptSigner.sign(code, fp);
            System .out.println("   SIGNED\n" + signed);

            SignedScript verified = scriptSigner.getVerifiedCode(signed.serialize());
            System .out.println("   OK\n");
        } catch (GeneralSecurityException exception) {
            throw new MacroExecutionException(exception.getMessage(), exception);
        }
        return result;
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
