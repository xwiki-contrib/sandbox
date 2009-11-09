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
package com.xpn.xwiki.plugin.comments;

import com.xpn.xwiki.XWikiException;

public class CommentsException extends XWikiException
{
    public static final int MODULE_PLUGIN_COMMENTS = 1110;

    public static final int ERROR_COMMENTS_CREATECONTAINER_NULLSPACE = 1110001;

    public static final int ERROR_COMMENTS_ADDCOMMENT_NULLCONTAINER = 1110002;

    public static final int ERROR_COMMENTS_ADDCOMMENT_NULLCONTENT = 1110003;
    
    public static final int ERROR_COMMENTS_INVALID_COMMENT_ID = 1110004;

    // public static final int ERROR_RATINGSYSTEM_APPNOTFOUND_ON_INSTALL =
    // 1110005;

    public CommentsException()
    {
    }

    public CommentsException(int module, int code, String message)
    {
        super(module, code, message);
    }

    public CommentsException(int module, int code, String message, Exception e)
    {
        super(module, code, message, e);
    }

    public CommentsException(XWikiException e)
    {
        super();
        setModule(e.getModule());
        setCode(e.getCode());
        setException(e.getException());
        setArgs(e.getArgs());
        setMessage(e.getMessage());
    }
}
