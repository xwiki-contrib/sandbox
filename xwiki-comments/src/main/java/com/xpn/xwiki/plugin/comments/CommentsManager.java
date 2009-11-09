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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.comments.internal.SeparatePageComment;
import com.xpn.xwiki.plugin.comments.internal.DefaultComment;

import java.util.List;
import java.util.Map;

public interface CommentsManager
{
    String COMMENT_CLASS_FIELDNAME_DATE = "date";
    String COMMENT_CLASS_FIELDNAME_AUTHOR = "author";
    String COMMENT_CLASS_FIELDNAME_COMMENT = "comment";
    String COMMENT_CLASS_FIELDNAME_PARENT = "parent";
    String COMMENT_CLASS_FIELDNAME_PAGE = "page";
    String COMMENT_CLASS_FIELDNAME_HIGHLIGHT = "highlight";
    String COMMENT_CLASS_FIELDNAME_STATUS = "status";
    String COMMENT_MODERATION_MODERATED = "moderated";
    String COMMENT_MODERATION_REFUSED = "refused";
    String COMMENT_MODERATION_ACCEPTED = "accepted";

    void init(XWikiContext context);

    void virtualInit(XWikiContext context);

    String getCommentsClassName(XWikiContext context);

    Container newContainer(XWikiContext context);

    boolean hasModeration(XWikiContext context);

    boolean isModerated(XWikiContext context) throws CommentsException;

    boolean isModerator(XWikiContext context) throws CommentsException;

    Comment addComment(Container container, String author, String content, XWikiContext context) throws CommentsException;

    Comment addComment(Container container, String author, String content, Map commentMap, XWikiContext context) throws CommentsException;

    List<Comment> getComments(Container container, int start, int count, boolean asc, XWikiContext context)
            throws CommentsException;

    List<Comment> getModeratedComments(Container container, int start, int count, boolean asc, XWikiContext context)
            throws CommentsException;

    List<Comment> getComments(String status, int start, int count, boolean asc, XWikiContext context)
            throws CommentsException;
    
    List<Comment> getModeratedComments(int start, int count, boolean asc, XWikiContext context)
            throws CommentsException;

    List<Comment> getAcceptedComments(int start, int count, boolean asc, XWikiContext context)
            throws CommentsException;

    List<Comment> getRefusedComments(int start, int count, boolean asc, XWikiContext context)
            throws CommentsException;

    Comment getComment(String commentId, XWikiContext context) throws CommentsException;

    Comment getComment(Container container, int id, XWikiContext context)
            throws CommentsException;

    boolean removeComment(Comment comment, XWikiContext context) throws CommentsException;

    Comment getParentComment(Comment comment, XWikiContext context) throws CommentsException;

    Container getContainer(Comment comment, XWikiContext context) throws CommentsException;

    List<Comment> getComments(Comment comment, int startlevel, int levelsnumber, boolean asc, XWikiContext context) throws CommentsException;

    boolean acceptComment(Comment comment, XWikiContext context) throws CommentsException;

    boolean refuseComment(Comment comment, XWikiContext context) throws CommentsException;

    boolean remoderateComment(Comment comment, XWikiContext context) throws CommentsException;

    int getNumberOfCommentsInThread(XWikiDocument document, XWikiContext context) throws CommentsException;

    int getNumberOfCommentsInThread(Comment comment, XWikiContext context) throws CommentsException;
}
