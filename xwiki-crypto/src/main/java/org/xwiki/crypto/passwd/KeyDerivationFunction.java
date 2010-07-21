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
package org.xwiki.crypto.passwd;

import java.io.IOException;

import org.xwiki.component.annotation.ComponentRole;

/**
 * A key derivation function.
 * Each function must produce the same hash from the same password repeatably but there is no guarentee that a new
 * function of the same class will produce the same hash from the password. In order to make a password able to be
 * validated, you must call getSerialized() and initialize the next function with that output.
 * Each time a password is to be hashed, it should be done so with a new instance.
 *
 * @since 2.5
 * @version $Id:$
 */
@ComponentRole
public interface KeyDerivationFunction
{
    /**
     * Initialize this function with the desired key length.
     *
     * @param derivedKeyLength the desired length of the hash output.
     */
    public void init(final int derivedKeyLength);

    /**
     * @return true if this function has been initialized by calling init.
     */
    public boolean isInitialized();

    /**
     * Store this function as a byte array so another function of the same class can be initialized with the same
     * array and will then produce the same key for the given password.
     *
     * @return a byte array which can be used to recreate the same function again using init.
     * @throws IOException if something fails whithin the serialization framework.
     */
    public byte[] getSerialized() throws IOException;

    /**
     * Convert the given password to a byte array similar to the output from a message digest except specially tuned
     * for the uniwue requirements of protecting passwords.
     *
     * @param password the user supplied password.
     * @return a byte array derived from the password.
     */
    public byte[] hashPassword(final byte[] password);
}
