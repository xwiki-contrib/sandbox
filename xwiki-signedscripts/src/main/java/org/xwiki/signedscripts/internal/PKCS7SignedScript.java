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
package org.xwiki.signedscripts.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.signedscripts.SignedScript;
import org.xwiki.signedscripts.SignedScriptKey;


/**
 * Implementation of a signed script. The signature is stored in PKCS7 format with embedded signer
 * certificate and detached content.
 * 
 * @version $Id$
 * @since 2.5
 */
public class PKCS7SignedScript implements SignedScript
{
    /** Separates key-value pairs from code. */
    private static final String CODE_SEPARATOR = "------------------------------------------------------------";

    /** Format of a key-value pair. */
    private static final String KEY_VALUE_FORMAT = "%-13s : %s\n";

    /** Format of a long signature. */
    private static final String SIGNATURE_FORMAT = "%-13s   %.76s\n";

    /** Maximal line width of a base64 encoded data. */
    private static final int BASE64_WIDTH = 76;

    /** Internal mapping of all signed data. */
    private final Map<SignedScriptKey, String> data;

    /**
     * Create an instance of {@link PKCS7SignedScript} by parsing the given string. The signature
     * needs to be verified afterwards.
     * 
     * @param script signed script as created by {@link #toString()}
     * @throws IOException on errors
     */
    PKCS7SignedScript(String script) throws IOException
    {
        this.data = parse(script);

        checkConsistency();
    }

    /**
     * Create an instance of {@link PKCS7SignedScript} for the given string. Used to sign the script
     * object afterwards.
     * 
     * @param code the script to sign
     * @param fingerprint fingerprint of the certificate to use
     * @throws GeneralSecurityException on errors
     */
    PKCS7SignedScript(String code, String fingerprint) throws GeneralSecurityException
    {
        this.data = new HashMap<SignedScriptKey, String>();

        // the current parser always produces one newline at the end of code
        // TODO remove this workaround when the parser is fixed
        final String nl = "\n";
        String sanitizedCode = code.replaceAll("\\n+$", nl);
        if (!sanitizedCode.endsWith(nl)) {
            sanitizedCode += nl;
        }

        // normalize EOL
        sanitizedCode = sanitizedCode.replaceAll("\r\n", nl);
        sanitizedCode = sanitizedCode.replaceAll("\r", nl);

        set(SignedScriptKey.CODE, sanitizedCode);
        if (!isSet(SignedScriptKey.CODE)) {
            throw new GeneralSecurityException("The script to sign is empty");
        }
        set(SignedScriptKey.FINGERPRINT, fingerprint);
        if (!isSet(SignedScriptKey.FINGERPRINT)) {
            throw new GeneralSecurityException("The fingerprint is empty");
        }
    }

    /**
     * Create an instance of {@link PKCS7SignedScript} by combining the data from the given
     * {@link SignedScript} and Base64 encoded signature. The signature needs to be verified afterwards.
     * 
     * @param preparedScript script object initialized with all needed data
     * @param base64Signature Base64 encoded signature of data obtained by {@link #getDataToSign()}
     * @throws IOException on errors
     */
    PKCS7SignedScript(SignedScript preparedScript, String base64Signature) throws IOException
    {
        this.data = new HashMap<SignedScriptKey, String>();

        for (SignedScriptKey key : SignedScriptKey.values()) {
            if (key == SignedScriptKey.SIGNATURE) {
                continue;
            }
            set(key, preparedScript.get(key));
        }
        set(SignedScriptKey.SIGNATURE, base64Signature);
        checkConsistency();
    }

    /**
     * Check the consistency of the internal data.
     * 
     * @throws IOException if some inconsistency was found
     */
    private void checkConsistency() throws IOException
    {
        // check that all mandatory keys are present
        if (!isSet(SignedScriptKey.CODE)) {
            throw new IOException("Signed script not found.");
        }
        for (SignedScriptKey key : SignedScriptKey.values()) {
            if (!key.isOptional() && !this.data.containsKey(key)) {
                throw new IOException("Mandatory key \"" + key.toString() + "\" not found.");
            }
        }

        // check consistency of the rights list
        // TODO only "programming" is currently supported
        String allowRights = get(SignedScriptKey.ALLOWRIGHTS);
        if (allowRights != null && !SignedScriptKey.ALLOWRIGHTS.getDefault().equals(allowRights)) {
            throw new IOException("Custom " + SignedScriptKey.ALLOWRIGHTS + " are not supported yet.");
        }

        // check consistency of the actions list
        // TODO restrictions are not supported yet
        String allowActions = get(SignedScriptKey.ALLOWACTIONS);
        if (allowActions != null && !SignedScriptKey.ALLOWACTIONS.getDefault().equals(allowActions)) {
            throw new IOException(SignedScriptKey.ALLOWACTIONS + " is not implemented yet.");
        }
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.SignedScript#toString()
     */
    @Override
    public String toString()
    {
        return serialize();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.SignedScript#serialize()
     */
    public String serialize()
    {
        StringBuilder builder = new StringBuilder();
        for (SignedScriptKey key : SignedScriptKey.values()) {
            switch (key) {
                case CODE:
                    builder.append(CODE_SEPARATOR).append('\n');
                    builder.append(get(SignedScriptKey.CODE));
                    break;
                case INVALID:
                    // ignore
                    break;
                default:
                    String value = "*ERROR*";
                    if (isSet(key)) {
                        value = get(key);
                    } else if (key.isOptional()) {
                        continue;
                    }
                    if (key == SignedScriptKey.SIGNATURE) {
                        final int size = Math.min(BASE64_WIDTH, value.length());
                        int pos = size;
                        builder.append(String.format(KEY_VALUE_FORMAT, key.toString(), value.substring(0, size)));
                        while (pos < value.length()) {
                            builder.append(String.format(SIGNATURE_FORMAT, "", value.substring(pos)));
                            pos += size;
                        }
                    } else {
                        builder.append(String.format(KEY_VALUE_FORMAT, key.toString(), value));
                    }
                    break;
            }
        }
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.SignedScript#getCode()
     */
    public String getCode()
    {
        return get(SignedScriptKey.CODE);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.SignedScript#get(org.xwiki.crypto.data.SignedScriptKey)
     */
    public String get(SignedScriptKey key)
    {
        return this.data.get(key);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.data.SignedScript#isSet(org.xwiki.crypto.data.SignedScriptKey)
     */
    public boolean isSet(SignedScriptKey key)
    {
        String value = get(key);
        boolean result = value != null && value.trim().length() > 0;
        if (key.isOptional()) {
            result &= !key.getDefault().equals(value);
        }
        return result;
    }

    /**
     * Set a key to a given value.
     * 
     * @param key the key to use
     * @param value the value to use
     */
    void set(SignedScriptKey key, String value)
    {
        this.data.put(key, value);
    }

    /**
     * Create a string with the data that should be signed/verified.
     * 
     * @return data to sign/verify
     * @throws IOException on errors
     */
    public String getDataToSign() throws IOException
    {
        StringBuilder builder = new StringBuilder();

        for (SignedScriptKey key : SignedScriptKey.values()) {
            if (key == SignedScriptKey.SIGNATURE) {
                continue;
            }
            if (isSet(key)) {
                builder.append(key.toString());
                builder.append(get(key));
            } else if (!key.isOptional()) {
                throw new IOException("Missing mandatory key: " + key);
            }
        }
        if (builder.length() == 0) {
            throw new IOException("Data is empty");
        }
        return builder.toString();
    }

    /**
     * Parse the content of a signed macro and build a key-value map containing signature details.
     * The macro to be executed is stored under the special key "Code".
     * TODO write JavaCC parser
     * 
     * @param content the content of a signed macro to be parsed
     * @return the mapping containing the found information
     * @throws IOException on errors
     */
    private Map<SignedScriptKey, String> parse(String content) throws IOException
    {
        Map<SignedScriptKey, String> map = new HashMap<SignedScriptKey, String>();
        Pattern comment = Pattern.compile("^([^#]*)(#.*)?$");
        Pattern keyValue = Pattern.compile("^(\\w+)\\s*:\\s*(.*)$");
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        SignedScriptKey key = SignedScriptKey.INVALID;
        String value = "";
        boolean code = false;
        while ((line = reader.readLine()) != null) {
            if (code) {
                // use as-is
                value += line + '\n';
                continue;
            }
            // strip comments
            Matcher match = comment.matcher(line);
            if (match.matches()) {
                line = match.group(1);
            }
            // remove redundant spaces
            line = line.trim();
            Matcher kv = keyValue.matcher(line);
            if (kv.matches()) {
                // found key-value
                key = SignedScriptKey.byKey(kv.group(1));
                value = kv.group(2);
                map.put(key, value);
            } else if (line.matches("-+")) {
                // found code separator
                code = true;
                key = SignedScriptKey.CODE;
                value = "";
            } else {
                // append next line to the value
                value += line;
                map.put(key, value);
            }
        }
        map.put(key, value);
        return map;
    }
}

