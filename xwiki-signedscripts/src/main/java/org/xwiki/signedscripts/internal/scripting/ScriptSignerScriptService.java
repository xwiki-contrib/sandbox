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
package org.xwiki.signedscripts.internal.scripting;

import java.security.GeneralSecurityException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;
import org.xwiki.signedscripts.ScriptSigner;
import org.xwiki.signedscripts.SignedScript;

/**
 * Script service wrapping a {@link ScriptSigner} component.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component(roles = { ScriptService.class }, hints = { "scriptsigner" })
public class ScriptSignerScriptService implements ScriptService, ScriptSigner
{
    /** Wrapped script signer. */
    @Requirement
    private ScriptSigner signer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.ScriptSigner#sign(java.lang.String, java.lang.String, java.lang.String)
     */
    public SignedScript sign(String code, String fingerprint, String password) throws GeneralSecurityException
    {
        return signer.sign(code, fingerprint, password);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.ScriptSigner#prepareScriptForSigning(java.lang.String, java.lang.String)
     */
    public SignedScript prepareScriptForSigning(String code, String fingerprint) throws GeneralSecurityException
    {
        return signer.prepareScriptForSigning(code, fingerprint);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.ScriptSigner#constructSignedScript(org.xwiki.signedscripts.SignedScript,
     *      java.lang.String)
     */
    public SignedScript constructSignedScript(SignedScript preparedScript, String base64Signature)
        throws GeneralSecurityException
    {
        return signer.constructSignedScript(preparedScript, base64Signature);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.ScriptSigner#getVerifiedScript(java.lang.String)
     */
    public SignedScript getVerifiedScript(String signedScript) throws GeneralSecurityException
    {
        return signer.getVerifiedScript(signedScript);
    }
}
