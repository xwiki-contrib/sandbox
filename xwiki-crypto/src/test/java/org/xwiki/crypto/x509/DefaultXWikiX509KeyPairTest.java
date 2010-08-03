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
package org.xwiki.crypto.x509;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.crypto.internal.SerializationUtils;
import org.xwiki.crypto.passwd.PasswordCryptoService;
import org.xwiki.crypto.x509.XWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.DefaultXWikiX509KeyPair;
import org.xwiki.crypto.x509.internal.X509Keymaker;
import org.xwiki.test.AbstractComponentTestCase;


/**
 * Tests the {@link DefaultXWikiX509KeyPair} implementation.
 * 
 * @version $Id$
 * @since 2.5
 */
public class DefaultXWikiX509KeyPairTest extends AbstractComponentTestCase
{
    private final String password = "blah";

    private final String keyPairExportedAsBase64 =
        "-----BEGIN XWIKI CERTIFICATE AND PRIVATE KEY-----\n"
      + "rO0ABXNyADZvcmcueHdpa2kuY3J5cHRvLng1MDkuaW50ZXJuYWwuRGVmYXVsdFhXaWtpWDUwOUtleVBhaXIAAAAAAAAAAQIAA1sAEmVuY29kZ"
      + "WRDZXJ0aWZpY2F0ZXQAAltCWwAbcGFzc3dvcmRFbmNyeXB0ZWRQcml2YXRlS2V5cQB+AAFMABNwcml2YXRlS2V5QWxnb3JpdGhtdAASTGphdm"
      + "EvbGFuZy9TdHJpbmc7eHB1cgACW0Ks8xf4BghU4AIAAHhwAAADPTCCAzkwggIhoAMCAQICBgEqMu9xwTANBgkqhkiG9w0BAQUFADAgMR4wHAY"
      + "KCZImiZPyLGQBAQwOeHdpa2k6WFdpa2kuTWUwHhcNMTAwODAyMTIxMzI3WhcNMTAwODAzMTMxMzI3WjAgMR4wHAYKCZImiZPyLGQBAQwOeHdp"
      + "a2k6WFdpa2kuTWUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCAN9EnMM0SMro/++AuhORauhVo7ZTHy6tL4JlfhB2Lzwqk1ZuU1"
      + "3S2EYPB2pcg3pq/8e6p85xa1TZ3gzVShJAApuPdTEfX7AKTS1l6bxw6BhzLwU+sFZ4sX3jmv4cpIzBCon0qX5p62+7pZP6O4Umlpkjlki/iHd"
      + "9wErtyYv8ybQNNJqxOi7NrutGyFWmaY/72O/gdQ1kBTWW3gMCbhgR486W3RNwn8XWnis2Qp8/pM7WseuBnIYsJ2nsUgFqNGB1ppMUkQB7eubK"
      + "0ASazFpdlo2d2r5ZtjufaBpX6XulKky4YLt9Ehrux6FKIb9S9qhH191IPGG/kUBW+Cf8hjruTAgMBAAGjeTB3MAwGA1UdEwEB/wQCMAAwEQYJ"
      + "YIZIAYb4QgEBBAQDAgWgMA4GA1UdDwEB/wQEAwID+DAfBgNVHSMEGDAWgBQCIap7LoKNn323CB5Oqlarl0sx0DAjBgNVHREBAf8EGTAXhhVod"
      + "HRwOi8vbXkud2ViLmlkLmNvbS8wDQYJKoZIhvcNAQEFBQADggEBABzi9JUxibMQhEjUFDKjrYsoot+dRmt8ibkZbZF+ceCiGwb8SJrvkMJWLg"
      + "MOuFL8SEii0bJgODGlRqK5FUnm4jXiVOJjgQV2adhNxKpV98YW/OQ+IdmQJ71hKgFrgVbZ9nxRZ4d46x7DAASs59GbZNCl6NEmE9GCEkphz96"
      + "VOTh54WqWF0t/ts+FYIBA1/6Uk+n324VkAr4MaViPqjWC4KU23HFYBNWRkualSaDxlGChqgK6B6mt4F5FuU6eD66BmzQD5elVzb91gFvqHQ9U"
      + "dRyA7xKlC8tgumDj7ZP99iViWJiaS+5XSl3kIjx7SuYoTZP9XAJztjAamiNUbHP8WyZ1cQB+AAQAAAiGrO0ABXNyADhvcmcueHdpa2kuY3J5c"
      + "HRvLnBhc3N3ZC5pbnRlcm5hbC5DQVNUNVBhc3N3b3JkQ2lwaGVydGV4dAAAAAAAAAABAgAAeHIAO29yZy54d2lraS5jcnlwdG8ucGFzc3dkLm"
      + "ludGVybmFsLkFic3RyYWN0UGFzc3dvcmRDaXBoZXJ0ZXh0AAAAAAAAAAECAAJbAApjaXBoZXJ0ZXh0dAACW0JMAAtrZXlGdW5jdGlvbnQAL0x"
      + "vcmcveHdpa2kvY3J5cHRvL3Bhc3N3ZC9LZXlEZXJpdmF0aW9uRnVuY3Rpb247eHB1cgACW0Ks8xf4BghU4AIAAHhwAAAGYH05a00myiTOPpLM"
      + "/9STh/EEBDDKhGWTqFwVlddJaSfZDpYluVFwvfYiKZW1qaS6T5Qv067HRRk69FFzzWM7cVpgRx8+32hCM+eLlo0PAsScLZkmepVgeUaptehce"
      + "DDXXZLIWhheTvWLf3NNO4xZsQouatStQN5exZSW5XF0QoIgQEcHrFwNO4WeGdhJkl/ZIPXvCnflQgR3ukQVVsjxgxRshn8cksVYOTwiuwuw6S"
      + "0vu7G40a5Voxoe6PnV0O6kB0U3nRsM25mcs3t2i6dwbOH7Ju9K9fQ8tlHfw+vn+rxtzzffVU3fe/Uthtpi/vHSHLT4gJffXuKqmbIB3yetTBO"
      + "H6SoYdihcDuYYfcsNakeaBl79KoqNENWkKz5sRdZbo83qWg8p3WhpF2KYcQlXJung8k5l1UaolzFKhwtghFmmG9TmQ0+9iWjpu88sCqoBeuBu"
      + "GAWewj12lhRKqYOn9jkzImQJdNTGBsBFNgZJuEEwErQBy9HC6mGw9uMj9i+dCkt9fPR0C7huZBG+dOCkCi/mgqnZKRKwo6ItzAcNYcZZYibcM"
      + "pkabwPdT/GiHVbO7971k98+PdkQ/fMh/eo07uXoJcIihwttwZxjQFKhKUAjAhIL0Lmrzw7OIvOMfh8UU0lWoESOd19RQebZMdPP1hmLi8+nQw"
      + "mWytmg5qJ3AI4/QieUnNsCi8FkzWanjqOoH1zmaCEAF0pYqxsspLCjhP0MpMeHn/aB1Y3kg0/r4KQ5KnBE3lLMZ46v1uwC6EW63AY4roHdGUw"
      + "myEjPX9d1VrZgjPBUVO0+X8tajz19imzkVZSMLgro04pmI7CSYMINV1XCdlCBHVzt5ChI4ibOQbN+DGEilxJCmsW/a4OZIgDt88Ji0wW033lj"
      + "oJL5EvAnV6JC3K+CGjGQ1sTmA21uGYPPtPQCMI6SfYtZr0IC+1D/G8/PtArBk3LRiIUrUN0CkMULBYlEG0zG4vAkGjjOdhCCixPLQa9uedl9+"
      + "4y9g0BYntozC9JSXb2fEl9TvYFZhfVVFgp6zMLHuaacOD+m347yM1ymqjAszh/GuaCXaNuPndH9XxPRBOscLrJp6pHPIwmzwDloVbm1xncvCL"
      + "4AwnNeeCQnv9e99pw73lSd5myrS3E3J3du8oUVTZ5HZdopUjH14S2lylUKT2McOL2ZYa5Ie6Jcjoo8Ub6n2bfpD7qQ0t79YMOekzEgplsjtOl"
      + "fR9nT1cOs12l463C99Wb31u8ekpBkUnnyF2LjDm+I3GD8IFuh5tA5VNuNfpiyoHNuDNhBQaH4oTR0gIlgiJe8MWzNaXwV+a2Qv5P5u4yTWgXL"
      + "YElb/IRShX8u87ovTdRe5vZU5M3y7DVeE4h8ljNpNR0MAGmgTXNDtZPMwfSeNGfIPRiEgXmtJAmmqKsPUhTlBO9PY3mZdf+0+vqak75cscTAF"
      + "kvSBkccwL/yNiUOniZKnrzWWeExAslheG3yV4oTkGXTcpAzQLYgHEB7WJI9TTpr0K6eR71EW8ovh8HExChxMIr4TGTRUPVkpV6ZEgET8uLBkC"
      + "AiNO2bHTPxFc2mZwUVLPFpA2LQSZMNgcxt0FUJQQi0gNx2H5lEo5x2gDLHJNlSWF/7fx8fm9VWsCfcLbnOeD2ge16ljvRXkMKuIkck9o4s7+s"
      + "KLtefw0WzOyxazXs36FUva/rbBCGvO09Uy93yQm/ZU47eY0iFu5vK6UGSmgiW4RofsVvRGxlPZJVVzZ3AbVZlHqIPcmcXJcbOyVbkV1ibP/yR"
      + "hdofAUo6UxbhcG4gJamku/4nzfe9G+H4XbabLFVqrTlflibJKN2BoCgTVpyRDH9qTt4+ppn/RLCUi8CoAaFv7wqUVyJYmLJjve6Da98shAf+w"
      + "MQagTVMqcWoGOlqHMVLXtLAV/r2EdMjwXAM6fjvo8J3z4CWnJ0M3HCgJAL8smCkpc77M7b2YqSqsKYpEUP4423EKTNMCh0k5XdSc4FTepgzCq"
      + "J9LwXzXW7/FZEJ6iTmKa+a7W1qbjaDaZj84gmTS7jbI3iH2EUpiMxZAmCZxJtns50fdIMySv1eQufxWlScJEHHSRqLTGu0jNE2ZMd8qdIy41C"
      + "rZn4+I2bR4SjEjsDVv4+HMMpyWgXhSPE3nzeqdN/IWhgthy3iK2plBYxV/y97uo/1V5Y10D1NKvPVdFZqD69Q8efKyXNyAEZvcmcueHdpa2ku"
      + "Y3J5cHRvLnBhc3N3ZC5pbnRlcm5hbC5TY3J5cHRNZW1vcnlIYXJkS2V5RGVyaXZhdGlvbkZ1bmN0aW9uAAAAAAAAAAECAAVJAAlibG9ja1Npe"
      + "mVJABBkZXJpdmVkS2V5TGVuZ3RoSQANbWVtb3J5RXhwZW5zZUkAEHByb2Nlc3NvckV4cGVuc2VbAARzYWx0cQB+AAJ4cgBIb3JnLnh3aWtpLm"
      + "NyeXB0by5wYXNzd2QuaW50ZXJuYWwuQWJzdHJhY3RNZW1vcnlIYXJkS2V5RGVyaXZhdGlvbkZ1bmN0aW9uAAAAAAAAAAECAAB4cAAAAAgAAAA"
      + "YAAAEAAAAAAN1cQB+AAUAAAAQFFQ8aTdKF0wMfAtNhg2BfnQAA1JTQQ==\n"
      + "-----END XWIKI CERTIFICATE AND PRIVATE KEY-----";

    protected PasswordCryptoService service;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.service = getComponentManager().lookup(PasswordCryptoService.class);
    }

    @Test
    public void testExportImport() throws Exception
    {
        // A trick to save CPU during test, see: X509KeyServiceTest#getKeyPair
        final XWikiX509KeyPair keyPair = X509KeyServiceTest.getKeyPair(this.service);

        final String exported = keyPair.serializeAsBase64();
        final XWikiX509KeyPair imported = DefaultXWikiX509KeyPair.fromBase64String(exported);

        Assert.assertEquals(keyPair.serializeAsBase64(), imported.serializeAsBase64());
        Assert.assertEquals(keyPair.getPrivateKey(this.password), imported.getPrivateKey(this.password));
        Assert.assertEquals(keyPair.getCertificate(), imported.getCertificate());
        Assert.assertEquals(keyPair, imported);
    }

    @Test
    public void importRegressionTest() throws Exception
    {
        XWikiX509KeyPair imported = DefaultXWikiX509KeyPair.fromBase64String(keyPairExportedAsBase64);
        Assert.assertNotNull(imported.getPrivateKey(this.password));
    }
}
