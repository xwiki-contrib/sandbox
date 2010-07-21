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
package org.xwiki.crypto.passwd.internal.scrypt;

import org.junit.Test;
import org.junit.Assert;

import org.bouncycastle.util.encoders.Hex;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.xwiki.crypto.passwd.internal.PasswordBasedKeyDerivationFunction2;

/**
 * Tests Scrypt agains test outputs given in reference document.
 *
 * @since 2.5
 * @version $Id:$
 */
public class ScryptTest
{
    /*   scrypt("", "", 16, 1, 1, 64) =
     * 77 d6 57 62 38 65 7b 20 3b 19 ca 42 c1 8a 04 97
     * f1 6b 48 44 e3 07 4a e8 df df fa 3f ed e2 14 42
     * fc d0 06 9d ed 09 48 f8 32 6a 75 3a 0f c8 1f 17
     * e8 d3 e0 fb 2e 0d 36 28 cf 35 e2 0c 38 d1 89 06
     */
    private final String outputSample1 = "77d6576238657b203b19ca42c18a0497f16b4844e3074ae8dfdffa3fede21442fcd0069ded09"
                                       + "48f8326a753a0fc81f17e8d3e0fb2e0d3628cf35e20c38d18906";

    /*   scrypt("password", "NaCl", 1024, 8, 16, 64) =
     * fd ba be 1c 9d 34 72 00 78 56 e7 19 0d 01 e9 fe
     * 7c 6a d7 cb c8 23 78 30 e7 73 76 63 4b 37 31 62
     * 2e af 30 d9 2e 22 a3 88 6f f1 09 27 9d 98 30 da
     * c7 27 af b9 4a 83 ee 6d 83 60 cb df a2 cc 06 40
     */
    private final String outputSample2 = "fdbabe1c9d3472007856e7190d01e9fe7c6ad7cbc8237830e77376634b3731622eaf30d92e22"
                                       + "a3886ff109279d9830dac727afb94a83ee6d8360cbdfa2cc0640";

    /*   scrypt("pleaseletmein", "SodiumChloride", 16384, 8, 1, 64) =
     * 70 23 bd cb 3a fd 73 48 46 1c 06 cd 81 fd 38 eb
     * fd a8 fb ba 90 4f 8e 3e a9 b5 43 f6 54 5d a1 f2
     * d5 43 29 55 61 3f 0f cf 62 d4 97 05 24 2a 9a f9
     * e6 1e 85 dc 0d 65 1e 40 df cf 01 7b 45 57 58 87
     */
    private final String outputSample3 = "7023bdcb3afd7348461c06cd81fd38ebfda8fbba904f8e3ea9b543f6545da1f2d5432955613f"
                                       + "0fcf62d49705242a9af9e61e85dc0d651e40dfcf017b45575887";

    private final String scryptPBKDF2InputSaltHex = "24f07a94e244270e757f148e153b768730831aac33679d374f51cfb7d95a69ab";

    private final String scryptPBKDF2InputPassword = "password";

    private final int scryptPBKDF2DerivedKeyLength = 2048;

    /** Storing a sha256 hash of the output since the actual hex dump of the output is huge (4096 characters) */
    private final String scryptPBKDF2OutputSha256 = "8cabdcfa867e22c05456e850e2be111c9346e25dbf8d609488da68be6d883bc8";

    private final String integerifyAndModInputX =
        "9144c863 8b362a4e 56b9bce8 0b0064ac 23561ca6 2a4aeee5 686cd8ed f246f08b 1c3a807e 5bc725ac 2f453328 3445c8c6"
      + "3b2b4ff5 6653eac4 d68c0348 702d060f 466ab780 a34e857f 7252d473 4aa3bbf2 ed9d5502 79b18ed1 f4fcd982 fe4d7569"
      + "dac0d7ea 1bed4870 ac2857fa 9d1bd718 9c896da9 1eed16df 44c5e169 e2e4d0ff 727f5266 c2a879b7 901763a1 0b689fac"
      + "40b42500 a7b7f594 9a1c9bc4 7196bf64 c59d6fc7 ffe28bc1 5233fd78 a2b943ae 70e9ffcc f7ff9d89 fa3dd109 f26ae622"
      + "2621c460 f9b27069 d740cb74 7b85276d 2e2152a4 99a44987 01350180 0909b89f a7a26fb8 8b640c9a b7dc60f5 acbf8806"
      + "1f1d2f7c 13aa6364 b40b2dd9 1ccb8eb6 ac58b406 d1cdb156 df6a2c81 74c4c8fb 8c89399d 6e0709c1 8fa6d232 61d7202e"
      + "9f888c04 e05e04ca 54da362a 95c94ef6 234df60b 362b23c6 3011951e 85d11fc7 ffb263b3 18ef5811 a1dfc42b 313bab1e"
      + "3012a20c 42e6d2de a5da7d75 17a5ff68 5f6d57b0 cbc6d8ba 1afa4942 89e0498b de33f3b2 6e7e229d c50129bf b5dc83bc"
      + "255fd532 f922fdaa 2c4e6666 68bebedf f2067172 f808c551 495d49d4 43f8f050 a55a1927 38c7aff2 b39b1ee4 f41b9fb3"
      + "18e8b954 fe88d473 92dcab2d 67835f83 992f8ca7 f9803c72 2c55a54a ec3eb303 511864f5 86f551cd 9597c3ba 8b82a0e1"
      + "60baf599 49f2fc4b 609087c2 9f0fa1eb cdf5a730 b77dcfce 17f8b51b e0931f52 63789a9f d201b077 05d7a9eb b6733557"
      + "8908aa0a 8e6e56e9 ae6b6527 86454436 c7f253af 7ba5b8d1 c0cfa302 2dbabae5 59c39afc 9f767720 e95fb185 47a4af03"
      + "4e27036c 4cc1591c 2ff414f6 d4e1cfee cd4dc36b d13b9248 9daf07fc bb5f2249 84c5edcd d87f4b0a f5100bf1 6b062730"
      + "f37d7377 386a308e 0ebec79f 1b35ed0d d5849f4b 23308d19 399000f1 0e166c97 6f21a813 0ab24b61 21fb743f 8d40ff76"
      + "6bc82068 c1b61b72 aec2aac4 1e47c1b7 e5283daa 540ecb25 557abf1c 1307f020 a4626d32 02e032b1 f8f55625 c05a4994"
      + "4f724d74 206b9546 06c18420 93511ba5 959efa6b f9a4b2a8 55420db6 c67d61d5 38a5c910 8449f136 fc438fcd 33fcc1b9"
      + "efa31d7c d1abb5a3 1aeac456 0a2ba7e1 72a53999 4c1421a7 49135e31 171162cd c81c5c58 c765d48e 002ec891 529142ef"
      + "f209608e 05e8cb78 a909174c b12faf1c 7ba28bb4 81228db3 0bf0af8c 5df78f1c 3ad99692 9716eeb9 d03219dd 51e37cfe"
      + "64deeff9 289334f8 967a80d7 308af706 0485d323 9a5de574 55110c36 fdae2c24 51293f62 7ac41e2d c5e01118 098e3ec4"
      + "341a2549 244f8942 b677fb7b 9205f7fc c36578ee d1b31391 4f3ea132 db8fbf72 71986e45 f2ea74f7 57718776 baba10f1"
      + "b44d9e8e 7bea00c1 215a3ddc 232e25ad 58f6a5f9 8d80c25b 8943a0fd 9d260557 35764fd3 0033f9ab 20f92eb6 7ec6142a"
      + "769fa8db 9b49a22f 7c0116d7 6b6e92a2";

    private final int integerifyAndModInputN = 131072;

    private final int integerifyAndModOutput = 106126;

    private final String integerifyAndMod2InputX =
        "45844efe f4e5630f e29e98f2 585f3e7b 5526b8f8 7939d40e 6bf65da8 818b517c c360d882 f589b043 16600945 b1747358"
      + "32689275 c1c2b0fc ddc67d82 a90614bb 0510a964 e2b6e8d5 ffe79f9b 32f08ae0 de44bb76 612f6600 d8a88bf6 d0929410"
      + "7f91d0d5 cee5cfdf 9dd402ca a8ba274a 876dc9e4 c6efb102 2ffe93fe fd0c66dd";

    private final int integerifyAndMod2InputN = 16;

    private final int integerifyAndMod2Output = 4;
/*
X = 
45844efef4e5630fe29e98f2585f3e7b5526b8f87939d40e6bf65da8818b517cc360d882f589b04316600945b174735832689275c1c2b0fcddc67d82a90614bb510a964e2b6e8d5ffe79f9b32f08ae0de44bb76612f6600d8a88bf6d09294107f91d0d5cee5cfdf9dd402caa8ba274a876dc9e4c6efb1022ffe93fefd0c66dd
N = 16
j = 4
*/

    @Test
    public void scryptConformanceTest1() throws Exception
    {
        Scrypt engine = new Scrypt(new byte[0], 16, 1, 1, 64);
        byte[] out = engine.hashPassword(new byte[0]);
        String outStr = new String(Hex.encode(out), "US-ASCII");
        Assert.assertTrue("Mismatch:\nExpecting: " + outputSample1 + "\n      Got: " + outStr,
                          outputSample1.equals(outStr));
    }

    @Test
    public void scryptConformanceTest2() throws Exception
    {
        Scrypt engine = new Scrypt(new byte[] {'N', 'a', 'C', 'l'}, 1024, 8, 16, 64);
        byte[] out = engine.hashPassword(new byte[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'});
        String outStr = new String(Hex.encode(out), "US-ASCII");
        Assert.assertTrue("Mismatch:\nExpecting: " + outputSample2 + "\n      Got: " + outStr,
                          outputSample2.equals(outStr));
    }

    @Test
    public void scryptConformanceTest3() throws Exception
    {
        Scrypt engine = new Scrypt("SociumChloride".getBytes("US-ASCII"), //new byte[] {'S', 'o', 'd', 'i', 'u', 'm', 'C', 'h', 'l', 'o', 'r', 'i', 'd', 'e'},
                                   16384, 8, 1, 64);
        byte[] out = engine.hashPassword("pleaseletmein".getBytes("US-ASCII"));//    new byte[] {'p', 'l', 'e', 'a', 's', 'e', 'l', 'e', 't', 'm', 'e', 'i', 'n'});
        String outStr = new String(Hex.encode(out), "US-ASCII");
        Assert.assertTrue("Mismatch:\nExpecting: " + outputSample3 + "\n      Got: " + outStr,
                          outputSample3.equals(outStr));
    }

    @Test
    public void scryptPBKDF2Test() throws Exception
    {
        PasswordBasedKeyDerivationFunction2 sha256Pbkdf2 = new PasswordBasedKeyDerivationFunction2(new SHA256Digest());

        byte[] out = sha256Pbkdf2.generateDerivedKey(scryptPBKDF2InputPassword.getBytes("US-ASCII"),
                                                     Hex.decode(scryptPBKDF2InputSaltHex.getBytes("US-ASCII")),
                                                     1,
                                                     scryptPBKDF2DerivedKeyLength);
        Digest d = new SHA256Digest();
        d.update(out, 0, out.length);
        byte[] hash = new byte[32];
        d.doFinal(hash, 0);
        String outStr = new String(Hex.encode(hash), "US-ASCII");
        Assert.assertTrue("Mismatch:\nExpecting: " + scryptPBKDF2OutputSha256 + "\n      Got: " + outStr,
                          scryptPBKDF2OutputSha256.equals(outStr));
    }

    @Test
    public void scryptIntegerifyAndModTest() throws Exception
    {
        Scrypt engine = new Scrypt(new byte[0], 2, 1, 1, 1);
        int out = engine.integerifyAndMod(Hex.decode(integerifyAndModInputX.getBytes("US-ASCII")),
                                          integerifyAndModInputN);

        Assert.assertTrue("Mismatch:\nExpecting: " + integerifyAndModOutput + "\n      Got: " + out,
                          (integerifyAndModOutput == out));
    }

    @Test
    public void scryptIntegerifyAndModTest2() throws Exception
    {
        Scrypt engine = new Scrypt(new byte[0], 2, 1, 1, 1);
        int out = engine.integerifyAndMod(Hex.decode(integerifyAndMod2InputX.getBytes("US-ASCII")),
                                          integerifyAndMod2InputN);

        Assert.assertTrue("Mismatch:\nExpecting: " + integerifyAndMod2Output + "\n      Got: " + out,
                          (integerifyAndMod2Output == out));
    }
}
