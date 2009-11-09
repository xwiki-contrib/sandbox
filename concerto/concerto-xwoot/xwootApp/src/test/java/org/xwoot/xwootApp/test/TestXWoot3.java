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
package org.xwoot.xwootApp.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.xwoot.contentprovider.MockXWootContentProvider;
import org.xwoot.contentprovider.XWootContentProvider;
import org.xwoot.contentprovider.XWootContentProviderException;
import org.xwoot.contentprovider.XWootContentProviderInterface;
import org.xwoot.contentprovider.XWootId;
import org.xwoot.contentprovider.XWootObject;
import org.xwoot.contentprovider.XWootObjectField;
import org.xwoot.jxta.message.Message;
import org.xwoot.jxta.message.MessageFactory;
import org.xwoot.thomasRuleEngine.core.Value;
import org.xwoot.thomasRuleEngine.op.ThomasRuleOp;
import org.xwoot.wootEngine.Patch;
import org.xwoot.wootEngine.core.ContentId;
import org.xwoot.wootEngine.core.WootId;
import org.xwoot.wootEngine.core.WootRow;
import org.xwoot.wootEngine.op.WootIns;
import org.xwoot.wootEngine.op.WootOp;
import org.xwoot.xwootApp.XWoot3;
import org.xwoot.xwootApp.core.tre.XWootObjectIdentifier;
import org.xwoot.xwootApp.core.tre.XWootObjectValue;

public class TestXWoot3 extends AbstractXWootTest
{
    
    private static final String COMMENT_CONTENT_FIELD_NAME = "comment";
    
    private static final String TEST_SPACE_NAME = "test";
    private static final String CONTENT_FIELD_NAME = "content";
    private static final String AUTHOR_FIELD_NAME = "author";
    private static final String TITLE_FIELD_NAME = "title";
    
    private static final String DEFAULT_AUTHOR_FIELD_VALUE = "Terminator";
    private static final String DEFAULT_TITLE_FIELD_VALUE = "";
    
    private static final String XWIKI_COMMENT_CLASS_NAME = "XWiki.XWikiComments";
    
    private static final String GROUP_NAME = "testGroupName";
    private static final String GROUP_DESCRIPTION = "testGroupDescription";
    private static final char[] GROUP_PASSWORD = "groupPassword".toCharArray();
    private static final char[] KEYSTORE_PASSWORD = "localKeystorePassword".toCharArray();
    
    private static final String PAGE_ID = "test.1";
    private static final String PAGE_ID_FOR_STATE = "test.bogusPage";

    // un XWootObject peut etre de 3 type :
    // … pages
    // … xwiki objets cumulable
    // … xwiki objets non cumulabe

    // Guid :
    // page:PageId
    // object:guid
    // object:page:class[number]

    /**
     * Initialize the content provider by clearing the test space.
     * @param xwiki the {@link XWootContentProvider} instance to initialize.
     */
    private void initContentProvider(XWootContentProviderInterface xwiki)
    {
        if (xwiki instanceof XWootContentProvider) {
            try {
                ((XWootContentProvider) xwiki).getRpc().removeSpace(TEST_SPACE_NAME);
            } catch (XmlRpcException e) {
                System.out.println(xwiki.toString() + " : space + " + TEST_SPACE_NAME + " doesn't exist.");
            }
        }
    }

    /**
     * Create an XWootObject with the specified values. The fields {@link #TITLE_FIELD_NAME} and {@link #AUTHOR_FIELD_NAME} have default values described by {@link #DEFAULT_TITLE_FIELD_VALUE} and {@link #DEFAULT_AUTHOR_FIELD_VALUE}. 
     * 
     * @param pageId the pageId of this xwoot object.
     * @param content the value of the {@link #CONTENT_FIELD_NAME} field.
     * @param version the (major) version.
     * @param minorVersion the minor version.
     * @param newlyCreated if this object is newly created. Allows more control.
     * @return a new instance of XWootObject.
     */
    private XWootObject createObject(String pageId, String content, int version, int minorVersion, boolean newlyCreated)
    {
        XWootObjectField f1 = new XWootObjectField(CONTENT_FIELD_NAME, content, true);
        XWootObjectField f2 = new XWootObjectField(TITLE_FIELD_NAME, DEFAULT_TITLE_FIELD_VALUE, false);
        XWootObjectField f3 = new XWootObjectField(AUTHOR_FIELD_NAME, DEFAULT_AUTHOR_FIELD_VALUE, false);
        
        List<XWootObjectField> fields = new ArrayList<XWootObjectField>();
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        
        XWootObject obj =
            new XWootObject(pageId, Integer.valueOf(version), Integer.valueOf(minorVersion), "page:" + pageId, false, fields,
                newlyCreated);
        return obj;
    }

    private XWootObject simulateXWikiUserModification(XWootContentProviderInterface xwiki, String pageId, String content,
        int version, int minorVersion, boolean isNewlyCreated) throws XmlRpcException
    {

        XWootObject obj = this.createObject(pageId, content, version, minorVersion, isNewlyCreated);

        if (xwiki instanceof XWootContentProvider) {
            XWikiXmlRpcClient rpc = ((XWootContentProvider) xwiki).getRpc();
            XWikiPage page = new XWikiPage();
            page.setId(obj.getPageId());
            page.setContent((String) obj.getFieldValue(CONTENT_FIELD_NAME));
            page.setTitle(DEFAULT_TITLE_FIELD_VALUE);
            page = rpc.storePage(page);
            System.out.println(page);
        } else if (xwiki instanceof MockXWootContentProvider) {
            XWootId id = new XWootId(pageId, 10000, version, minorVersion);
            ((MockXWootContentProvider) xwiki).addEntryInList(id, obj);
        } else {
            return null;
        }
        return obj;
    }
    
    private XWootObject createComment(String pageId, String content, int major, int minor, boolean newlyCreated)
    {
        XWootObjectField f1 = new XWootObjectField(COMMENT_CONTENT_FIELD_NAME, content, true);
        XWootObjectField f2 = new XWootObjectField(AUTHOR_FIELD_NAME, DEFAULT_AUTHOR_FIELD_VALUE, false);
        
        List<XWootObjectField> fields = new ArrayList<XWootObjectField>();
        fields.add(f1);
        fields.add(f2);
        
        XWootObject obj =
            new XWootObject(pageId, Integer.valueOf(major), Integer.valueOf(minor), "object:" + UUID.randomUUID(), true, fields,
                newlyCreated);
        return obj;
    }

    private XWootObject simulateXWikiUserCreateXWikiComment(XWootContentProviderInterface xwiki, String pageId, String commentContent,
        int version, int minorVersion, boolean isNewlyCreated) throws Exception 
        {

        XWootObject obj = this.createComment(pageId, commentContent, version, minorVersion, isNewlyCreated);

        if (xwiki instanceof XWootContentProvider) {
            XWootContentProvider contentProvider = (XWootContentProvider) xwiki;
            XWikiXmlRpcClient rpc = contentProvider.getRpc();
            
            XWikiPage page = null;
            try {
                page = rpc.getPage(pageId);
            } catch (XmlRpcException e) {
                // page does not exist
            }
            
            if (page == null) {
                // We need a pageId to exist in order to create a comment, so create a page.
                page = new XWikiPage();
                page.setId(pageId);
                page.setContent("");
                page = rpc.storePage(page);
                
                // clear the modification generated by the new page. We are interested only in object in a page replication right now.
                contentProvider.getModifiedPagesIds();
                contentProvider.clearAllModifications();
            }
            
            XWikiObject comment = new XWikiObject();
            comment.setClassName(XWIKI_COMMENT_CLASS_NAME);
            comment.setGuid(obj.getGuid().substring(obj.getGuid().indexOf(":") + 1));
            comment.setPageId(pageId);
            comment.setProperty(COMMENT_CONTENT_FIELD_NAME, commentContent);
            comment.setProperty(AUTHOR_FIELD_NAME, obj.getFieldValue(AUTHOR_FIELD_NAME));
            
            comment = rpc.storeObject(comment);
            System.out.println("Simulated and stored xwiki object: " + comment);
        } else if (xwiki instanceof MockXWootContentProvider) {
            XWootId id = new XWootId(pageId, 10000, version, minorVersion);
            ((MockXWootContentProvider) xwiki).addEntryInList(id, obj);
        } else {
            return null;
        }
        
        return obj;
    }

    private void cleanWikis() throws XWootContentProviderException
    { //author : Conan the barbarian 
        this.xwiki21.login("Admin", "admin");
        this.initContentProvider(this.xwiki21);
        this.xwiki21.logout();
        this.xwiki22.login("Admin", "admin");
        this.initContentProvider(this.xwiki22);
        this.xwiki22.logout();
        this.xwiki23.login("Admin", "admin");
        this.initContentProvider(this.xwiki23);
        this.xwiki23.logout();
    }

    @Before
    public void start() throws XWootContentProviderException, InterruptedException
    { //author : Conan the barbarian 
        this.cleanWikis();
        Thread.sleep(100);
    }

    @After
    public void end() throws XWootContentProviderException, InterruptedException
    { //author : Conan the barbarian 
        this.cleanWikis();
        Thread.sleep(100);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithOneXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String content = "titi\n";
        String content2 = "toto\n";
        // connect XWoot to content provider
        this.xwoot31.connectToContentManager();

        XWootContentProviderInterface mxwcp = this.xwoot31.getContentProvider();
        this.initContentProvider(mxwcp);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp.getModifiedPagesIds();
        mxwcp.clearAllModifications();

        // simulate XWiki user page creation
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp, PAGE_ID, content, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(),
            ((XWootObject) this.xwoot31.getTre().getValue(new XWootObjectIdentifier(pageGuid))
                .get())
                    .getGuid());

        // verify wootable field
        Assert.assertEquals(content, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());

        XWootObject xwootObj2 = this.simulateXWikiUserModification(mxwcp, PAGE_ID, content2, 2, 0, false);

        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj2.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(content2, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
    }
    
    /**
     * Add a comment, synch, check if it exists correctly in the model.
     * Then create 2 more comments, synch, check if they are correctly in the model.
     * 
     * Last, check if comment1 is still correctly in the model after the previous comments were added.
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testCommentWithOneXWiki() throws Exception
    {
        String commentContent1 = "A First Comment!\n";
        String commentContent2 = "A Second Comment!\n";
        String commentContent3 = "A third Comment!\n";
        
        // connect XWoot to content provider
        this.xwoot31.connectToContentManager();

        XWootContentProviderInterface mxwcp = this.xwoot31.getContentProvider();
        this.initContentProvider(mxwcp);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp.getModifiedPagesIds();
        mxwcp.clearAllModifications();

        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
        //this.xwiki21.getModifiedPagesIds();
        
        // Add a comment on the page.
        XWootObject comment1 = this.simulateXWikiUserCreateXWikiComment(mxwcp, PAGE_ID, commentContent1, 1, 0, true);
        
        //// Make sure our simulateXWikiUserCreateXWikiComment generates only one change event (the new comment).
        //Assert.assertEquals(1, this.xwoot31.getContentManager().getModifiedPagesIds().size());
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(this.actionsGeneratedModifications(mxwcp, 1, 10));
        
        // synch
        this.xwoot31.synchronize();
        
        // verify no-wootables fields
        Assert.assertEquals(comment1.getGuid(), ((XWootObject) this.xwoot31.getTre()
            .getValue(new XWootObjectIdentifier(comment1.getGuid()))
                .get())
                    .getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent1, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment1.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        
        // Create 2 comments.
        XWootObject comment2 = this.simulateXWikiUserCreateXWikiComment(mxwcp, PAGE_ID, commentContent2, 1, 0, true);
        XWootObject comment3 = this.simulateXWikiUserCreateXWikiComment(mxwcp, PAGE_ID, commentContent3, 1, 0, true);
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(this.actionsGeneratedModifications(mxwcp, 2, 10));
        
        // then synch.
        this.xwoot31.synchronize();
     
        // verify no-wootables fields
        Assert.assertEquals(comment2.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(comment2.getGuid()))
                .get())
                    .getGuid());
        Assert.assertEquals(comment3.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(comment3.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent2, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment2.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        Assert.assertEquals(commentContent3, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment3.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        
        // Is comment1 still there?
        
        // verify no-wootables fields
        Assert.assertEquals(comment1.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(comment1.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent1, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment1.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicWithTwoXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String content = "titi\n";

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation in order to have data for a state.
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, content, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        
        // simulate XWiki user page creation (the real test)
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, content, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(content, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(content, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }
    
//    /**
//     * DOCUMENT ME!
//     * 
//     * @throws Exception DOCUMENT ME!
//     */
//    @Test
//    public void testStateImportWithTwoXWiki() throws Exception
//    {
//        String pageGuid = "page:" + PAGE_ID_FOR_STATE;
//        String content = "titi\n";
//        String overriddenContent = "toto\n";
//
//        // connect XWoot
//        this.xwoot31.reconnectToP2PNetwork();
//        this.xwoot31.connectToContentManager();
//        this.xwoot32.reconnectToP2PNetwork();
//        this.xwoot32.connectToContentManager();
//
//        // connect sites
//        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
//        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
//        
//        // check connections
//        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
//        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
//        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());
//
//        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentManager();
//        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentManager();
//        
//        this.initContentProvider(mxwcp1);
//        this.initContentProvider(mxwcp2);
//        
//        // Ignore the rest of the pages. They are not our objective.
//        mxwcp1.getModifiedPagesIds();
//        mxwcp1.clearAllModifications();
//        mxwcp2.getModifiedPagesIds();
//        mxwcp2.clearAllModifications();
//
//        // simulate XWiki user page creation in order to have data for a state.
//        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, content, 1, 0, true);
//        // simulate XWiki user page creation on the other wiki with different content that should be overridden by the state import.
//        XWootObject xwootObj2 = this.simulateXWikiUserModification(mxwcp2, PAGE_ID_FOR_STATE, overriddenContent, 1, 0, true);
//
//        // synchronize xwoot
//        this.xwoot31.synchronize();
//        
//        // Compute and import a state from the current data.
//        this.xwoot31.computeState();
//        File receivedTemporaryState = this.xwoot32.askStateToGroup();
//        this.xwoot32.importState(receivedTemporaryState);
//        receivedTemporaryState.delete();
//
//        // verify no-wootables fields
//        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
//            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
//        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
//            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
//
//        // verify wootable field
//        Assert.assertEquals(content, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID_FOR_STATE, pageGuid,
//            "content"));
//        Assert.assertEquals(content, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID_FOR_STATE, pageGuid,
//            "content"));
//
//        Assert.assertEquals(0, this.xwoot31.getContentManager().getModifiedPagesIds().size());
//    }
    
    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testGetLostMessagesWithTwoXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String bodusPageGuid = "page:" + PAGE_ID_FOR_STATE;
        String contentFroBogusPage = "bogusContent\n";
        String message1ReceivedByAll = "Line1\n";
        String message2ReceivedByAll = "Line2\n";
        String initialCommonContent = message1ReceivedByAll + message2ReceivedByAll;
        String message3Lost = "Line3\n";
        String desiredCommonContent = initialCommonContent + message3Lost;

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation in order to have data for a state.
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, contentFroBogusPage, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(bodusPageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(bodusPageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentFroBogusPage, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID_FOR_STATE, bodusPageGuid,
            "content"));
        Assert.assertEquals(contentFroBogusPage, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID_FOR_STATE, bodusPageGuid,
            "content"));
        
        
        
        // simulate adding a line on the first wiki and receiving it by all.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, message1ReceivedByAll, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(message1ReceivedByAll, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(message1ReceivedByAll, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        
        // simulate adding a line on the second wiki and receiving it by all. This brings us to an initial common content of 2 messages.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, initialCommonContent, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(initialCommonContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(initialCommonContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        

        // Peer 2 goes offline.
        this.xwoot32.disconnectFromP2PNetwork();
        
        
        
        // simulate adding another on the first wiki and the second peer not receiving this.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, desiredCommonContent, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(desiredCommonContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(initialCommonContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        // Peer2 comes back online.
        this.xwoot32.reconnectToP2PNetwork();
        // It joins the group
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        // Does an anti-entropy with all the group members. (mock implementation does it synchronously, real implementation does it asynchronously)
        this.xwoot32.doAntiEntropyWithAllNeighbors();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(desiredCommonContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(desiredCommonContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        
        

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }
    
    
    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testOfflineWorkWithTwoXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String bodusPageGuid = "page:" + PAGE_ID_FOR_STATE;
        String contentFroBogusPage = "bogusContent\n";
        String message1ReceivedByAll = "Line1\n";
        String message2ReceivedByAll = "Line2\n";
        String initialCommonContent = message1ReceivedByAll + message2ReceivedByAll;
        String message3Lost = "Line3\n";
        String desiredCommonContent = initialCommonContent + message3Lost;

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation in order to have data for a state.
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, contentFroBogusPage, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(bodusPageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(bodusPageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentFroBogusPage, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID_FOR_STATE, bodusPageGuid,
            "content"));
        Assert.assertEquals(contentFroBogusPage, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID_FOR_STATE, bodusPageGuid,
            "content"));
        
        
        
        // simulate adding a line on the first wiki and receiving it by all.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, message1ReceivedByAll, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(message1ReceivedByAll, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(message1ReceivedByAll, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        
        // simulate adding a line on the second wiki and receiving it by all. This brings us to an initial common content of 2 messages.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, initialCommonContent, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(initialCommonContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(initialCommonContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        

        // Peer 2 goes offline.
        this.xwoot32.disconnectFromP2PNetwork();
        
        // simulate adding another on the second (offline) wiki and the first peer not receiving this.
        xwootObj = this.simulateXWikiUserModification(mxwcp2, PAGE_ID, desiredCommonContent, 1, 0, true);

        // synchronize xwoot
        this.xwoot32.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(initialCommonContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(desiredCommonContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        // Peer2 comes back online.
        this.xwoot32.reconnectToP2PNetwork();
        // It joins the group
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        // Does an anti-entropy with all the group members. (mock implementation does it synchronously, real implementation does it asynchronously)
        this.xwoot32.doAntiEntropyWithAllNeighbors();
        
        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(desiredCommonContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(desiredCommonContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        
        

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }
    
    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testModifyingExistingContentWithTwoXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String existingLine1 = "Exsiting Line1\n";
        String existingLine2 = "Exsiting Line2\n";
        String existingLine3 = "Exsiting Line3\n";
        String existingContent = existingLine1 + existingLine2 + existingLine3;
        String contentToAddAtEnd = "Added Line3'\n";
        String contentAddedAtEnd = existingContent + contentToAddAtEnd;
        String contentToAddAtMiddle = "Added Line2'\n";
        String contentAddedAtMiddle = existingLine1 + existingLine2 + contentToAddAtMiddle + existingLine3 + contentToAddAtEnd;
        String contentToAddAtBegin = "Added Line1'\n";
        String contentAddedAtBegin = contentToAddAtBegin + existingLine1 + existingLine2 + contentToAddAtMiddle + existingLine3 + contentToAddAtEnd;
        String contentDeletedAtEnd = contentToAddAtBegin + existingLine1 + existingLine2 + contentToAddAtMiddle + existingLine3;
        String contentDeletedAtMiddle = contentToAddAtBegin + existingLine1 + existingLine2 + existingLine3;
        String contentDeletedAtBegin = existingContent;
        String contentDeletedAll = "";

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // Populate the first wiki with the existing data.
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, existingContent, 1, 0, true);
        
        // synchronize xwoot
        this.xwoot31.synchronize(false);
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        // verify non-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        
        // verify wootable field
        Assert.assertEquals(existingContent, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(existingContent, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        // simulate XWiki user adding a line at the end of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentAddedAtEnd, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentAddedAtEnd, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentAddedAtEnd, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));

        
        
        
        // simulate XWiki user adding a line at the middle of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentAddedAtMiddle, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentAddedAtMiddle, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentAddedAtMiddle, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        
        // simulate XWiki user adding a line at the the beginning of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentAddedAtBegin, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentAddedAtBegin, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentAddedAtBegin, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        // simulate XWiki user deleting a line at the the end of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentDeletedAtEnd, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentDeletedAtEnd, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentDeletedAtEnd, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        // simulate XWiki user deleting a line at the the middle of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentDeletedAtMiddle, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentDeletedAtMiddle, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentDeletedAtMiddle, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        
        // simulate XWiki user deleting a line at the the beginning of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentDeletedAtBegin, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentDeletedAtBegin, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentDeletedAtBegin, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        // simulate XWiki user deleting a line at the the beginning of the existing content.
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, contentDeletedAll, 1, 0, true);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(contentDeletedAll, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(contentDeletedAll, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        
        
        
        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }
    
    /**
     * Make sure that your previous actions generated modifications in the contentProvider.
     * </p>
     * This is a workaround for the bug in the content provider.
     * @FIXME: not working.
     * 
     * @param xwiki the content provider to test.
     * @param numberOfExpectedModifications
     * @return true if the expectedNumberOfModifications are returned by the contentProvider.
     * @throws XWootContentProviderException if problems occur.
     */
    /*private boolean actionsGeneratedModifications(XWootContentProviderInterface xwiki, int numberOfExpectedModifications, int numberOfTries) throws XWootContentProviderException {
        boolean modificationsGenerated = false;
        for (int tries = 0; tries < numberOfTries; tries++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            
            int actualModifications = xwiki.getModifiedPagesIds().size();
            System.out.println("Try " + tries + " : " + actualModifications + "/" + numberOfExpectedModifications);
            if (actualModifications == numberOfExpectedModifications) {
                modificationsGenerated = true;
                System.out.println("Success.");
                break;
            }
        }
        
        return modificationsGenerated;
    }*/
    
    /**
     * Create a comment on xwiki1, then synchronize. Create 2 more comments on the xwiki2 then synch.
     * Result:
     * Both xwikis will have the same comments.
     * 
     * @throws if problems occur.
     */
    @Test
    public void testCommentsWithTwoXWiki() throws Exception
    {
        String commentContent1 = "A First Comment!\n";
        String commentContent2 = "A Second Comment!\n";
        String commentContent3 = "A third Comment!\n";

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        /*this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);*/

        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();
        
        // simulate XWiki user create comment on xw1 in order to have data for a state.
        XWootObject comment1XW1 = this.simulateXWikiUserCreateXWikiComment(this.xwiki21, PAGE_ID_FOR_STATE, commentContent1, 1, 0, true);
        
        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
        //this.xwiki21.getModifiedPagesIds();
        
        // Make sure our simulateXWikiUserCreateXWikiComment generates only one change event (the new comment).
        //Assert.assertEquals(1, this.xwiki21.getModifiedPagesIds().size());
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(actionsGeneratedModifications(this.xwiki21, 1, 10));

        // synchronize xw1
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        // simulate XWiki user create comment on xw1. (the real test data)
        comment1XW1 = this.simulateXWikiUserCreateXWikiComment(this.xwiki21, PAGE_ID, commentContent1, 1, 0, true);
        
        // synchronize xw1
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(comment1XW1.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(comment1XW1.getGuid())).get()).getGuid());
        Assert.assertEquals(comment1XW1.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(comment1XW1.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent1, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment1XW1.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        Assert.assertEquals(commentContent1, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, comment1XW1.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
        
        
        // Create 2 more comments on xw2.
        XWootObject comment2XW2 = this.simulateXWikiUserCreateXWikiComment(this.xwiki22, PAGE_ID, commentContent2, 1, 0, true);
        XWootObject comment3XW2 = this.simulateXWikiUserCreateXWikiComment(this.xwiki22, PAGE_ID, commentContent3, 1, 0, true);
        
        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
       // this.xwiki22.getModifiedPagesIds();
        // FIXME: this should be a workaround for NewXWootContentProvider.getModifiedPagesIds not returning any modification on first call.
       // this.xwiki22.getModifiedPagesIds();
        
        // Make sure our simulateXWikiUserCreateXWikiComment generates only two change event (the 2 new comments).
       // Assert.assertEquals(2, this.xwiki22.getModifiedPagesIds().size());
        
        // If after several tries the content manager still does not report the modification, then there is a problem.
        //Assert.assertTrue(actionsGeneratedModifications(this.xwiki22, 2, 10));
        
        // synch xw2
        this.xwoot32.synchronize();
        
        // verify no-wootables fields
        Assert.assertEquals(comment2XW2.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(comment2XW2.getGuid())).get()).getGuid());
        Assert.assertEquals(comment2XW2.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(comment2XW2.getGuid())).get()).getGuid());
        
        Assert.assertEquals(comment3XW2.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(comment3XW2.getGuid())).get()).getGuid());
        Assert.assertEquals(comment3XW2.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(comment3XW2.getGuid())).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(commentContent2, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, comment2XW2.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        Assert.assertEquals(commentContent2, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment2XW2.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        
        Assert.assertEquals(commentContent3, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, comment3XW2.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));
        Assert.assertEquals(commentContent3, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, comment3XW2.getGuid(),
            COMMENT_CONTENT_FIELD_NAME));

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testBasicConflictBetweenTwoXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String content = "titi\n";
        String result = "titi\ntiti\n";

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // simulate XWiki user page creation in order to have data for a state.
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, content, 1, 0, true);
        XWootObject xwootObj2 = this.simulateXWikiUserModification(mxwcp2, PAGE_ID_FOR_STATE, content, 1, 0, true);
        Assert.assertEquals(xwootObj, xwootObj2);

        // synchronize xwoot
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        // simulate XWiki user page creation (real test data)
        xwootObj = this.simulateXWikiUserModification(mxwcp1, PAGE_ID, content, 1, 0, true);
        xwootObj2 = this.simulateXWikiUserModification(mxwcp2, PAGE_ID, content, 1, 0, true);
        Assert.assertEquals(xwootObj, xwootObj2);

        // synchronize xwoot
        this.xwoot31.synchronize();

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot32.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(result, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));
        Assert.assertEquals(result, this.xwoot32.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            "content"));

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testConflitBetweenVueAndModel() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String content = "toto";
        String content2 = "titi";
        String result = "titi\ntoto\n";

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        
        XWootContentProviderInterface mxwcp = this.xwoot31.getContentProvider();
        
        this.initContentProvider(mxwcp);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp.getModifiedPagesIds();
        mxwcp.clearAllModifications();

        // simulate XWiki user page creation to create content for a state.
        XWootObject xwootObj = this.simulateXWikiUserModification(mxwcp, PAGE_ID_FOR_STATE, content, 1, 0, true);
        
        // synchronize xwoot
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        
        // simulate XWiki user page creation (real test)
        xwootObj = this.simulateXWikiUserModification(mxwcp, PAGE_ID, content, 1, 0, true);

        // create patch to change wootEngine model : insert "titi" in first
        // position
        // add wootable content in patch
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootId wootId = new WootId(String.valueOf(0), 0);
        WootIns op0 = new WootIns(new WootRow(wootId, content2), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(PAGE_ID, pageGuid, CONTENT_FIELD_NAME, false));
        op0.setOpId(wootId);
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId(PAGE_ID);
        patch.setObjectId(pageGuid);
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        // patch must contain corresponding TRE op to have the xwootObject
        // add no wootable content in patch
        XWootObject obj2 = this.createObject(PAGE_ID, content2, 1, 0, true);
        Value tre_val = new XWootObjectValue();
        ((XWootObjectValue) tre_val).setObject(obj2);
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier(obj2.getGuid());
        ThomasRuleOp tre_op = this.xwoot31.getTre().getOp(tre_id, tre_val);
        List<Object> tre_ops = new ArrayList<Object>();
        tre_ops.add(tre_op);
        patch.setMDelements(tre_ops);

        Message mess = MessageFactory.createMessage("test_Peer", patch, Message.Action.BROADCAST_PATCH);

        this.xwoot31.receiveMessage(mess);

        // verify no-wootables fields
        Assert.assertEquals(xwootObj.getGuid(), ((XWootObject) this.xwoot31.getTre().getValue(
            new XWootObjectIdentifier(pageGuid)).get()).getGuid());

        // verify wootable field
        Assert.assertEquals(result, this.xwoot31.getWootEngine().getContentManager().getContent(PAGE_ID, pageGuid,
            CONTENT_FIELD_NAME));

        Assert.assertEquals(0, this.xwoot31.getContentProvider().getModifiedPagesIds().size());
    }

    // /**
    // * DOCUMENT ME!
    // *
    // * @throws Exception DOCUMENT ME!
    // */
    // @Test
    // public void testConflitBetweenVueAndModel() throws Exception
    // {
    // // connect XWoot to content provider
    // this.xwoot31.reconnectToP2PNetwork();
    // this.xwoot31.connectToContentManager();
    //
    // String pageName="test.1";
    //
    // // simulate XWiki user page creation
    // XWootContentProviderInterface mxwcp = this.xwoot31.getContentManager();
    // XWootId id = new XWootId(pageName, 10, 1, 0);
    // XWootObject obj1 = this.createObject(pageName,"titi",1,0,true);
    // this.simulateXWikiUserModification(mxwcp, id, obj1);
    //
    // // create patch to change wootEngine model : insert "titi" in first
    // // position
    // Patch patch = new Patch();
    // List<WootOp> vector = new ArrayList<WootOp>();
    // WootIns op0 = new WootIns(new WootRow(new WootId(0, 0), "toto"), new WootId(-1, -1), new WootId(-2, -2));
    // op0.setContentId(new ContentId(pageName, "page:test.1", "content", false));
    // op0.setOpId(new WootId(0, 0));
    // vector.add(op0);
    // patch.setData(vector);
    // patch.setPageId(pageName);
    // patch.setObjectId("page:test.1");
    // patch.setTimestamp(10);
    // patch.setVersion(0);
    // patch.setMinorVersion(1);
    //
    // // patch must contain corresponding TRE op to have the xwootObject
    //
    // XWootObject obj2 = this.createObject(pageName,"toto",0,1,true);
    // Value tre_val = new XWootObjectValue();
    // ((XWootObjectValue) tre_val).setObject(obj2);
    // XWootObjectIdentifier tre_id = new XWootObjectIdentifier(obj2.getGuid());
    // ThomasRuleOp tre_op = this.xwoot31.getTre().getOp(tre_id, tre_val);
    // List tre_ops = new ArrayList<ThomasRuleOp>();
    // tre_ops.add(tre_op);
    // patch.setMDelements(tre_ops);
    //
    // Message mess = new Message();
    // mess.setAction(LpbCastAPI.LOG_AND_GOSSIP_OBJECT);
    // mess.setContent(patch);
    // mess.setOriginalPeerId("test_Peer");
    // mess.setRound(1);
    //
    // this.xwoot31.receivePatch(mess);
    //
    // Assert.assertEquals("toto\ntiti\n", this.xwoot31.getWootEngine().getContentManager().getContent(pageName,
    // "page:test.1", "content"));
    // }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithTwoConcurrentXWiki() throws Exception
    {
        String pageGuid = "page:" + PAGE_ID;
        String content1 = "Ligne1 sur XWiki1\n";
        String content2 = "Ligne1 sur XWiki2\n";
        String content3 = "Ligne 1 sur xwiki fantôme";
        String result1 = "Ligne 1 sur xwiki fantôme\nLigne1 sur XWiki1\nLigne1 sur XWiki2\n";
        String content4 = "Nouvelle ligne sur xwiki1\nLigne1 sur XWiki1\n";
        String content5 = "Ligne1 sur XWiki1\nNouvelle ligne sur xwiki2\n";
        String result2 = "Nouvelle ligne sur xwiki1\nLigne1 sur XWiki1\nNouvelle ligne sur xwiki2\n";

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();

        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());

        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();

        // Create data for a state and synchronize.
        this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, content1, 1, 0, true);
        xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        
        // simulate XWiki user page creation (real test)
        this.simulateXWikiUserModification(mxwcp1, PAGE_ID, content1, 1, 0, true);
        this.simulateXWikiUserModification(mxwcp2, PAGE_ID, content2, 1, 0, true);

        // create patch to change wootEngine model : insert "Ligne 1 sur xwiki fantôme" in first
        // position
        Patch patch = new Patch();
        List<WootOp> vector = new ArrayList<WootOp>();
        WootId wootId = new WootId(String.valueOf(0), 0);
        WootIns op0 = new WootIns(new WootRow(wootId, content3), WootId.FIRST_WOOT_ID, WootId.LAST_WOOT_ID);
        op0.setContentId(new ContentId(PAGE_ID, pageGuid, "content", false));
        op0.setOpId(wootId);
        vector.add(op0);
        patch.setData(vector);
        patch.setPageId(PAGE_ID);
        patch.setObjectId(pageGuid);
        patch.setTimestamp(10);
        patch.setVersion(1);
        patch.setMinorVersion(0);

        // patch must contain corresponding TRE op to have the xwootObject

        XWootObject obj3 =
            this.createObject(PAGE_ID, "Cette valeur est ecrasée par le contenu du wootEngine\n", 1, 0, true);
        Value tre_val = new XWootObjectValue();
        ((XWootObjectValue) tre_val).setObject(obj3);
        XWootObjectIdentifier tre_id = new XWootObjectIdentifier(obj3.getGuid());
        ThomasRuleOp tre_op = this.xwoot31.getTre().getOp(tre_id, tre_val);
        List<Object> tre_ops = new ArrayList<Object>();
        tre_ops.add(tre_op);
        patch.setMDelements(tre_ops);
        
        Message mess = MessageFactory.createMessage("test_peer", patch, Message.Action.BROADCAST_PATCH);

        this.xwoot31.receiveMessage(mess);
        this.xwoot32.receiveMessage(mess);

        Assert.assertEquals(result1, this.wootEngine1.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME),
            this.wootEngine2.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");

        this.simulateXWikiUserModification(mxwcp1, PAGE_ID, content4, 2, 0, false);
        this.simulateXWikiUserModification(mxwcp2, PAGE_ID, content5, 2, 0, false);

        this.xwoot31.synchronize();

        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");

        Assert.assertEquals(result2, this.wootEngine1.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME),
            this.wootEngine2.getContentManager().getContent(PAGE_ID, pageGuid, CONTENT_FIELD_NAME));
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception DOCUMENT ME!
     */
    @Test
    public void testWithThreeConcurrentXWiki() throws Exception
    {
        String pageId = "test.final";
        String pageGuid = "page:" + pageId;
        String content1 = "Ligne 1 sur xwiki1\n";
        String content2 = "Ligne -1 sur xwiki1\nLigne 0 sur xwiki1\nLigne 1 sur xwiki1\n";
        String content3 = "Ligne 0 sur xwiki2\nLigne 1 sur xwiki1\nLigne 2 sur xwiki2\n";
        String content4 = "Ligne 1 sur xwiki1\nLigne 2 sur xwiki3\nLigne 3 sur xwiki3\n";
        String result =
            "Ligne -1 sur xwiki1\n" + "Ligne 0 sur xwiki1\n" + "Ligne 0 sur xwiki2\n" + "Ligne 1 sur xwiki1\n"
                + "Ligne 2 sur xwiki2\n" + "Ligne 2 sur xwiki3\n" + "Ligne 3 sur xwiki3\n";

        // configure neighbors
//        this.lpbCast1.addNeighbor(this.xwoot31, this.xwoot32);
//        this.lpbCast1.addNeighbor(this.xwoot31, this.xwoot23);
//        this.lpbCast2.addNeighbor(this.xwoot32, this.xwoot31);
//        this.lpbCast2.addNeighbor(this.xwoot32, this.xwoot23);
//        this.lpbCast3.addNeighbor(this.xwoot23, this.xwoot31);
//        this.lpbCast3.addNeighbor(this.xwoot23, this.xwoot32);

        // connect XWoot
        this.xwoot31.reconnectToP2PNetwork();
        this.xwoot31.connectToContentManager();
        this.xwoot32.reconnectToP2PNetwork();
        this.xwoot32.connectToContentManager();
        this.xwoot33.reconnectToP2PNetwork();
        this.xwoot33.connectToContentManager();
        
        // connect sites
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        this.xwoot33.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connections
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot33.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());
        Assert.assertTrue(this.xwoot33.getPeer().isConnectedToGroupRendezVous());

        // /////////////////////
        // Scenario execution
        // /////////////////////
        // simulate a change from wikiContentManager user...

        // simulate XWiki user page creation
        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();
        XWootContentProviderInterface mxwcp3 = this.xwoot33.getContentProvider();
        this.initContentProvider(mxwcp1);
        this.initContentProvider(mxwcp2);
        this.initContentProvider(mxwcp3);
        
        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();
        mxwcp3.getModifiedPagesIds();
        mxwcp3.clearAllModifications();
        
        // create data for initial state.
        this.simulateXWikiUserModification(mxwcp1, PAGE_ID_FOR_STATE, content1, 1, 0, true);
        
        // synchronize xwoot
        this.xwoot31.synchronize();
        
        // Compute and import a state from the current data.
        this.xwoot31.computeState();
        File receivedTemporaryState = this.xwoot32.askStateToGroup();
        this.xwoot32.importState(receivedTemporaryState);
        receivedTemporaryState.delete();
        File receivedTemporaryState2 = this.xwoot33.askStateToGroup();
        this.xwoot33.importState(receivedTemporaryState2);
        receivedTemporaryState2.delete();
        
        
        // Real test
        this.simulateXWikiUserModification(mxwcp1, pageId, content1, 1, 0, true);

        // Launch the synch...
        this.xwoot31.synchronize();

        Assert.assertEquals(content1, this.wootEngine1.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));

        Assert.assertEquals(content1, this.wootEngine2.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));

        Assert.assertEquals(content1, this.wootEngine3.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));

        this.simulateXWikiUserModification(mxwcp1, pageId, content2, 2, 0, false);
        this.simulateXWikiUserModification(mxwcp2, pageId, content3, 2, 0, false);
        this.simulateXWikiUserModification(mxwcp3, pageId, content4, 2, 0, false);

        // Launch the synch...
        this.xwoot31.synchronize();
        System.out.println("woot1 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");
        System.out.println("woot2 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine2.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");
        System.out.println("woot3 : ");
        System.out.println("-------------------");
        System.out.println(this.wootEngine3.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));
        System.out.println("-------------------");
        Assert.assertEquals(result, this.wootEngine1.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));
        Assert.assertEquals(this.wootEngine1.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME),
            this.wootEngine2.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));
        Assert.assertEquals(this.wootEngine2.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME),
            this.wootEngine3.getContentManager().getContent(pageId, pageGuid, CONTENT_FIELD_NAME));

    }

    /* FIXME: This causes problems when root user runs tests however we set rights on paths.
    @Test(expected = XWootException.class)
    public void testXWoot1() throws Exception
    {
        this.xwoot31 =
            new XWoot3(this.xwiki21, this.wootEngine1, this.lpbCast1, "/cantBecreated" + File.separator + "Site1",
                "Site1", new Integer(1), this.tre1, this.ae1);
    }*/

    @Test(expected = RuntimeException.class)
    public void testXWoot2() throws Exception
    {
        File f = new File(WORKINGDIR + File.separatorChar + "file.tmp");
        f.delete();
        Assert.assertFalse(f.exists());
        f.createNewFile();
        Assert.assertTrue(f.exists());
        this.xwoot31 =
            new XWoot3(this.xwiki21, this.wootEngine1, this.peer1, f.toString(), this.tre1, this.ae1, null, null);
    }

    /* FIXME: same root-user-running-tests problem.
    @Test(expected = XWootException.class)
    public void testXWoot3() throws Exception
    {
        File f = new File(WORKINGDIR + File.separatorChar + "folder");
        f.delete();
        Assert.assertFalse(f.exists());
        f.mkdir();
        Assert.assertTrue(f.exists());
        f.setReadOnly();
        Assert.assertFalse(f.canWrite());
        this.xwoot31 =
            new XWoot3(this.xwiki21, this.wootEngine1, this.peer1, f.toString(), this.tre1, this.ae1);
    }*/

    /**
     * Pass a connected peer to the XWoot constructor.
     * <p>
     * Result: The peer will get disconnected and the XWoot object will be instantiated propperly.
     */
    @Test
    public void testXWoot4() throws Exception
    {
        File f = new File(WORKINGDIR);
        f.mkdir();
        Assert.assertTrue(f.exists());
        Assert.assertFalse(this.peer1.isConnectedToNetwork());
        
        this.peer1.startNetworkAndConnect(null, null);
        Assert.assertTrue(this.peer1.isConnectedToNetwork());
        
        this.xwoot31 =
            new XWoot3(this.xwiki21, this.wootEngine1, this.peer1, f.toString(), this.tre1, this.ae1, null, null);
        
        Assert.assertNotNull(this.xwoot31);
    }

    /**
     * FIXME: Will fail because of reconnectToP2PNetwork not fully implemented?
     * 
     * @throws Exception if problems occur.
     */
    @Test
    public void testConnection() throws Exception
    {
        // receiver = p2pNetwork
        Assert.assertFalse(this.xwoot31.isConnectedToP2PNetwork());
        
        this.xwoot31.reconnectToP2PNetwork();
        Assert.assertTrue(this.xwoot31.isConnectedToP2PNetwork());
        
        this.xwoot31.disconnectFromP2PNetwork();
        Assert.assertFalse(this.xwoot31.isConnectedToP2PNetwork());
        
        this.xwoot31.reconnectToP2PNetwork();
        Assert.assertTrue(this.xwoot31.isConnectedToP2PNetwork());
        
        this.xwoot31.reconnectToP2PNetwork();
        Assert.assertTrue(this.xwoot31.isConnectedToP2PNetwork());

        
        // content provider
        Assert.assertFalse(this.xwoot31.isContentManagerConnected());
        
        this.xwoot31.connectToContentManager();
        Assert.assertTrue(this.xwoot31.isContentManagerConnected());
        
        this.xwoot31.disconnectFromContentManager();
        Assert.assertFalse(this.xwoot31.isContentManagerConnected());
    }

    // @Test
    // public void testComputeState() {
    // fail("Not yet implemented");
    // }

    // @Test
    // public void testInitialiseWootStorage() {
    // fail("Not yet implemented");
    // }

    @Test
    public void testJoinNetwork()
    {
        // void
    }

    //
    // @Test
    // public void testReceive() {
    // fail("Not yet implemented");
    // }

    //
    // @Test
    // public void testRemoveNeighbour() {
    // fail("Not yet implemented");
    // }

    // With Mock it's ok but with real XWiki Content Provider this test takes a few
    // minutes ...
    @Test
    public void testState() throws Exception
    {
        String pageId1 = "test.1";
        String pageGuid1 = "page:" + pageId1;
        String content1 = "toto";

        String pageId2 = "test.2";
        String pageGuid2 = "page:" + pageId2;
        String content2 = "titi";

        String pageId3 = "test.3";
        String pageGuid3 = "page:" + pageId3;
        String content3 = "tata";

        this.xwoot31.connectToContentManager();
        this.xwoot32.connectToContentManager();
        XWootContentProviderInterface mxwcp1 = this.xwoot31.getContentProvider();
        XWootContentProviderInterface mxwcp2 = this.xwoot32.getContentProvider();

        // Ignore the rest of the pages. They are not our objective.
        mxwcp1.getModifiedPagesIds();
        mxwcp1.clearAllModifications();
        mxwcp2.getModifiedPagesIds();
        mxwcp2.clearAllModifications();
        
        // simulate XWiki user page creation
        this.simulateXWikiUserModification(mxwcp1, pageId1, content1, 1, 0, true);
        this.simulateXWikiUserModification(mxwcp1, pageId2, content2, 1, 0, true);
        this.simulateXWikiUserModification(mxwcp1, pageId3, content3, 1, 0, true);

        this.xwoot31.joinNetwork(null);
        this.xwoot31.createNewGroup(GROUP_NAME, GROUP_DESCRIPTION, KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connection
        Assert.assertTrue(this.xwoot31.getPeer().isConnectedToGroup());
        
        this.xwoot31.synchronize();

        Assert.assertEquals(3, this.xwoot31.getWootEngine().getContentManager().listPages().length);
        Assert.assertEquals("toto\n", this.xwoot31.getWootEngine().getContentManager().getContent(pageId1, pageGuid1,
            "content"));
        Assert.assertEquals("titi\n", this.xwoot31.getWootEngine().getContentManager().getContent(pageId2, pageGuid2,
            "content"));
        Assert.assertEquals("tata\n", this.xwoot31.getWootEngine().getContentManager().getContent(pageId3, pageGuid3,
            "content"));

        File f = this.xwoot31.computeState();
        Assert.assertNotNull(f);
        
        // create connection
        this.xwoot32.joinNetwork(null);
        this.xwoot32.joinGroup(this.xwoot31.getPeer().getCurrentJoinedPeerGroup().getPeerGroupAdvertisement(), KEYSTORE_PASSWORD, GROUP_PASSWORD);
        
        // check connection
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroup());
        Assert.assertTrue(this.xwoot32.getPeer().isConnectedToGroupRendezVous());
        
        this.xwoot32.importState(f);
        this.xwoot32.connectToContentManager();
        
        Assert.assertEquals(3, this.xwoot32.getWootEngine().getContentManager().listPages().length);
        Assert.assertEquals("toto\n", this.xwoot32.getWootEngine().getContentManager().getContent(pageId1, pageGuid1,
            "content"));
        Assert.assertEquals("titi\n", this.xwoot32.getWootEngine().getContentManager().getContent(pageId2, pageGuid2,
            "content"));
        Assert.assertEquals("tata\n", this.xwoot32.getWootEngine().getContentManager().getContent(pageId3, pageGuid3,
            "content"));

    }
}
