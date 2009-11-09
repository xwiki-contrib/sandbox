package com.xpn.xwiki.plugin.comments;

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

import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWikiContext;

import java.util.Date;
import java.util.List;

public interface Comment
{
    /**
     * Retrieves the parent comment of this comment
     * Returns null if there is no parent comment
     * @return Comment parent comment
     */
    Comment getParentComment();

    /**
     * Retrieves the container to which this comment applies
     * This can be a page, a comment, or something else
     * @return Container parent container
     */
    Container getContainer();

    /**
     * Retrives the current comment as a container able to receive child comments
     * @return Container container
     * @throws CommentsException
     */
    Container getAsContainer() throws CommentsException;

    /**
     * Retrives the current comment as a BaseObject
     * This method is used for compatiblity
     * @return BaseObject comment object
     * @throws CommentsException
     */
    BaseObject getAsObject() throws CommentsException;


    /**
     * Allows to access the comments manager used to manage this comment
     * @return CommentsManager comments manager
     */
    CommentsManager getCommentsManager();

    /**
     * Retrieves the comment unique ID allowing to distinguish it from other comments of the same container
     * @return  String comment ID
     */
    String getCommentId();

    /**
     * Retrieves the comment unique ID allowing to find the comment
     * @return  String comment ID
     */
    String getGlobalCommentId();

    /**
     * Retrives the current comment author
     * @return String author of the comment
     */
    String getAuthor();

    /**
     * Retrives the top page on which the comment is put (head of the thread)
     * @return String page of the comment
     */
    String getPage();

    /**
     * Retrieves the date of the comment
     * @return Date date of the comment
     */
    Date getDate();

    /**
     * Retrieves the content of the comment
     * @return  String comment content
     */
    String getContent();

    /**
     * Retrieves the status of the comment
     * It can be "unmoderated", "accepted", "refused"
     * @return  String status
     */
    String getStatus();

    /**
     * Retrieves additional properties
     * @param propertyName
     * @return Object property value
     */
    Object get(String propertyName);

    /**
     * Retrieves additional properties
     * @param propertyName
     * @return Object property value
     */
    String display(String propertyName, String mode, XWikiContext context);


    /**
     * Retrieves the list of child comments
     * @param asc Ascending or Descending (date sort order)
     * @return List of comments or lists
     */
    List<Comment> getComments(boolean asc) throws CommentsException;

    /**
     * Retrieves the list of child comments
     * @param startLevel Level to start from
     * @param levelsNumber Number of Child levels
     * @param asc Ascending or Descending (date sort order)
     * @return List of comments or lists
     */
    List<Comment> getComments(int startLevel, int levelsNumber, boolean asc) throws CommentsException;

    void setAuthor(String author);

    void setContent(String content);

    void setStatus(String status);

    void addComment(String author, String content) throws CommentsException;

    void acceptComment() throws CommentsException;

    void refuseComment() throws CommentsException;

    void remoderateComment() throws CommentsException;

    void save() throws CommentsException;

    boolean remove() throws CommentsException;

    String toString();
}
