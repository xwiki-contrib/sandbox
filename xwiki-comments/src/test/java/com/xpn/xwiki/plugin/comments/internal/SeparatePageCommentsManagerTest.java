package com.xpn.xwiki.plugin.comments.internal;

import java.util.List;

import com.xpn.xwiki.plugin.comments.Comment;
import com.xpn.xwiki.plugin.comments.CommentsException;
import com.xpn.xwiki.plugin.comments.Container;

public class SeparatePageCommentsManagerTest extends AbstractCommentsManagerTest
{

    protected void setUp() throws Exception
    {
        super.setUp();
        commentsmanager = new SeparatePageCommentsManager();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public final void testSeparatePageCommentsManager()
    {
        
    }

    public final void testGetCommentFromDocument()
    {
        
    }
    
    public void testAddComment() throws CommentsException
    {
        Container container=new DefaultContainer(context);
        List<Comment> comments = commentsmanager.getComments(container, 0, 0, false, context);
        int comNo = comments.size();
        comments = commentsmanager.getComments(container, 0, 0, false, context);
        //assertTrue("addComment methos failed",comNo + 1 == comments.size());
    }
    
    public void testRemoveComment()
    {
        
    }
}
