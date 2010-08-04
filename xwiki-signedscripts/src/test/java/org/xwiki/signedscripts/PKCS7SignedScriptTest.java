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

    /** Serialized signed script */
    private static final String SIGNED_SCRIPT =
        "Author        : UID=XWiki.Admin\n" +
        "Authority     : UID=XWiki.Admin\n" +
        "Fingerprint   : 942356b9c40a765c73f600036f89b41cdb09a65b\n" +
        "XWikiVersion  : 2.4M2\n" +
        "CreatedOn     : Tuesday, August 3, 2010 11:55:34 PM UTC\n" +
        "Signature     : MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIAwggMuMIIC\n" +
        "                FqADAgECAgYBKjphreUwDQYJKoZIhvcNAQEFBQAwHTEbMBkGCgmSJomT8ixkAQEMC1hXaWtpLkFk\n" +
        "                bWluMB4XDTEwMDgwMzIyNTUzNFoXDTExMDgwMzIzNTUzNFowHTEbMBkGCgmSJomT8ixkAQEMC1hX\n" +
        "                aWtpLkFkbWluMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi4xFJfWGJIkg85D2VpMv\n" +
        "                HxiA83py3k11okU74cBffh/4uG09vfTfj8m0Kda8xF9beuJiNME2p2EwVqftCXcJkL5rn4B+EeFz\n" +
        "                S255wAlOA6TQtJFQ9UoCnQy/7uEuKEUgwmhZCds5TqOyNEkS5lGJU0m39ro0LADYIHV4Pmd5U+wA\n" +
        "                ogtXooG29CrE7cgII1DyTruCtHCR7rxuRTTQAiHvdaEZt9OR/erYEwj6kfsJxRT47jhRf688nbhF\n" +
        "                Y61UnPg2XwWc4gK09ClJBSzNIYVWxOWqoTMcPwz1Vca8kQnlHKKpq89en7kQA4YfZQggAX6mKxJK\n" +
        "                alhSj18dm8uJGt/w/QIDAQABo3QwcjAMBgNVHRMBAf8EAjAAMBEGCWCGSAGG+EIBAQQEAwIFoDAO\n" +
        "                BgNVHQ8BAf8EBAMCA7gwHwYDVR0jBBgwFoAUaVCK4HHxuB8PGxGVEPUUDKDAOWEwHgYDVR0RAQH/\n" +
        "                BBQwEoYQaHR0cDovL215LndlYi5pZDANBgkqhkiG9w0BAQUFAAOCAQEAg+QPjUJCnmz1Tn6+EfnS\n" +
        "                DNFpdPwjvenfKBibndbZoYuqvCgeucUgAW744RD9wZ4rW6UEZ+faclFZhaDV80mWAFoovgV1SvdQ\n" +
        "                z0yTLNc/9GhNtkOltb2nTuwKbyP/c7IpmzmaKWhkw5JFlZ6rNzuWYpDfTl39zmH4+2J7MpsSJ4JX\n" +
        "                2i0jTN8nQWvwRqRKQcI6+8KAq+reCiGnrVIBgWj3LJOLi8a3jB7cLQcoiV0odTNadHOeXHBO3F4R\n" +
        "                OpiaeA6DZylfN/A91VTBY+msx/09AmeB4HBfBVPYxd9URV0rTRzbz4p9v7iv+CBoFvbscTkrlVDT\n" +
        "                uWun8EvtWZytYFaRTwAAMYIBrTCCAakCAQEwJzAdMRswGQYKCZImiZPyLGQBAQwLWFdpa2kuQWRt\n" +
        "                aW4CBgEqOmGt5TAJBgUrDgMCGgUAoF0wGAYJKoZIhvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG\n" +
        "                9w0BCQUxDxcNMTAwODAzMjM1NTM0WjAjBgkqhkiG9w0BCQQxFgQUaYMiQ3VukBQs/4QbC0sWNwjQ\n" +
        "                FTkwDQYJKoZIhvcNAQEBBQAEggEALHTxL5VGj8mXXqAkkbPGPWbA0RHzZxCoYUJx4NFQcZVEGqdK\n" +
        "                j7UCHbA9l2a0ZmG2ZFIeiXr2VYEOChrQwboNXAZsqlxmUva9a0MQ+coBt6motmL+y0CkK4HBDJZs\n" +
        "                4jIAygLIqfKhDmd3pm/ntfLJ5RjNtz2T7oc/zl9O7quJkbqraTLWOFDC83pYRs3YLvRm45l/3B8X\n" +
        "                qHUxq4m4C9Wr6FBUm6o558a+jzHNrVnpyssgBBcvJNOZAVpRB+ny5o9Gj+JdqXdv9f1CpuXL3KqL\n" +
        "                CS4/abuYmOplkcx/+GcskNECleOrGqj+lw/vH5F77ZS5ZY+UVLPnaC4h/aD1xwt4VAAAAAAAAA==\n" +
        "------------------------------------------------------------\n" +
        "{{groovy}}println();{{/groovy}}\n\n";

    /** The CODE from the signed script above. */
    private final String CODE = "{{groovy}}println();{{/groovy}}\n\n";

    @Test
    public void testParse() throws Exception
    {
        SignedScript script = new TestableSignedScript(SIGNED_SCRIPT);
        // check all parsed key-value pairs
        String sigPart = "BBQwEoYQaHR0cDovL215LndlYi5pZDANBgkqhkiG9w0BAQUFAAOCAQEAg+QPjUJCnmz1Tn6+EfnS";
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
        Assert.assertEquals("Tuesday, August 3, 2010 11:55:34 PM UTC", script.get(SignedScriptKey.CREATEDON));
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
        String wrong = SIGNED_SCRIPT.replaceAll("\\{\\{.+\\}\\}\n\n", "");
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

