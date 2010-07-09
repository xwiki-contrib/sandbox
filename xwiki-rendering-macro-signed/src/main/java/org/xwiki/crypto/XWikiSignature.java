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
package org.xwiki.crypto;

import java.security.GeneralSecurityException;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.crypto.data.XWikiCertificate;
import org.xwiki.crypto.data.XWikiKeyPair;


/**
 * Signature component used to sign and verify data.
 * 
 * @version $Id$
 * @since 2.5
 */
@ComponentRole
public interface XWikiSignature
{
    /**
     * Sign given data.
     * 
     * @param data the data to sign
     * @param keyPair the key pair to use
     * @return the signature
     * @throws GeneralSecurityException on errors
     */
    byte[] sign(byte[] data, XWikiKeyPair keyPair) throws GeneralSecurityException;

    /**
     * Verify given data.
     * 
     * @param data the data to verify
     * @param signature the signature produced by {@link #sign(byte[], XWikiKeyPair)}
     * @param certificate the certificate to use
     * @return true if the verification succeeds, false otherwise
     * @throws GeneralSecurityException on errors
     */
    boolean verify(byte[] data, byte[] signature, XWikiCertificate certificate) throws GeneralSecurityException;
}

