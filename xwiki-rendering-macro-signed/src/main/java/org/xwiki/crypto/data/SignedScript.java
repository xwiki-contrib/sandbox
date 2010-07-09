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
package org.xwiki.crypto.data;


/**
 * Represents a signed script object. Signed script can be serialized to string or created from string.
 * 
 * @version $Id$
 * @since 2.5
 */
public interface SignedScript
{
    /**
     * The output produced by this method can be parsed by {@link ScriptSigner#getVerifiedCode(String)}.
     * 
     * @return serialized representation of the signed script
     */
    String serialize();

    /**
     * Convenience method to retrieve the signed code (usually script macro). Equivalent to
     * {@link #get(SignedScriptKey.CODE)}.
     * 
     * @return the signed script content
     */
    String getCode();

    /**
     * Return the stored data by key name or null if not set.
     * 
     * @param key key to use
     * @return the corresponding value
     */
    String get(SignedScriptKey key);

    /**
     * Check whether the given key is present in the map, its value is non-empty and (in case the key
     * is optional) not equal to the default value.
     * 
     * @param key key to use
     * @return true if the key is set, false otherwise
     */
    boolean isSet(SignedScriptKey key);
}
