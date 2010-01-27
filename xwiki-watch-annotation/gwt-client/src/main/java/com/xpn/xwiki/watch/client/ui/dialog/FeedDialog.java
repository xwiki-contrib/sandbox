package com.xpn.xwiki.watch.client.ui.dialog;

import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.data.Feed;
import com.xpn.xwiki.watch.client.data.Group;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software;you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation;either version2.1of
 * the License,or(at your option)any later version.
 * <p/>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software;if not,write to the Free
 * Software Foundation,Inc.,51 Franklin St,Fifth Floor,Boston,MA
 * 02110-1301 USA,or see the FSF site:http://www.fsf.org.
 *
 * @author ldubost
 */

public abstract class FeedDialog extends ValidatingDialog {
    protected Feed feed;
    protected ListBox groupsListBox = new ListBox();
    protected String[] languages;
    
    public static final int VALIDATE_CHANGED_FEEDNAME = 1;

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     */
    public FeedDialog(XWikiGWTApp app, String name, int buttonModes, Feed feed) {
        this(app, name, buttonModes, feed, null);
    }

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     * @languages list of available languages
     */
    public FeedDialog(XWikiGWTApp app, String name, int buttonModes, Feed feed, String[] languages) {
        super(app, name, buttonModes);
        this.languages = languages;
        this.feed = feed;

        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getParametersPanel());
        main.add(getActionsPanel());
        add(main);
    }

    public void onValid(final DialogValidationResponse response)
    {
        //update the feed data
        this.updateFeed();
        // If the response signals a change in the feed name, adjust the feed
        if (response.getCode() == FeedDialog.VALIDATE_CHANGED_FEEDNAME) {
            // Adjust
            String feedName = (String)response.getData();
            this.feed.setName(feedName);
        }
        setCurrentResult(feed);
        if (feed.getPageName().equals("")) {
            ((Watch)app).addFeed(feed, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    // There should already have been an error display
                    ((Watch)app).refreshFeedTree();
                }

                public void onSuccess(Object object) {
                    //FeedDialog.super.onValid(response);
                    FeedDialog.this.endSuperDialog();
                    ((Watch)app).refreshFeedTree();
                    // this will force a reload of feeds on the server
                    ((Watch)app).forceServerLoading();
                }
            });
        } else {
            //FeedDialog.super.onValid(response);
            FeedDialog.this.endSuperDialog();
        }
    }

    protected Widget getGroupsFields() {
        FlowPanel groupsPanel = new FlowPanel();
        Label groupLabel = new Label();
        groupLabel.setStyleName("groups-label");
        groupLabel.setText(app.getTranslation(getDialogTranslationName() + ".groups"));
        groupsPanel.add(groupLabel);
        List currentGroups = feed.getGroups();
        groupsListBox.setMultipleSelect(true);
        Map groupMap = ((Watch)app).getConfig().getGroups();
        Iterator it = groupMap.keySet().iterator();
        while (it.hasNext()) {
            String groupname = (String) it.next();
            String all = ((Watch)app).getTranslation("all");
            if (!groupname.equals(all)) {
                //get group for this key
                Group currentGroup = (Group)groupMap.get(groupname);
                //don't add unless it is a real group
                if (!currentGroup.getPageName().equals("") || currentGroups.contains(groupname)) {
                    String grouptitle = currentGroup.getName();
                    if (groupname.indexOf(".")==-1)
                     grouptitle = "[" + grouptitle + "]";
                    groupsListBox.addItem(grouptitle, groupname);
                    if (currentGroups.contains(groupname)) {
                        groupsListBox.setItemSelected(groupsListBox.getItemCount()-1, true);
                    }
                }
            }
        }
        groupsPanel.add(groupsListBox);
        return groupsPanel;
    }

    protected abstract void updateFeed();
    protected abstract Widget getParametersPanel();

    protected void checkUniqueFeedName(final String feedName, final AsyncCallback cb) {
        //get an unique feed name to use as unique name for feed
        ((Watch)this.app).getXWikiServiceInstance().getUniquePageName(((Watch)this.app).getWatchSpace(), feedName, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                cb.onFailure(throwable);
            }

            public void onSuccess(Object o) {
                //check the response. If it's the feedName we received in parameter, everything is fine
                String receivedPageName = (String)o;
                DialogValidationResponse response = new DialogValidationResponse();
                response.setValid(true);
                if (!receivedPageName.equalsIgnoreCase(feedName)) {
                    response.setCode(FeedDialog.VALIDATE_CHANGED_FEEDNAME);
                    response.setData(receivedPageName);
                }
                cb.onSuccess(response);
            }
        });        
    }
    
    protected void checkUniqueFeedTitle(String feedTitle, final AsyncCallback cb) {
        //check feed title to be unique
        ((Watch)this.app).getDataManager().existsFeedWithTitle(feedTitle, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                cb.onFailure(throwable);
            }

            public void onSuccess(Object o) {
                //check the response
                Boolean checkResponse = (Boolean)o;
                DialogValidationResponse response = new DialogValidationResponse();
                if (checkResponse.booleanValue()) {
                    response.setValid(false);
                    response.setMessage(app.getTranslation(getDialogTranslationName() + ".notuniquename"));
                } else {
                    response.setValid(true);
                }
                cb.onSuccess(response);
            }
        });        
    }
}
