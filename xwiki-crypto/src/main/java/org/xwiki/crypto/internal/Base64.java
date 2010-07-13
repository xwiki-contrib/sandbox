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
package org.xwiki.crypto.internal;

import java.io.UnsupportedEncodingException;


/**
 * Utility class for Base64 encoding and decoding. Supports conversion of strings containing Base64 encoded
 * data. The conversion uses UTF-8 because it is always available and Base64 alphabet is a subset of UTF-8.
 * 
 * @version $Id$
 * @since 2.5
 */
public final class Base64
{
    /** Charset used for String <-> byte[] conversion. The conversion cannot fail, because UTF-8
     *  is always available. */
    private static final String CHARSET = "utf-8";

    /** Default line length for {@link #encodeToChunkedString(byte[])}. */
    private static final int DEFAULT_LINE_LENGTH = 64;

    /** New line separator. */
    private static final String NEWLINE = System.getProperty("line.separator", "\n");

    /**
     * Encode given data and return the base64 encoded result as string (no line breaks).
     * 
     * @param data the data to encode
     * @return base64 encoded data
     */
    public static String encodeToString(byte[] data)
    {
        try {
            return new String(encode(data), CHARSET);
        } catch (UnsupportedEncodingException exception) {
            // cannot happen
            throw new RuntimeException(exception);
        }
    }

    /**
     * Encode given data and return the base64 encoded result as string, chunking it into several lines
     * of the default length (64).
     * 
     * @param data the data to encode
     * @return base64 encoded data
     */
    public static String encodeToChunkedString(byte[] data)
    {
        return encodeToChunkedString(data, DEFAULT_LINE_LENGTH);
    }

    /**
     * Encode given data and return the base64 encoded result as string, chunking it into several lines
     * of the given length.
     * 
     * @param data the data to encode
     * @param lineLength maximal line length
     * @return base64 encoded data
     */
    public static String encodeToChunkedString(byte[] data, int lineLength)
    {
        StringBuilder result = new StringBuilder();
        String encoded = encodeToString(data);
        int begin = 0;
        int end = lineLength;
        while (end < encoded.length()) {
            result.append(encoded.substring(begin, end));
            result.append(NEWLINE);
            begin = end;
            end += lineLength;
        }
        result.append(encoded.substring(begin));
        result.append(NEWLINE);
        return result.toString();
    }

    /**
     * Encode given data and return the base64 encoded result as a byte array.
     * 
     * @param data the data to encode
     * @return base64 encoded data array
     */
    public static byte[] encode(byte[] data)
    {
        return org.bouncycastle.util.encoders.Base64.encode(data);
    }

    /**
     * Decode the base64 encoded data represented as string.
     * 
     * @param base64Encoded base64 encoded data string
     * @return the decoded data array
     */
    public static byte[] decodeFromString(String base64Encoded)
    {
        try {
            return decode(base64Encoded.getBytes(CHARSET));
        } catch (UnsupportedEncodingException exception) {
            // cannot happen
            throw new RuntimeException(exception);
        }
    }

    /**
     * Decode the base64 encoded data array.
     * 
     * @param base64Encoded base64 encoded data
     * @return the decoded data array
     */
    public static byte[] decode(byte[] base64Encoded)
    {
        return org.bouncycastle.util.encoders.Base64.decode(base64Encoded);
    }
}

