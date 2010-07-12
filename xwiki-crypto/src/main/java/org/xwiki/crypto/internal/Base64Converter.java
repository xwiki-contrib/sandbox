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

import org.apache.commons.codec.binary.Base64;
import org.xwiki.component.annotation.Component;
import org.xwiki.crypto.Converter;


/**
 * Base64 converter.
 * 
 * @version $Id$
 * @since 2.5
 */
@Component("base64")
public class Base64Converter implements Converter
{
    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.Converter#encode(byte[])
     */
    public String encode(byte[] data)
    {
        Base64 encoder = new Base64(0);
        return encoder.encodeToString(data);
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.crypto.Converter#decode(java.lang.String)
     */
    public byte[] decode(String encoded)
    {
        return Base64.decodeBase64(encoded);
    }
}

