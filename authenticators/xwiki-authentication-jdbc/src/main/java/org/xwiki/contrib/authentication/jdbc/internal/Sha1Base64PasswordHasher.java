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

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.authentication.jdbc.PasswordHasher;

/**
 * Simple password hasher: digest password with SHA-1 and store it in Base64.
 */
@Component
@Singleton
@Named("sha1base64")
public class Sha1Base64PasswordHasher implements PasswordHasher
{

    @Override
    public boolean verify(String dbPassword, String suppliedPassword)
    {
        return dbPassword.equals(create(suppliedPassword));
    }
    
    @Override
    public String create(String password)
    {
        return Base64.encodeBase64String(DigestUtils.sha1(password));
    }

}
