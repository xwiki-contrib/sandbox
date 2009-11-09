package com.xpn.xwiki.plugin.comments;

import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 17 sept. 2008
 * Time: 14:51:00
 * To change this template use File | Settings | File Templates.
 */
public class CommentApi extends Api {

    protected Comment comment;

    public CommentApi(Comment comment, XWikiContext context) {
       super(context);
        this.comment = comment;
    }

    protected Comment getComment() {
        return comment;
    }

    public CommentApi getParentComment() {
        return new CommentApi(comment.getParentComment(), context);
    }

    public String getGlobalCommentId() {
        return comment.getGlobalCommentId();
    }

    public String getCommentId() {
        return comment.getCommentId();
    }

    public String getAuthor() {
        return comment.getAuthor();
    }

    public String getPage() {
        return comment.getPage();
    }

    public Date getDate() {
        return comment.getDate();
    }

    public String getContent() {
        return comment.getContent();
    }

    public String getStatus() {
        return comment.getStatus();
    }

    public Object get(String propertyName) {
        return comment.get(propertyName);
    }

    public String display(String propertyName, String mode) {
        return comment.display(propertyName, mode, context);
    }

    public List<CommentApi> getComments() {
        try {
            return CommentsPluginApi.wrapComments(comment.getComments(false), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<CommentApi> getComments(boolean asc) {
        try {
            return CommentsPluginApi.wrapComments(comment.getComments(asc), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<CommentApi> getComments(int startLevel, int levelsNumber, boolean asc) {
        try {
            return CommentsPluginApi.wrapComments(comment.getComments(startLevel, levelsNumber, asc), context);
        } catch (CommentsException e) {
            context.put("exception", e);
            return null;
        }
    }

    public boolean isModerated() {
        return "moderated".equals(getStatus());
    }


}
