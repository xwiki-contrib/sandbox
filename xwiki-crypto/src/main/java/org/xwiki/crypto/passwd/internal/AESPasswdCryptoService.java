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
package org.xwiki.crypto.passwd.internal;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.xwiki.component.annotation.Component;


/**
 * A password crypto service implementing AES-256 with SHA-384.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("AES-256")
public class AESPasswdCryptoService extends DefaultPasswdCryptoService
{
    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.internal.DefaultPasswdCryptoService#getCipher()
     */
    @Override
    protected BlockCipher getCipher()
    {
        return new AESEngine();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.internal.DefaultPasswdCryptoService#getKeyLength()
     */
    @Override
    protected int getKeyLength()
    {
        return 32;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.internal.DefaultPasswdCryptoService#getDigest()
     */
    @Override
    protected Digest getDigest()
    {
        return new SHA384Digest();
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.passwd.internal.DefaultPasswdCryptoService#getHeader()
     */
    @Override
    protected String getHeader()
    {
        return "------BEGIN PASSWORD AES256CBC-SHA384 CIPHERTEXT-----\n";
    }
}

