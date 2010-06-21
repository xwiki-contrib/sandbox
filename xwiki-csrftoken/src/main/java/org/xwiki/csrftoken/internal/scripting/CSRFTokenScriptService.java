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
package org.xwiki.csrftoken.internal.scripting;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.csrftoken.CSRFToken;
import org.xwiki.script.service.ScriptService;

/**
 * Script service wrapping a {@link CSRFToken} component.
 * 
 * @version $Id: $
 * @since 2.4
 */
@Component("csrf")
public class CSRFTokenScriptService extends AbstractLogEnabled implements ScriptService
{
    /** Wrapped CSRF token component. */
    @Requirement
    private CSRFToken csrf;

    /**
     * Returns the anti-CSRF token associated with the current user.
     * Creates a fresh token on first call.
     * 
     * @return the secret token
     * @see CSRFToken#isTokenValid(String)
     */
    public String getToken()
    {
        return csrf.getToken();
    }

    /**
     * Removes the anti-CSRF token associated with the current user. Current token is invalidated
     * immediately, a subsequent call of {@link #getToken()} will generate a fresh token.
     * 
     * @see CSRFToken#clearToken()
     */
    public void clearToken()
    {
        csrf.clearToken();
    }

    /**
     * Check if the given <code>token</code> matches the internally stored token associated with the
     * current user.
     * 
     * @param token random token from the request
     * @return true if the component is disabled or the given token is correct, false otherwise
     * @see CSRFToken#isTokenValid(String)
     */
    public boolean isTokenValid(String token)
    {
        return csrf.isTokenValid(token);
    }

    /**
     * Get the URL where a failed request should be redirected to.
     * 
     * @return URL of the resubmission page with correct parameters
     * @see CSRFToken#getResubmissionURL()
     */
    public String getResubmissionURL()
    {
        return csrf.getResubmissionURL();
    }
}

