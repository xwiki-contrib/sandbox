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
package org.xwiki.contrib.authentication.jdbc.internal;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.authentication.jdbc.PasswordHasher;

/**
 * This class is for computing and verifying PBKDF2-HMAC-SHA1 hashes. Salt is randomly generated as it should be.
 * 
 * @author Petr Praus
 */
@Component
@Singleton
@Named("pbkdf2")
public class PBKDF2PasswordHasher implements PasswordHasher
{
    private static final Logger LOG = LoggerFactory.getLogger(PBKDF2PasswordHasher.class);

    public static final int hashLength = 160; // PBKDF2 uses internally HMAC SHA-1

    public static final int saltLength = 12; // salt length in bytes

    public static final Random random = new Random();

    public static final int defaultRounds = 10000; // default number of rounds when creating a hash (checksum)

    /**
     * Constructs PKBDF2 hash (see RFC 2898 for a description of PBKDF2).
     * 
     * @param password
     * @param salt
     * @return constructed hash
     */
    public static byte[] computePBKDF2Hash(char[] password, byte[] salt, int rounds) throws NoSuchAlgorithmException,
        InvalidKeySpecException
    {

        KeySpec spec = new PBEKeySpec(password, salt, rounds, hashLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return hash;
    }

    /**
     * Takes a plaintext password, generates random salt and creates a string for the database in the format described
     * in the comment of verify() function below. The created format needs to be understood by the verify function! The
     * string is decoded into char array using toCharArray which is UTF-16 encoding.
     * 
     * @param password User's password you wish to hash with PBKDF2
     * @return Formatted hash string: $digest$rounds$salt$checksum
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static String createPBKDF2Hash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        char[] pwd = password.toCharArray();
        byte[] salt = getRandomSalt();
        byte[] checksum = computePBKDF2Hash(pwd, salt, defaultRounds);

        // Create the hash string that is understood by verify() below.
        StringBuffer hashstr = new StringBuffer();
        hashstr.append("$").append("pbkdf2");
        hashstr.append("$").append(defaultRounds);
        hashstr.append("$").append(DatatypeConverter.printBase64Binary(salt));
        hashstr.append("$").append(DatatypeConverter.printBase64Binary(checksum));

        return hashstr.toString();
    }

    /**
     * Generate random salt of a predefined length
     * 
     * @return randomly generated salt
     */
    public static byte[] getRandomSalt()
    {
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Compares supplied password
     * 
     * @param suppliedPassword Password harvested from the user (plaintext)
     * @param dbPassword User's hashed password+params from our database in a special format dbPassword has the
     *            following format (including "$"): $digest$rounds$salt$checksum digest - actual algorithm, always
     *            "pbkdf2" in our case, implying we use pbkdf2 with SHA-1 rounds - number of PBKDF2 rounds salt - Base64
     *            encoded salt checksum - actual Base64 encoded hash value
     * @return true if hash of suppliedPassword given the parameters (rounds, salt) equals checksum part of dbPassword
     *         string.
     */
    public boolean verify(String dbPassword, String suppliedPassword)
    {
        String[] dbpw = dbPassword.split("[$]");
        if (dbpw.length != 5) {
            // password field has invalid format
            return false;
        }
        String digest = dbpw[1];
        if (!digest.equals("pbkdf2")) { // check that we have correct digest for this person in the database
            return false;
        }
        int rounds = Integer.parseInt(dbpw[2]);
        byte[] salt = DatatypeConverter.parseBase64Binary(dbpw[3]);
        byte[] dbChecksum = DatatypeConverter.parseBase64Binary(dbpw[4]);

        // byte[] sppw = suppliedPassword.getBytes(Charset.forName("UTF-8"));
        try {
            byte[] suppliedPwHash = computePBKDF2Hash(suppliedPassword.toCharArray(), salt, rounds);
            return Arrays.equals(suppliedPwHash, dbChecksum);
        } catch (GeneralSecurityException ex) {
            return false;
        }
    }
    
    @Override
    public String create(String password)
    {
        try {
            return createPBKDF2Hash(password);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Cannot find proper hashing algorithm", e);
        } catch (InvalidKeySpecException e) {
            LOG.error("Invalid keyspec", e);
        }
        return null;
    }
}
