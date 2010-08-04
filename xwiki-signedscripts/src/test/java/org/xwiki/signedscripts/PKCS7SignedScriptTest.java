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

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.crypto.x509.XWikiX509Certificate;
import org.xwiki.signedscripts.framework.AbstractSignedScriptsTest;
import org.xwiki.signedscripts.internal.PKCS7SignedScript;



/**
 * Tests {@link PKCS7SignedScript}.
 * 
 * @version $Id$
 * @since 2.5
 */
public class PKCS7SignedScriptTest extends AbstractSignedScriptsTest
{
    /**
     * Wrapper to test {@link PKCS7SignedScript}.
     */
    private class TestableSignedScript extends PKCS7SignedScript
    {
        /**
         * Create new {@link TestableSignedScript}.
         * 
         * @param preparedScript
         * @param base64Signature
         * @throws IOException
         */
        public TestableSignedScript(SignedScript preparedScript, String base64Signature) throws IOException
        {
            super(preparedScript, base64Signature);
        }

        /**
         * Create new {@link TestableSignedScript}.
         * 
         * @param CODE
         * @param fingerprint
         * @throws GeneralSecurityException
         */
        public TestableSignedScript(String code, String fingerprint) throws GeneralSecurityException
        {
            super(code, fingerprint);
        }

        /**
         * Create new {@link TestableSignedScript}.
         * 
         * @param script
         * @throws IOException
         */
        public TestableSignedScript(String script) throws IOException
        {
            super(script);
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.xwiki.signedscripts.internal.PKCS7SignedScript#set(org.xwiki.signedscripts.SignedScriptKey, java.lang.String)
         */
        @Override
        public void set(SignedScriptKey key, String value)
        {
            super.set(key, value);
        }
    }

    @Test
    public void testParse() throws Exception
    {
        SignedScript script = new TestableSignedScript(SIGNED_SCRIPT);
        // check all parsed key-value pairs
        String sigPart = "2i0jTN8nQWvwRqRKQcI6+8KAq+reCiGnrVIBgWj3LJOLi8a3jB7cLQcoiV0odTNadHOeXHBO3F4R";
        XWikiX509Certificate cert = getTestKeyPair().getCertificate();
        Assert.assertNull(script.get(SignedScriptKey.ALLOWACTIONS));
        Assert.assertNull(script.get(SignedScriptKey.ALLOWRIGHTS));
        Assert.assertNull(script.get(SignedScriptKey.DOCUMENT));
        Assert.assertNull(script.get(SignedScriptKey.EXPIRESON));
        Assert.assertNull("Invalid key is not null", script.get(SignedScriptKey.INVALID));
        Assert.assertEquals(cert.getAuthorName(), script.get(SignedScriptKey.AUTHOR));
        Assert.assertEquals(cert.getIssuerName(), script.get(SignedScriptKey.AUTHORITY));
        Assert.assertEquals(CODE, script.get(SignedScriptKey.CODE));
        Assert.assertEquals(script.get(SignedScriptKey.CODE), script.getCode());
        Assert.assertEquals("Wednesday, August 4, 2010 12:18:59 PM UTC", script.get(SignedScriptKey.CREATEDON));
        Assert.assertEquals(cert.getFingerprint(), script.get(SignedScriptKey.FINGERPRINT));
        Assert.assertTrue("Signature seems to be wrong", script.get(SignedScriptKey.SIGNATURE).contains(sigPart));
        Assert.assertEquals("2.4M2", script.get(SignedScriptKey.XWIKIVERSION));
    }

    @Test
    public void testParseSerialize() throws IOException
    {
        Assert.assertEquals(SIGNED_SCRIPT, new TestableSignedScript(SIGNED_SCRIPT).toString());
    }

    @Test(expected = IOException.class)
    public void testMissingCodeOnParsing() throws IOException
    {
        String wrong = SIGNED_SCRIPT.replaceAll("\\{\\{.+\\}\\}\n", "");
        new TestableSignedScript(wrong);
    }

    @Test(expected = IOException.class)
    public void testMissingCode() throws IOException
    {
        TestableSignedScript script = null;
        try {
            script = new TestableSignedScript(SIGNED_SCRIPT);
            script.set(SignedScriptKey.CODE, "");
        } catch (IOException exception) {
            // we should not fail yet
            Assert.fail("Parsing failed");
        }
        new TestableSignedScript(script, "test");
    }


    @Test(expected = IOException.class)
    public void testMissingAuthorOnParsing() throws IOException
    {
        String wrong = SIGNED_SCRIPT.replaceAll("Author[^\n]+\n", "");
        new TestableSignedScript(wrong);
    }

    @Test(expected = IOException.class)
    public void testMissingAuthor() throws IOException
    {
        TestableSignedScript script = null;
        try {
            script = new TestableSignedScript(SIGNED_SCRIPT);
            script.set(SignedScriptKey.AUTHOR, "");
        } catch (IOException exception) {
            // we should not fail yet
            Assert.fail("Parsing failed");
        }
        new TestableSignedScript(script, "test");
    }

    @Test
    public void testIsSet() throws IOException
    {
        TestableSignedScript script = new TestableSignedScript(SIGNED_SCRIPT);
        Assert.assertTrue("Fingerprint should be set", script.isSet(SignedScriptKey.FINGERPRINT));
        Assert.assertFalse("Document should not be set", script.isSet(SignedScriptKey.DOCUMENT));
        script.set(SignedScriptKey.DOCUMENT, SignedScriptKey.DOCUMENT.getDefault());
        Assert.assertFalse("Document should still not be set", script.isSet(SignedScriptKey.DOCUMENT));
        script.set(SignedScriptKey.DOCUMENT, "XWiki.Test");
        Assert.assertTrue("Document should be set now", script.isSet(SignedScriptKey.DOCUMENT));
    }

    @Test
    public void testSignatureReplaces() throws IOException
    {
        SignedScript prepared = new TestableSignedScript(SIGNED_SCRIPT);
        SignedScript script = new TestableSignedScript(prepared, "test");
        Assert.assertEquals("Signature was not replaced", "test", script.get(SignedScriptKey.SIGNATURE));
    }

    @Test(expected = IOException.class)
    public void testSignatureIsEmpty() throws IOException
    {
        SignedScript prepared = new TestableSignedScript(SIGNED_SCRIPT);
        new TestableSignedScript(prepared, "");
    }

    @Test(expected = GeneralSecurityException.class)
    public void testFingerprintIsEmpty() throws GeneralSecurityException
    {
        new TestableSignedScript(CODE, "");
    }

    @Test(expected = GeneralSecurityException.class)
    public void testCodeIsEmpty() throws GeneralSecurityException
    {
        new TestableSignedScript("", "tralala");
    }

    @Test
    public void testNLNormalization() throws GeneralSecurityException
    {
        SignedScript script = new TestableSignedScript("\rnew line\rtest\r\n\r\r\r\n\rtest\r\n\r\n", "tralala");
        Assert.assertFalse("The code was not normalized", script.getCode().contains("\r"));
    }

    @Test
    public void testNoNLAtTheEnd() throws GeneralSecurityException
    {
        SignedScript script = new TestableSignedScript("test", "tralala");
        Assert.assertEquals("test\n", script.getCode());
    }

    @Test
    public void testManyNLAtTheEnd() throws GeneralSecurityException
    {
        SignedScript script = new TestableSignedScript("test\r\n\r\r\n\n\n", "tralala");
        Assert.assertEquals("test\n", script.getCode());
    }
}

