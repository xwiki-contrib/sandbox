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
        "29035cc0751930ac18833d1adbb8c7fd0ed2988d9e8979e703df9081206948c91d3a0603c911cd289dd76c9699f50f002146d506ecc6"
      + "a48e797b31b772879a93096dcd7473a2b7569db2729dcfbb3bf85e1ba0379be5d8add5731cf5786aa0d04eb860f8236b01d5ab8f09e2"
      + "c551f6cf27d48c0d1388f2dd9548b95890c318019c8ff1c69b9d89b4bdd4cc9a797201a05c614260f9a13f9a030527b58060acecd5cf"
      + "dac7647e139892e7f901d976cf51e52c35ee7621b386af0627182be9e800375baf9b401df3468d6316374453179bb71b118cbbb79ca2"
      + "191af11e1587de5833814e7036866a2b5b1129725b9f3214afecaa95a0cf61fbd64b878caa64583ee3510717572fa6d1630ceed6b480"
      + "01a604fb43f458df640c4ecf78fea40b799e2c4f50804af4c041e0341cdb90fef5cd486fb07edf3a76fd2d3711c311f4b950d0d13215"
      + "73438d84fdf71c743e2660f8286712388cc24aaa370ec34634c33c59801978bce75e61f582653d60a1754b9148f41a841b58d8309290"
      + "2dfe4c01587a7e08515c15f5196b54c6f3c8585afb47f8acbd41a9b3fb17afc7890609ab46952010c1454b14484135eb6277ba1b240e"
      + "3e064d3c385f3ff4ad0e54674d29d2ded854353eccdf055584cbf3016ea18665521ceb259edb8a1734c393ebfcdf65ff08c5496cb0b5"
      + "c285d21cca0dcdb3ece3d89919ba92ed6cdbe6a3e09a4c84f5e467fed96445f37af656f2f34cb86916d20983c01d97a6a56033742327"
      + "37f0d1ef5e7202c490d1af2ffac470d4fc4fb963bb387c8670df2010aec223420accada12e7cddc3261edf4377f2d600ec992b2e3da0"
      + "50c18c72f7c55da70025cabf2afd8d294f7279e5664a242bb25b22493265f6778c85aa730e1baaaec45e89f4675c6ac5548b90677a15"
      + "42271a7f4599b4e944e5b5e25035a450d57c86ad059c5ccf2688b7790d92486e450ee504ed0a2e0c8be9b04c6a18400b5859e2eab592"
      + "e5c53e41ebf1c4ef2abb75531d36f60ef816e56f0f5aafe9d1efd829161d4e6f09f9c62bbcd418f5e9a762e22acc12f9d96ec5bf71be"
      + "2977676a99a38997d2482d0e70eceb2d535447e13d559d3ce5a0342be5af2e6a2b3e26e831425414ee8860984059659281923fc6f4a3"
      + "a03c426f6b1aafc95289ac58d12c92e3ddd7c4a2de3ba7d81d2804bab0fa2f0c41c93e1723b7e32ab794e4052f748c487fad09cde630"
      + "991ce93960877872efbaf1219cbc52db499c61a042afe88f7fa3a9b47fe9859b9d995677bf5701ad29050ca026153ee1034670148783"
      + "f17f06069b776963c0c65241ba7b49b322cbf2455baea29c9a1b5cffc11e79b41e7edad139f6567b971f"
      + /* The part which integerify takes --> */ "bfcfe14e" /* <-- */ + "2f4cedba4e743e4c718cf22d32856392f77f6e91be3"
      + "d9cffc82055fb4f6e78c1c989d4376d09af0b40a518e3bac775a1976748d9492db3b3fc5ad27c";

    private final int integerifyAndModInputN = 131072;

    private final int integerifyAndModOutput = 118719;

    private final int integerifyAndModInteger = 1323421631;

    private final String integerifyAndMod2InputX =
        "87b6bd32acc0101d15d45a285ac434cd79b1001f8787734800119515688ecb11cb59cda924ce34457eec681aeed0bf307370ac538d54"
      + "f93f6d02ecf44687f7780221075f621080bf4e89f575dc45975f25e08eefd5aa7f756ad8f400a4f8e0a485befa5d33272a055ff35a65"
      + "fb9c0fb9f3bc6964a6620427e7f4ef2eccb79b967d4492473dca8b9712b952151aa66d3ff82cc6b3ba6dc4c37c63d69555af32179bc6"
      + "7481e04062b7a6cdb6c6b5f19b21c4aa5aaa7379dea0ce3cdeb16d30d3560bcf92daa0aa7190f2978388b6e221bf08536ca3004cc9ad"
      + "4b18a7c6b0fa1bd9ba9434a9fb5ca18d7b91a00be44e2e3b1abc631958d6c11275fcc0d877ce6a00838874a6d831a7aaf3f3fdfd35d6"
      + "7a1fd3857830e489648c7a9be6207ead8c947759a6d8622db3c6ef107bc262d5c34ded7b1b282ff58fb85a95ebcf77076ff740a5c22e"
      + "bb6b0cf5d684021f8ce12d70a429b83145c5d413e062e9a9c8f854e0bae0ec072fd343b0c64fdb20ce19806731df34fb38274149d7e3"
      + "a57c8717111c32aac249b8e9db6370933f4b3e8f0835dcb9d0b8ee084055b41db8c607d62b83e34132fc038003a15dc473049c6c53e3"
      + "5c5357a04c3eb4ec134f72c6055b63f82f85a1943d7c9a56cb0e4c8ecc7714f508e684e8afe15bed885d0bf6e2cfb703eb86044c6f9f"
      + "9564eb3265d047d217ee3845089f0299a887001211affaa89621175e8ff469dc9c40e145ec4dce2f3fbb2ef57de99e42bb9aa36dce0d"
      + "c60d0afd663fdea41b27afd3e699ba1c8a9c3698b048b7dad76f5a431872aba7c4b701728122c59d3a9fc6836c209d92b0fcd7e717fd"
      + "035aaf2e04ab070e79a7a3e40688c732e74960975f0fdba42fcfc5397ad2b81c2591a9cf83807348e099ceab9f39505bc8c70612b3ee"
      + "c5f555de5e54e684ab919809cbee4e334b79733fe26eafe3f4ac0e420dbcf9df4b271ef31fb44b8af4df3a050941e772e3c8d181ad8e"
      + "2bf93e9088eaa5114fd321b3df63856337bb8f65a095197871f6f6f67f318c9a4c31c1a1f994f85396cf32c40ea3ed716ac61b91c004"
      + "c02bb4a093398f0b6de6f942ae7aff0d2f4a1f9ece4262ac77a10befe583dd8c8e3be78fdab22c5a7174f66fe51c45bdb14f8b98007d"
      + "bd4c6ba52ffba2bcd0362b5074d0e308a4bd45af0e1a5db8ac8ecdc0728857faf7ab057507891d47f05d57e3d3e508cc3dd6f6d65387"
      + "3d1d0cb5982103aa78d253ebb9b2e3110ebf865b4c33992c8f1e32f00c0c8eb2d4897fda50cd18c3cdb7d01a2edb552a4a2239447a9d"
      + "ac5c172bad55c55c4166be7455a27fa77db64c26868a6e3feb153fda4e166608784e654dac26d7fd3c1c"
      + /* The part which integerify takes --> */ "46b6c53a" /* <-- */ + "1da5eca004169ce62559710d67d5d99260fe9b09b9c"
      + "1e6bd52175d27c9c7e97e47cb6dbc24fa8f97ad342b897dddc5aecabf68b3624f3272fa7965a2";

    private final int integerifyAndMod2InputN = 131072;

    private final int integerifyAndMod2Output = 112198;

    private final int integerifyAndMod2Integer = 986035782;

    /* BlockMix Tests */

    private final String blockMixInput =
        "20df6ae5e1fbe1039ed4739c0f0e786cff435342063418b46b8d572b40a37b18a8b4e1c798745ec4eceddac3eafd367a844e0e2f08c0"
      + "8f4af7d87c11ef7c793c2fedeffc16ebc9f7757744948fb358ea301b013d6e19faeffac127cb5bc67f230e7d3a7188957bcb8569589b"
      + "ff3e9f2fb0ae32ab009953d381df7dd863c7150386ee043ca98e98998ac874a1e0d77fa8f90a288447fff54d8f7709d470271c85be67"
      + "78bb6ace3cd0d49351a1de2ba9a0f047fd3c49ae0b210b99c8dbbe1fef75ce661b123a279c6ba907873df13f76f33d6a6eb0e29ca3a3"
      + "8c53f5914b2353affed668b99fa6b4063f0eaa191acfd95773f387e84c62ac4b955d82924dca3c8bb062daf214ee84387b1b4e5d6d2b"
      + "1e7a0093dc22aa4c4a3301fdb075799ed7f9293846ad046736c4f05e827d3e9ccd4793c6362454a88ff1dcd960eef5a1161f4d1c47cf"
      + "63c7bc772b225b7d035b57e7a7eb8e7c761c35b9ae6dd683ac10d6b7a66d88f5bba8eac6559e55063ee7ef820c27d05ee1c98636c52f"
      + "26b37a4c1462c263ae177ea468f62f80025352d0e507ce2fba6bf7d04d47aaaf376983711484b439212c794f17cac8d72411528b9d24"
      + "409e0a7d2de642ae7d9e125e117ab1292aaf3fe92505c8cdf0b82031b80c51d90380470c3f31f5fb26fb5b5edaa12468d8a233c74eed"
      + "3980feb3ad9d820853704078fcc032fbc9b4d267ed647c632c864e92d64744ac3a9ab1ef0b5273ac6415534feec297830c2d75cf0ff4"
      + "d23372ddfabddf1ce32ca7d9a9ab77c5b7fa2e525e49ea02243bd6ade1891633d1b74f0f1d873b30cebb3ae22830d3404c9cfa07b948"
      + "8e98319b948d44898009372375b9cad94e4adba29f9622c9ac21b44ddfe033846555514459ae3787af920a3825c38a1b81251e038f3b"
      + "0e611f0e9a9677359ec2bf145a3e4dbdd37a2501531e37fe36eddc37fab604150a0805bb94c3f9e679ab2d6d7f777f6491b9880959ee"
      + "b98795bbea9d0ceb7e75f398026b6f018c2eae42dd00f7c2ecc24782bc5ec265be104b0b4aeeab575a6489d89c1006db83a47a499019"
      + "46fb50bacae576b37babfd2080532de7506dadeb7dccd30683e31fcf30b3ed66f15c56f41cb87dc44d868ffcb8b9603cdb04e072db96"
      + "fd51f75e1449908703409b5c9e79e61c74e873dea871d913433e866f26e2fffc0f2e1fe22be5db4e230a3dd57e4092dbf1fe0d2ad156"
      + "c4edda8a8d31bc6eb7292a15e32d5817cfa7c490d8ad0bf2158b66ae0ccd9a78a8b24eeea1e62095ac1b15ce6487222ea54336db03ff"
      + "b1975bc26695091f1eb4adc358d8c143baec29f4c476bf914a36d9342905848d1e34f11f8e9838447fcf2bdbf0a095e03d377be5db2d"
      + "d0242352271c3dcb6e5402a557135904cd76b462a6dd5855bb4820e5ef49a2c0741c90da4e8f0c5215b3cec94628be25e7314c9f";

    private final String blockMixOutput =
        "9dd3f7a162703e0d5dd7406553e8719b863bb8b09ff5a0f97eb894a3e9abe9f621f799c12053f0fd842bfafe9ccb4359c0f3e6fcd47a"
      + "b472e3e7594d4ffbe846a6cd61abe45be18234856fea3d338bf40ad568809622934e7132022f82f444aeafdd24e8de30927565240b63"
      + "edd5a3e8cbc3b41c7c8fd0dae63c9de2f903da95683edbd55631387d585beb602ed22bd167a75c476543680ff352832cd9be5e3ae50d"
      + "a1848007f37ef5b49602cb00489cfcfb50401102e3f9af07ecefe3994c513569ee855b2297773a03a5e83f97bb7a84763abe88b9ccb0"
      + "340a0665ee759b51472053abd85c7c9e710e9a58a3ace3c0a20135b280df2cc62895244f7cf4c5cfa66d290f4513164ebc4f86bd40cf"
      + "9b582fa9fa5e16450f6994282eb98c295783650720a223d466a1d98cb739d1f2566bb67d4e779741e9e4158d929f02706f2ba1f425e6"
      + "f458b9383fd0c19302c319442bf341ad207cb00a3f7c64d20a7051ff74773912aebc8f03b2a3bf9cd89107183ec1ac491fe28c728b0a"
      + "e6d8368aeed76ca1a4f8b5a31e88069bc8a2c40cbde48e0453bd5246dfd208ef7c28c1312f91840e974cc7bb490f22b9ab0401e112c2"
      + "96c80ac93c863beaa5ff73309acfde9795e5fa6cdf176078f0fba225993f994b05d00caf7fdfdb481db30f8c0912a72b53b759aadd5c"
      + "64194ead458e346553f6c101c861c4bd94c3a69de1f75169edbea0af53f668c0a7c14f5685e463192681ba5ad76d1891823bc4ea8b44"
      + "e09f383206ca30bbdbb0e7431943938319402bda077f121dd15ee69044c45c7f29538b48bc4b1dc4c5d837ee5ecc215a685c7ad32edd"
      + "58549e06db7ff005366f4a2142816e69302586ae647eb13d6f83de6f24c46a7f1eb499e718732485615ff88142c65fd679c2cebdeb7a"
      + "3e28da7e6b1e6d7cde3dd9adb60c08efee16b130276735cc3567d48140995811137d374e5e7d9f71ae011506670f96b92623450f04cb"
      + "a299ef14a4385d244af47978b1a120cf6fcbfa39b340bb99be88eb0c9bd527d7255c4e646361ad1db5181591b0f289aa2515454b3d7d"
      + "ee0238d5abfdf1841630e3f1173d0e06fc2fec19c411ab7822feab332a089a77dbaea37be5b59917f3d61aa26318586ccbf2147d64e6"
      + "33ca050be0dc9453604f0576b562537322695d321d437723f06755b6b22e258f48e225a203b69c8a2a0230f03a0ad64f005ab32e4b32"
      + "5d86a115511391e7fb9662a6bab2a442a9eafb95d37bd18809914afc2990a01d1ef899513f1225b26294c3bbc5a46c68c3e90daa3322"
      + "391aad285dfe12654417041e46b7685ea5ec052363e5076135a8f03641056bd986a37570c67719230e0e253a88ef417fc68204f1955b"
      + "ac20e15046fd80b8ffbe4cd2b3d598a7fb4761b9925928937310b6776574ba8ce5c197cadf2ca5dd7e9f3fffa4055d091c67e4aa";

    /** Salsa20/8 Test */
    private final String salsa8Input = "81ffeb6d3d79838730feab1d88031943f92472484672e2ba55f0d5326d2fe089c07189bd2059c"
      + "66b0f1a1640bc041e87165116a2f1d0cd9b236985ce681aea38";

    private final String salsa8Output = "7509afc5289854822a0bd4486c2d12004d81e8cf7e5713062db7e8b3ab58ea16c5781bc0a6d1"
      + "be1476cb1138a0275755ff114341b9ad7cb3e8f8f57b54d15d23";

    /** Default Constructor needed because this class extends Scrypt to test protected methods. */
    public ScryptTest()
    {
        super(new byte[0], 2, 8, 1, 1);
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
        Scrypt engine = new Scrypt(new byte[] {'S', 'o', 'd', 'i', 'u', 'm', 'C', 'h', 'l', 'o', 'r', 'i', 'd', 'e'},
                                   16384, 8, 1, 64);
        byte[] out = engine.hashPassword(new byte[] {'p', 'l', 'e', 'a', 's', 'e', 'l', 'e', 't', 'm', 'e', 'i', 'n'});
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

    @Test
    public void scryptUnsignedModTest2() throws Exception
    {
        long[] unsignedLongs = {
            0x0000000000000065L, // 101
            0x00000000000003F2L, // 1010
            0x0000000000001003L, // 4099
            0x80000000000007D0L, // 9223372036854777808 (signed +1000)
            555932188L, // collected from crypto_scrypt
        };
        int[] modulises = {
            4,
            8,
            16,
            32,
            131072
        };
        int[] results = {
            1,  // 100 % 5
            2,  // 1010 % 8
            3,  // 4099 % 16
            16, // 9223372036854777808 % 32
            55836 // 555932188 % 131072
        };
        for (int i = 0; i < unsignedLongs.length; i++) {
            int out = this.unsignedMod(unsignedLongs[i], modulises[i]);
            Assert.assertTrue("\nExpecting: " + results[i] + "\n      Got: " + out,
                              (results[i] == out));
        }
    }

    @Test
    public void integerifyTest1() throws Exception
    {
        long out = this.integerify(Hex.decode(this.integerifyAndModInputX.getBytes("US-ASCII")));
        Assert.assertTrue("\nExpecting: " + Long.toHexString((long) integerifyAndModInteger) + "\n      Got: "
                          + Long.toHexString(out), (integerifyAndModInteger == out));
    }

    @Test
    public void integerifyTest2() throws Exception
    {
        long out = this.integerify(Hex.decode(this.integerifyAndMod2InputX.getBytes("US-ASCII")));
        Assert.assertTrue("\nExpecting: " + Long.toHexString((long) integerifyAndMod2Integer) + "\n      Got: "
                          + Long.toHexString(out), (integerifyAndMod2Integer == out));
    }

    @Test
    public void blockMixTest() throws Exception
    {
        byte[] out = Hex.decode(this.blockMixInput.getBytes("US-ASCII"));
        // Blockmix requires that memory be allocated.
        this.allocateMemory(true);
        this.blockMix(out);
        String outStr = new String(Hex.encode(out), "US-ASCII");
        Assert.assertTrue("\nExpecting: " + this.blockMixOutput + "\n      Got: "
                          + outStr, this.blockMixOutput.equals(outStr));
    }

    @Test
    public void salsa8Test() throws Exception
    {
        byte[] out = Hex.decode(this.salsa8Input.getBytes("US-ASCII"));
        this.scryptSalsa8(out);
        String outStr = new String(Hex.encode(out), "US-ASCII");
        Assert.assertTrue("\nExpecting: " + this.salsa8Output + "\n      Got: "
                          + outStr, this.salsa8Output.equals(outStr));
    }
}
