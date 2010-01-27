package com.xpn.xwiki.watch.client.ui.dialog;

import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.watch.client.data.Feed;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;
import java.util.ArrayList;

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

public class StandardFeedDialog extends FeedDialog {
    protected TextBox feedNameTextBox;
    protected TextBox feedURLTextBox;

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     */
    public StandardFeedDialog(XWikiGWTApp app, String name, int buttonModes, Feed feed) {
        super(app, name, buttonModes, feed);
    }

    protected void updateFeed()
    {
        // If this is a new feed, set the name, if it's an existing feed, edit its title
        if (feed.getPageName().trim().length() > 0) {
            feed.setTitle(feedNameTextBox.getText().trim());
        } else {
            feed.setName(feedNameTextBox.getText().trim());
            feed.setTitle(feedNameTextBox.getText().trim());
        }
        feed.setUrl(feedURLTextBox.getText().trim());
        List groups = new ArrayList();
        for (int i=0;i<groupsListBox.getItemCount();i++) {
            if (groupsListBox.isItemSelected(i))
             groups.add(groupsListBox.getValue(i));
        }
        feed.setGroups(groups);
    }

    protected void validateDialogData(final AsyncCallback cb)
    {
        // Prepare the response
        final DialogValidationResponse response = new DialogValidationResponse();
        final String feedName = feedNameTextBox.getText().trim();
        if (feedURLTextBox.getText().equals("")) {
            response.setValid(false);
            response.setMessage(app.getTranslation(getDialogTranslationName() + ".nofeedurl"));
            cb.onSuccess(response);
            return;
        }

        if (feedName.equals("")) {
            response.setValid(false);
            response.setMessage(app.getTranslation(getDialogTranslationName() + ".nofeedname"));
            cb.onSuccess(response);
            return;
        }

        // Validate feedname only if we are using it
        boolean validatingFeedName = (this.feed.getPageName().trim().length() == 0);
        String feedTitle = (this.feed.getTitle().trim().length() > 0 ? this.feed.getTitle() : this.feed.getName());
        if ((validatingFeedName && this.feed.getName().trim().equalsIgnoreCase(feedName)) 
                || (!validatingFeedName && feedTitle.trim().equalsIgnoreCase(feedName))) {
            response.setValid(true);
            cb.onSuccess(response);
            return;
        } else { 
            if (validatingFeedName) {
                // Validate feed name only if the feed title is valid 
                this.checkUniqueFeedTitle(feedName, new AsyncCallback(){
                    public void onSuccess(Object obj) {
                        // Get the validation response
                        DialogValidationResponse response = (DialogValidationResponse)obj;
                        if (response.isValid()) {
                            // Do the feed check
                            StandardFeedDialog.this.checkUniqueFeedName(feedName, cb);
                        } else {
                            cb.onSuccess(obj);
                        }
                    }
                    public void onFailure(Throwable t) {
                        cb.onFailure(t);
                    }
                });
            } else {
                // Simply validate feed title
                this.checkUniqueFeedTitle(feedName, cb);
            }
            return;
        }        
    }

    protected Widget getParametersPanel() {
        FlowPanel paramsPanel = new FlowPanel();
        Label feedNameLabel = new Label();
        feedNameLabel.setStyleName("feedname-label");
        feedNameLabel.setText(app.getTranslation(getDialogTranslationName() + ".feedname"));
        paramsPanel.add(feedNameLabel);
        feedNameTextBox = new TextBox();
        if ((feed!=null)&&(feed.getName()!=null))
            feedNameTextBox.setText(feed.getName());
        // If it's a feed update and the feed has a title, print the title
        if (feed.getPageName().trim().length() > 0) {
            if (feed.getTitle().trim().length() > 0) {
                feedNameTextBox.setText(feed.getTitle());
            }
        }
        feedNameTextBox.setVisibleLength(60);
        feedNameTextBox.setName("feedname");
        feedNameTextBox.setStyleName(getCSSName("feedname"));
        paramsPanel.add(feedNameTextBox);
        Label feedURLLabel = new Label();
        feedURLLabel.setStyleName("feedurl-label");
        feedURLLabel.setText(app.getTranslation(getDialogTranslationName() + ".feedurl"));
        paramsPanel.add(feedURLLabel);
        feedURLTextBox = new TextBox();
        if ((feed!=null)&&(feed.getUrl()!=null))
            feedURLTextBox.setText(feed.getUrl());
        feedURLTextBox.setVisibleLength(60);
        feedURLTextBox.setName("feedurl");
        feedURLTextBox.setStyleName(getCSSName("feedurl"));
        paramsPanel.add(feedURLTextBox);
        paramsPanel.add(getGroupsFields());
        return paramsPanel;
    }

}
