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
package org.xwiki.crypto.data.internal;

import java.util.Enumeration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;

import java.security.KeyStoreException;
import java.security.GeneralSecurityException;

import org.xwiki.crypto.data.XWikiX509Certificate;
import org.xwiki.crypto.data.XWikiX509KeyPair;
import org.xwiki.crypto.internal.Convert;


import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
//
import org.bouncycastle.asn1.pkcs.PKCS12PBEParams;
import java.security.Key;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import org.bouncycastle.jce.provider.JDKPKCS12KeyStore;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.SecretKeyFactory;
import java.security.Provider;
import java.security.SecureRandom;
import javax.crypto.CipherSpi;
import org.bouncycastle.jce.provider.JCEPBEKey;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import javax.crypto.interfaces.PBEKey;
//



/**
 * Wrapper for storing a {@link PrivateKey} and the corresponding {@link XWikiX509Certificate}.
 * 
 * @version $Id$
 * @since 2.5
 */
public final class DefaultXWikiX509KeyPair implements XWikiX509KeyPair
{
    /** Marks the beginning of a private credential in a string. */
    private static final String BEGIN_CREDENTIAL = "-----BEGIN BASE64 ENCODED PKCS12 CREDENTIAL-----";

    /** Marks the end of a private credential in a string. */
    private static final String END_CREDENTIAL = "-----END BASE64 ENCODED PKCS12 CREDENTIAL-----";

    /** The encryption provider (Bouncycastle). */
    private final String provider = "BC";

public static final Provider BC_PROV = new BouncyCastleProvider();
/*
public static final SecureRandom RANDOM = new SecureRandom();
*/
    /** The type of key store. */
    private final String keyStoreType = "PKCS12";

    /** Binary form of the PKCS#12 container which this class wraps. */
    private final byte[] pkcs12Bytes;

    /** 
     * Array of certificates starting with the cert matching the private key at index 0 and 
     * ascending to the root certificate authority at the highest index.
     */
    private final XWikiX509Certificate[] certChain;

    /**
     * Create new {@link XWikiX509KeyPair}.
     * 
     * @param key the private key to use.
     * @param certificates an array containing at index 0 the certificate matching the private key, and at indexes above
     *                     that index, the chain of certificates ascending up to the root certificate authority.
     * @param password the password to require if a user wants to extract the private key.
     *                 This password will also be applied to the PKCS#12 output if this is exported.
     *                 The strength of the PKCS#12 cryptography grows with the length of the password and symmetrical 
     *                 encryption using keys larger than 128 bits is not allowed by Java unless you install Unlimited 
     *                 Strength Jurisdiction Policy Files available on the Sun Microsystems website.
     * @throws GeneralSecurityException if initializing or saving of the key store fails.
     */
    public DefaultXWikiX509KeyPair(final PrivateKey key,
                                   final String password,
                                   final X509Certificate... certificates)
        throws GeneralSecurityException
    {
        if (certificates == null || certificates.length < 1 || password == null || key == null) {
            throw new IllegalArgumentException("You must provide a private key, a password and "
                                               + "at least one certificate.");
        }
        final Certificate[] nonX509Certs = new Certificate[certificates.length];
        for (int i = 0; i < certificates.length; i++) {
            nonX509Certs[i] = (Certificate) certificates[i];
        }

        final java.security.KeyStoreSpi store = new JDKPKCS12KeyStore(BC_PROV,
                                                                      PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4,
                                                                      PKCSObjectIdentifiers.pbeWithSHAAnd128BitRC4);
/*
        {
            protected byte[] wrapKey(
                String                  algorithm,
                Key                     key,
                PKCS12PBEParams         pbeParams,
                char[]                  password)
                throws IOException
            {
                PBEKeySpec          pbeSpec = new PBEKeySpec(password);
                byte[]              out;
        
                try
                {
                    SecretKeyFactory    keyFact = SecretKeyFactory.getInstance(
                                                        algorithm, DefaultXWikiX509KeyPair.BC_PROV);
                    PBEParameterSpec    defParams = new PBEParameterSpec(
                                                        pbeParams.getIV(),
                                                        pbeParams.getIterations().intValue());
        
                    //Cipher cipher = Cipher.getInstance(algorithm, DefaultXWikiX509KeyPair.BC_PROV);

                    //System.out.println(algorithm);


                    DefaultXWikiX509KeyPair.MyCipher mc = new DefaultXWikiX509KeyPair.MyCipher();

                    mc.init(Cipher.WRAP_MODE, keyFact.generateSecret(pbeSpec), defParams, DefaultXWikiX509KeyPair.RANDOM);

                    //cipher.init(Cipher.WRAP_MODE, keyFact.generateSecret(pbeSpec), defParams);
        
                    out = mc.wrap(key);
                }
                catch (Exception e)
                {
                    throw new IOException("exception encrypting data - " + e.toString(), e);
                }
        
                return out;
            }

            protected byte[] cryptData(
                boolean               forEncryption,
                AlgorithmIdentifier   algId,
                char[]                password,
                boolean               wrongPKCS12Zero,
                byte[]                data)
                throws IOException
            {
                String          algorithm = algId.getObjectId().getId();
                PKCS12PBEParams pbeParams = new PKCS12PBEParams((ASN1Sequence)algId.getParameters());
                PBEKeySpec      pbeSpec = new PBEKeySpec(password);
        
                try
                {
                    SecretKeyFactory keyFact = SecretKeyFactory.getInstance(algorithm, DefaultXWikiX509KeyPair.BC_PROV);
                    PBEParameterSpec defParams = new PBEParameterSpec(
                        pbeParams.getIV(),
                        pbeParams.getIterations().intValue());
                    DefaultXWikiX509KeyPair.MyJCEPBEKey key = (DefaultXWikiX509KeyPair.MyJCEPBEKey) keyFact.generateSecret(pbeSpec);
        
                    key.setTryWrongPKCS12Zero(wrongPKCS12Zero);
        
                    //Cipher cipher = Cipher.getInstance(algorithm, DefaultXWikiX509KeyPair.BC_PROV);
                    DefaultXWikiX509KeyPair.MyCipher mc = new DefaultXWikiX509KeyPair.MyCipher();

                    int mode = forEncryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
                    mc.init(mode, key, defParams, DefaultXWikiX509KeyPair.RANDOM);
                    return mc.doFinal(data);
                }
                catch (Exception e)
                {
                    throw new IOException("exception decrypting data - " + e.toString(), e);
                }
            }

        };*/

//KeyStore.getInstance(keyStoreType, provider);
        try {
            store.engineLoad(null, null);
            store.engineSetKeyEntry("",
                              key,
                              null,
                              nonX509Certs);
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            store.engineStore(os, password.toCharArray());
            this.pkcs12Bytes = os.toByteArray();
        } catch (IOException e) {
            // If it's a wrapped illegal key size exception then we should give the lecture about import regulations
            // and Java Cryptography Extension Policy Files.
            if (e.getMessage().contains("encrypting data - java.security.InvalidKeyException: Illegal key size")) {
                throw new GeneralSecurityException(e.getMessage() + "\nThis is probably because you are trying to use "
                                                   + "a password longer than 7 characters and you have not imported "
                                                   + "the Java Cryptography Extension Unlimited Strength Jurisdiction "
                                                   + "Policy Files.\nSome localities have import regulations on "
                                                   + "cryptography with keys larger than a given size. PKCS#7 keys "
                                                   + "grow with the length of the password and as a result passwords "
                                                   + "longer than 7 characters are not possible without installing the "
                                                   + "Unlimited Strength Jurisdiction Policy Files which are available "
                                                   + "on the Sun Microsystems website.", e);
            }
            throw new GeneralSecurityException("Failed to initialize or save key store", e);
        } finally {
            //DefaultXWikiX509KeyPair.cleanStore(store);
        }

        this.certChain = new XWikiX509Certificate[certificates.length];
        for (int i = 0; i < certificates.length; i++) {
            this.certChain[i] = new XWikiX509Certificate(certificates[i]);
        }
    }
/*
    public static class MyCipher extends org.bouncycastle.jce.provider.JCEBlockCipher.DESedeCBC
    {
        public void init(int a, Key b, AlgorithmParameterSpec c, SecureRandom d) throws GeneralSecurityException
        {
            super.engineInit(a, b, c, d);
        }

        public byte[] wrap(Key a) throws GeneralSecurityException
        {
            return super.engineWrap(a);
        }

        public Key unwrap(byte[] a, String b, int c) throws GeneralSecurityException
        {
            return super.engineUnwrap(a, b, c);
        }

        public byte[] doFinal(byte[] input) throws GeneralSecurityException
        {
            return super.engineDoFinal(input, 0, input.length);
        }
    }

    public static class MyJCEPBEKey extends JCEPBEKey
    {
        // Not used but the compiler demands it.
        public MyJCEPBEKey()
        {
            super(null, null, 0, 0, 0, 0, null, null);
        }

        boolean tryWrong = false;

        public void setTryWrongPKCS12Zero(boolean tryWrong)
        {
            this.tryWrong = tryWrong;
        }
    
        public boolean shouldTryWrongPKCS12()
        {
            return tryWrong;
        }
    }
*/
    /**
     * Create new {@link XWikiX509KeyPair} from a PKCS#12 store in base 64 String format.
     * 
     * @param pkcs12Base64String this String will be searched for {@link BEGIN_CREDENTIAL} and {@link END_CREDENTIAL}
     *                           anything between those will be assumed to be base64 encoded PKCS#12 data representing
     *                           the credential to import. If there are multiple credentials, only the first will be
     *                           parsed.
     * @param password the password to use to open the PKCS#12 store.
     * @throws GeneralSecurityException if reading the PKCS#12 store fails.
     */
    public DefaultXWikiX509KeyPair(final String pkcs12Base64String,
                                   final String password)
        throws GeneralSecurityException
    {
        this(Convert.fromBase64String(pkcs12Base64String, BEGIN_CREDENTIAL, END_CREDENTIAL), password);
    }

    /**
     * Create new {@link XWikiX509KeyPair} from a PKCS#12 store.
     * 
     * @param pkcs12Bytes the PKCS#12 to get the certificate and private key from.
     *                    the certificate in this container must be an {@link X509Certificate}
     * @param password the password to use to open the PKCS#12 store, 
     *                 this password will also be demanded to get at the private key.
     * @throws GeneralSecurityException if loading the key store fails.
     */
    public DefaultXWikiX509KeyPair(final byte[] pkcs12Bytes,
                                   final String password)
        throws GeneralSecurityException
    {
        final KeyStore store = KeyStore.getInstance(keyStoreType, provider);
        X509Certificate[] certificates = null;

        try {
            store.load(new ByteArrayInputStream(pkcs12Bytes), password.toCharArray());
            certificates = (X509Certificate[]) store.getCertificateChain("");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Only PKCS#12 containers with X509Certificates are accepted.");
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to load key store to parse PKCS#12 container", e);
        } finally {
            DefaultXWikiX509KeyPair.cleanStore(store);
        }

        this.certChain = new XWikiX509Certificate[certificates.length];
        for (int i = 0; i < certificates.length; i++) {
            this.certChain[i] = new XWikiX509Certificate(certificates[i]);
        }

        this.pkcs12Bytes = pkcs12Bytes;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getCertificate().hashCode();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof DefaultXWikiX509KeyPair) {
            return this.pkcs12Bytes.equals(((DefaultXWikiX509KeyPair) obj).pkcs12Bytes);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#toString()
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("XWikiPrivateCredential\n");
        builder.append("------------\n");
        builder.append(getCertificate().toString());
        builder.append("Private key cannot be shown without a password.");
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#getCertificates()
     */
    public XWikiX509Certificate[] getCertificates()
    {
        return this.certChain;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#getCertificate()
     */
    public XWikiX509Certificate getCertificate()
    {
        return this.getCertificates()[0];
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#getPublicKey()
     */
    public PublicKey getPublicKey()
    {
        return getCertificate().getPublicKey();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#getPrivateKey()
     * @throws GeneralSecurityException if loading the key store fails.
     */
    public PrivateKey getPrivateKey(final String password)
        throws GeneralSecurityException
    {
        final KeyStore store = KeyStore.getInstance(keyStoreType, provider);
        try {
            store.load(new ByteArrayInputStream(pkcs12Bytes), password.toCharArray());
            return (PrivateKey) store.getKey("", null);
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to load key store to get the private key", e);
        } finally {
            DefaultXWikiX509KeyPair.cleanStore(store);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#getFingerprint()
     */
    public String getFingerprint()
    {
        return getCertificate().getFingerprint();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#toPKCS12()
     */
    public byte[] toPKCS12()
    {
        return this.pkcs12Bytes;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.XWikiPrivateCredential#toBase64PKCS12()
     */
    public String toBase64PKCS12()
    {
        return BEGIN_CREDENTIAL
             + Convert.getNewline()
             + Convert.toChunkedBase64String(this.pkcs12Bytes)
             + Convert.getNewline()
             + END_CREDENTIAL;
    }

    /**
     * Make sure there are no keys left in the key store.
     * The store should not leak but by deleting the entries here, we can rest easier.
     *
     * @param store the KeyStore to clean of all it's keys.
     * @throws GeneralSecurityException if getting the list of aliases fails.
     */
    private static void cleanStore(final KeyStore store)
        throws GeneralSecurityException
    {
        final Enumeration<String> aliases = store.aliases();
        while (aliases.hasMoreElements()) {
            try {
                store.deleteEntry(aliases.nextElement());
            } catch (KeyStoreException e) {
                // probably a "no such entry" exception, safe to ignore since the key is still dropped.
            }
        }
    }
}
