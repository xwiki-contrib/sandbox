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

package com.xpn.xwiki.plugin.comments.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.plugin.comments.*;

import java.util.*;

public class DefaultComment implements Comment
{
    private static Log LOG = LogFactory.getLog(DefaultComment.class);

    private Container container;

    private XWikiDocument document;


    private BaseObject object;

    private XWikiContext context;

    public DefaultComment(Container container, String author, String content, XWikiContext context)  throws CommentsException
    {
        this(container, author, new Date(), content, null, context);
    }

    public DefaultComment(Container container, String author, String content, Map commentMap, XWikiContext context) throws CommentsException
    {
        this(container, author, new Date(), content, commentMap, context);
    }

    public DefaultComment(Container container, String author, Date date, String content, Map commentMap, XWikiContext context)  throws CommentsException
    {
        boolean isReply = false;
        if (container != null && container.getComment() != null) {
            isReply = true;
        }
        this.container = container;
        this.context = context;

        createObject(container, author, date, content, isReply, commentMap);
    }

    public DefaultComment(Container container, XWikiDocument doc, BaseObject obj, XWikiContext context)  throws CommentsException
    {
        this.document = doc;
        this.object = obj;
        this.context = context;
        this.container = container;
    }

    public CommentsManager getCommentsManager() {
        return ((CommentsPlugin) context.getWiki().getPlugin(CommentsPlugin.COMMENTS_PLUGIN_NAME, context)).getCommentsManager(context);
    }

    /**
     * CommentId represente the comment ID. It is the object number in the default comments case
     */
    public String getCommentId() {
        return "" + object.getNumber();
    }

    /**
     * CommentId represente the comment ID. It is the object number in the default comments case
     */
    public String getGlobalCommentId() {
        return document.getFullName() + ":" + object.getNumber();
    }


    public BaseObject getAsObject()
    {
        return object;
    }

    public XWikiDocument getDocument() throws XWikiException {
        if (document==null) {
            document = context.getWiki().getDocument(container.getDocumentName(), context);
        }
        return document;
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#getAuthor()
     */
    public String getAuthor()
    {
        return object.getStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_AUTHOR);
    }

    public void setAuthor(String author) {
        object.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_AUTHOR, author);
    }

    public String getPage()
    {
        return document.getFullName();
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#getDate()
     */
    public Date getDate()
    {
        return object.getDateValue(CommentsManager.COMMENT_CLASS_FIELDNAME_DATE);
    }


    /**
     * {@inheritDoc}
     *
     * @see Comment#getContent()
     */
    public String getContent()
    {
        return object.getLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_COMMENT);
    }

    public void setContent(String content) {
        object.setLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_COMMENT, content);
    }

    public String getStatus() {
        return getAsObject().getStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_STATUS);
    }


    public void setStatus(String status) {
        getAsObject().setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_STATUS, status);
    }


    /**
     * {@inheritDoc}
     *
     * @see Comment#get(String)
     */
    public Object get(String propertyName) {
        try {
            return ((BaseProperty)getAsObject().get(propertyName)).getValue();
        } catch (XWikiException e) {
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see Comment#display(String,String,XWikiContext)
     */
    public String display(String propertyName, String mode, XWikiContext context) {
        return document.display(propertyName, mode, object, context);
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#getParentComment()
     */
    public Comment getParentComment()
    {
        return container.getComment();
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#getContainer()
     */
    public Container getContainer()
    {
        return container;
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#addComment(String, String)
     */
    public void addComment(String author, String content) throws CommentsException {
        new DefaultComment(getAsContainer(), author, content, context);
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#getComments(boolean)
     */
    public List<Comment> getComments(boolean asc) throws CommentsException {
        return getCommentsManager().getComments(this, 0, 1, asc, context);
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#getComments(int, int, boolean)
     */
    public List<Comment> getComments(int startlevel, int levelsnumber, boolean asc) throws CommentsException {
        return getCommentsManager().getComments(this, startlevel, levelsnumber, asc, context);
    }

    public void acceptComment() throws CommentsException {
        setStatus(CommentsManager.COMMENT_MODERATION_ACCEPTED);
        save();
    }

    public void refuseComment() throws CommentsException {
        setStatus(CommentsManager.COMMENT_MODERATION_REFUSED);
        save();
    }

    public void remoderateComment() throws CommentsException {
        setStatus(CommentsManager.COMMENT_MODERATION_MODERATED);
        save();
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.comments.Comment#getAsContainer()
     */
    public Container getAsContainer() throws CommentsException {
        return getCommentsManager().getContainer(this, context);
    }


    public void save() throws CommentsException {
        try {
            context.getWiki().saveDocument(getDocument(), context);
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see Comment#remove()
     */
    public boolean remove() throws CommentsException
    {
        try {
            XWikiDocument doc = getDocument();
            if (!doc.removeObject(object)) {
                return false;
            } else {
                // save is needed to remove effectively
                context.getWiki().saveDocument(doc, context);
                return true;
            }
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    private void createObject(Container container, String author, Date date, String content, boolean isreply, Map commentMap) throws CommentsException
    {
        try {
            XWikiDocument doc = getDocument();

            String commentsClassName = getCommentsManager().getCommentsClassName(context);
            BaseObject obj = new BaseObject();
            obj.setClassName(commentsClassName);
            obj.setName(doc.getFullName());
            // read data from map
            context.getWiki().getDocument(commentsClassName, context).getxWikiClass().fromMap(commentMap, obj);
            obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_AUTHOR, author);
            obj.setLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_HIGHLIGHT, "");
            if (isreply) {
                obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PARENT, container.getComment().getCommentId());
            } else {
                obj.setStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_PARENT, "");
            }
            obj.setDateValue(CommentsManager.COMMENT_CLASS_FIELDNAME_DATE, date);
            obj.setLargeStringValue(CommentsManager.COMMENT_CLASS_FIELDNAME_COMMENT, content);
            doc.addObject(commentsClassName, obj);
            // set the internal variable
            object = obj;
        } catch (XWikiException e) {
            throw new CommentsException(e);
        }
    }

    public String toString()
    {
        boolean shouldAddSpace = false;
        StringBuffer sb = new StringBuffer();
        if (getAuthor() != null) {
            sb.append("\nAuthor=").append(getAuthor());
            shouldAddSpace = true;
        }
        if (getDate() != null) {
            sb.append(shouldAddSpace ? " " : "");
            sb.append("\nDate=").append(getDate());
            shouldAddSpace = true;
        }
        if (getContent() != null) {
            sb.append(shouldAddSpace ? " " : "");
            sb.append("\nContent=").append(getContent()).append("\n");
            shouldAddSpace = true;
        }

        return sb.toString();
    }

}
