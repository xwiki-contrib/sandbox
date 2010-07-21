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
package org.xwiki.crypto.passwd.scrypt;

import org.junit.Test;
import org.junit.Assert;

import org.bouncycastle.util.encoders.Hex;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.xwiki.crypto.passwd.internal.PasswordBasedKeyDerivationFunction2;
import org.xwiki.crypto.passwd.internal.scrypt.Scrypt;

/**
 * Tests Scrypt agains test outputs given in reference document.
 *
 * @since 2.5
 * @version $Id$
 */
public class ScryptTest extends Scrypt
{
    /*   scrypt("", "", 16, 1, 1, 64) =
     * 77 d6 57 62 38 65 7b 20 3b 19 ca 42 c1 8a 04 97
     * f1 6b 48 44 e3 07 4a e8 df df fa 3f ed e2 14 42
     * fc d0 06 9d ed 09 48 f8 32 6a 75 3a 0f c8 1f 17
     * e8 d3 e0 fb 2e 0d 36 28 cf 35 e2 0c 38 d1 89 06
     */
    private final String outputSample1 = "77d6576238657b203b19ca42c18a0497f16b4844e3074ae8dfdffa3fede21442fcd0069ded0"
                                       + "948f8326a753a0fc81f17e8d3e0fb2e0d3628cf35e20c38d18906";

    /*   scrypt("password", "NaCl", 1024, 8, 16, 64) =
     * fd ba be 1c 9d 34 72 00 78 56 e7 19 0d 01 e9 fe
     * 7c 6a d7 cb c8 23 78 30 e7 73 76 63 4b 37 31 62
     * 2e af 30 d9 2e 22 a3 88 6f f1 09 27 9d 98 30 da
     * c7 27 af b9 4a 83 ee 6d 83 60 cb df a2 cc 06 40
     */
    private final String outputSample2 = "fdbabe1c9d3472007856e7190d01e9fe7c6ad7cbc8237830e77376634b3731622eaf30d92e2"
                                       + "2a3886ff109279d9830dac727afb94a83ee6d8360cbdfa2cc0640";

    /*   scrypt("pleaseletmein", "SodiumChloride", 16384, 8, 1, 64) =
     * 70 23 bd cb 3a fd 73 48 46 1c 06 cd 81 fd 38 eb
     * fd a8 fb ba 90 4f 8e 3e a9 b5 43 f6 54 5d a1 f2
     * d5 43 29 55 61 3f 0f cf 62 d4 97 05 24 2a 9a f9
     * e6 1e 85 dc 0d 65 1e 40 df cf 01 7b 45 57 58 87
     */
    private final String outputSample3 = "7023bdcb3afd7348461c06cd81fd38ebfda8fbba904f8e3ea9b543f6545da1f2d5432955613"
                                       + "f0fcf62d49705242a9af9e61e85dc0d651e40dfcf017b45575887";

    private final String scryptPBKDF2InputSaltHex = "24f07a94e244270e757f148e153b768730831aac33679d374f51cfb7d95a69ab";

    private final String scryptPBKDF2InputPassword = "password";

    private final int scryptPBKDF2DerivedKeyLength = 2048;

    /** Storing a sha256 hash of the output since the actual hex dump of the output is huge (4096 characters) */
    private final String scryptPBKDF2OutputSha256 = "8cabdcfa867e22c05456e850e2be111c9346e25dbf8d609488da68be6d883bc8";

    private final String integerifyAndModInputX =
        "dc1cdd34603f1ee9cf815af82570e47afe87fed1e7400e2dfb610d4e4400b5df0162ce5bdbcfe85d69680610a8dd32fba06c906851a8"
      + "41ef74585af9fbed2bfe40b63735df59e261a8e43185b1a079f128e16f516efff4d5f8f392ee17416eedde1702bd4fa8d55543681f42"
      + "8a71eff3fdfa148b545e4df6db27bb565beb735c85bbfbda3c6bc97b9212f7fc223b2f1df819ef8e5c66cd8bcdee3a7d1984a722aeb7"
      + "f751145ec61788074571535c8e6bb37b1b7c93f89fd4e4cbfbf96a515e44d02dcf0a8ef2fd9fd94fbfa20766372349bb204f50fa7752"
      + "c77121a9f0a1a9fefdc1a6975038b4cac5486d9b70b2dab2ba127b3dc0d8a0d4a3ce2e3cd8f686f5add4476286f94e95c0d74b08a421"
      + "23159ef523068a0d09a3d9ce027a34d700073326526251b6e14bf2c45cae8b80dfc1b6774d9692349c4f02621e4c606c5d11600c5f6a"
      + "1a12479cc55cadcd47a17018d4377a3e1707fee61c84583b32b94605d8d15a8475b362b3ea47a4d3922c57a6d74a51dbf86272854a97"
      + "e57a66cf7891ba374aa2cced708a938f8dcf2d5822ed37687d7a1e9eef66e060b7cb25490e648b93ef98e1eb32543de72213e6c6e6fe"
      + "b9702acfabd92d1b213f54f9084e0590eadca3ccc7d9d1c5c2c097c545aaf36c91b5088a9bd27e8d2ae6fd3d110630f63337d814aa89"
      + "e9df909a3097919bcd76a12f025efe061317cc04484ee91cacbc4daa203387e89cf13e56ee8c6632d547d80c95fdf7c9199f179f915a"
      + "e3105772866a9070af998eb93762b4f68a4262bc90dc195a5b77b67eba4ffe3ec5f42fa614d37283cd38c3b3c14411c2f0e4896b329d"
      + "22113322764e7ad90a14d6c54201df924a6e1b9fea3df0caac6f3a6f997f79a5de2e8d74bee5d77e3fc5281aebd4d39d6930adfbb7ce"
      + "784d560c02a12197c316f9ca3b30b11e3bec7d71c47bf71b5ed1b7a47c85a5f082b653b1497a738fb7ee6b98b4c7534173da8ce140d9"
      + "fc506113400b54d7c2f226d3a221636ac38725fb841a4bc6253e62a09c196eec518817837890555e46b29d35cd52cf596fae5c00656c"
      + "12603d349ad725b34f707bda82033a03ce75641a13c3bc2a18559784a5c1d5559988b35505651fef4c6be12c17928bff4268a26045b9"
      + "65d07bc0965bacd422e631d83402c29ac02ce6bb5f0c6a1d7bdd7b576b39b3d673da9d46d85a11070865e9a7cb9783d4e0e6597dcc02"
      + "1dbc152845b6ebc2fe258625f2711aa4a97ab3c731bcfe422e0d53b0df0e626ec4006b230c27e9befcb2cc5de9a7d5edc863954fadc0"
      + "a3ab9a26b86710c5ad249ba8ea49edf88e2463ac8e508faea77547932b346fd50eaf6046acb9001c3f5b1cda222140d8bf29f3332b6c"
      + "0844dafbe12e830baac4f9d07d154103ac25b88c0bd87334eac87ca85d51a0288c2f6c1dd0faa313194e43e4d981058e0b4a5957";

    private final int integerifyAndModInputN = 131072;

    private final int integerifyAndModOutput = 55836;

    private final String integerifyAndMod2InputX =
        "fe4e84450f63e5f4f2989ee27b3e5f58f8b826550ed43979a85df66b7c518b8182d860c343b089f545096016587374b175926832fcb0"
      + "c2c1827dc6ddbb1406a964a91005d5e8b6e29b9fe7ffe08af03276bb44de00662f61f68ba8d8109492d0d5d0917fdfcfe5ceca02d49d"
      + "4a27baa8e4c96d8702b1efc6fe93fe2fdd660cfd";

    private final int integerifyAndMod2InputN = 16;

    private final int integerifyAndMod2Output = 4;

    /** Default Constructor needed because this class extends Scrypt to test protected methods. */
    public ScryptTest()
    {
        super(new byte[0], 2, 1, 1, 1);
    }

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
        int out = this.integerifyAndMod(Hex.decode(integerifyAndModInputX.getBytes("US-ASCII")),
                                        integerifyAndModInputN);

        Assert.assertTrue("Mismatch:\nExpecting: " + integerifyAndModOutput + "\n      Got: " + out,
                          (integerifyAndModOutput == out));
    }

    @Test
    public void scryptIntegerifyAndModTest2() throws Exception
    {
        int out = this.integerifyAndMod(Hex.decode(integerifyAndMod2InputX.getBytes("US-ASCII")),
                                        integerifyAndMod2InputN);

        Assert.assertTrue("Mismatch:\nExpecting: " + integerifyAndMod2Output + "\n      Got: " + out,
                          (integerifyAndMod2Output == out));
    }
}
