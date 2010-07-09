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
package org.xwiki.crypto.internal.scripting;

import java.security.GeneralSecurityException;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.crypto.ScriptSigner;
import org.xwiki.crypto.data.SignedScript;
import org.xwiki.script.service.ScriptService;


/**
 * Script service wrapping a {@link ScriptSigner} component.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("scriptsigner")
public class ScriptSignerScriptService implements ScriptService
{
    /** Enclosed script signer. */
    @Requirement
    private ScriptSigner signer;

    /**
     * @param code code to sign
     * @param fingerprint certificate fingerprint identifying the private key to use
     * @return signed script object
     * @throws GeneralSecurityException on errors
     * @see org.xwiki.crypto.ScriptSigner#sign(java.lang.String, java.lang.String)
     */
    public SignedScript sign(String code, String fingerprint) throws GeneralSecurityException
    {
        return signer.sign(code, fingerprint);
    }

    /**
     * @param signedScript serialized signed script object 
     * @return code contained in the signed script
     * @throws GeneralSecurityException if verification fails or on errors
     * @see org.xwiki.crypto.ScriptSigner#getVerifiedCode(java.lang.String)
     */
    public SignedScript getVerifiedCode(String signedScript) throws GeneralSecurityException
    {
        return signer.getVerifiedCode(signedScript);
    }
}

