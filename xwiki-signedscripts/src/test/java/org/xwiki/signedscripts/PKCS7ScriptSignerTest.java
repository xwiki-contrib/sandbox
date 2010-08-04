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
package org.xwiki.signedscripts;

import java.security.GeneralSecurityException;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.crypto.x509.X509CryptoService;
import org.xwiki.signedscripts.framework.AbstractSignedScriptsTest;
import org.xwiki.signedscripts.internal.PKCS7ScriptSigner;


/**
 * Tests {@link PKCS7ScriptSigner}.
 * 
 * @version $Id$
 * @since 2.5
 */
public class PKCS7ScriptSignerTest extends AbstractSignedScriptsTest
{
    /** The tested script signer component. */
    ScriptSigner signer;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.framework.AbstractSignedScriptsTest#setUp()
     */
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.signer = getComponentManager().lookup(ScriptSigner.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.signedscripts.framework.AbstractSignedScriptsTest#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();
        // key manager
        final String kpFingerprint = getTestKeyPair().getFingerprint();
        final KeyManager mockKeyManager = registerMockComponent(KeyManager.class);
        getMockery().checking(new Expectations() {{
            allowing(mockKeyManager).getCertificate(with(kpFingerprint));
                will(returnValue(getTestKeyPair().getCertificate()));
            allowing(mockKeyManager).getKeyPair();
                will(returnValue(getTestKeyPair()));
            allowing(mockKeyManager).getTrustedFingerprint(USER);
                will(returnValue(kpFingerprint));
        }});
    }

    @Test
    public void testSign() throws GeneralSecurityException
    {
        SignedScript script = signer.sign(CODE, "passwrd");
        Assert.assertEquals(CODE, script.getCode());
        Assert.assertEquals(getTestKeyPair().getFingerprint(), script.get(SignedScriptKey.FINGERPRINT));
    }

    @Test
    public void testSignVerify() throws GeneralSecurityException
    {
        SignedScript script = signer.sign(CODE, "passwrd");
        // NOTE: the test may fail randomly if the next second starts at this line 
        SignedScript verified = signer.getVerifiedScript(script.serialize());
        Assert.assertEquals(script.toString(), verified.toString());
    }

    @Test
    public void testExternalSignIsSign() throws Exception
    {
        final SignedScript signed = signer.sign(CODE, "passwrd");
        // NOTE: the test may fail randomly if the next second starts at this line 
        SignedScript prepared = signer.prepareScriptForSigning(CODE);
        // browser imitation
        X509CryptoService pkcs7 = getComponentManager().lookup(X509CryptoService.class);
        String signature = pkcs7.signText(prepared.getDataToSign(), getTestKeyPair(), "passwrd");
        SignedScript script = signer.constructSignedScript(prepared, signature);
        Assert.assertEquals(signed.toString(), script.toString());
    }

    @Test(expected = GeneralSecurityException.class)
    public void testPreparedNotVerify() throws GeneralSecurityException
    {
        SignedScript prepared = signer.prepareScriptForSigning(CODE);
        // the test relies on the fact that an unsigned script has the empty string instead of the signature
        signer.getVerifiedScript(prepared.serialize());
        Assert.fail("Prepared script passed verification");
    }

    @Test
    public void testRegression() throws Exception
    {
        Assert.assertNotNull(this.signer.getVerifiedScript(SIGNED_SCRIPT));
    }
}

