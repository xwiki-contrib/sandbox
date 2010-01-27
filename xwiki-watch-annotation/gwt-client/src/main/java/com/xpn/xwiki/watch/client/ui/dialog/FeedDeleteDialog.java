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
 */

package com.xpn.xwiki.watch.client.ui.dialog;

import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.data.Feed;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * Dialog to confirm and configure the feed removal.
 * It will display the confirm message and allow the user to specify if the fetched articles for this
 * feed should also be removed.
 */
public class FeedDeleteDialog extends Dialog
{
    private Feed feed;
    private CheckBox deleteArticlesCheckBox;

    public FeedDeleteDialog(XWikiGWTApp xWikiGWTApp, String name, Feed feed)
    {
        this(xWikiGWTApp, name, feed, null);
    }

    public FeedDeleteDialog(XWikiGWTApp xWikiGWTApp, String name, Feed feed,
        AsyncCallback nextCallback)
    {
        super(xWikiGWTApp, name, Dialog.BUTTON_NEXT | Dialog.BUTTON_CANCEL, nextCallback);

        this.feed = feed;

        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));
        String feedTitle = feed.getTitle().trim().length() > 0 ? feed.getTitle() : feed.getName();
        HTMLPanel questionPanel = new HTMLPanel(
            app.getTranslation("removefeed.confirm", new String[] {feedTitle}));
        questionPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(questionPanel);
        //create the checkbox
        this.deleteArticlesCheckBox = new CheckBox();
        this.deleteArticlesCheckBox.setText(
                app.getTranslation(getDialogTranslationName() + ".withArticles"));
        this.deleteArticlesCheckBox.setChecked(false);
        this.deleteArticlesCheckBox.setStyleName(getCSSName("witharticles"));
        main.add(deleteArticlesCheckBox);     
        main.add(getActionsPanel());
        this.add(main);
    }

    protected void endDialog()
    {
        //update the data of this dialog
        this.setCurrentResult(new Boolean(this.deleteArticlesCheckBox.isChecked())); 
        ((Watch)this.app).getDataManager().removeFeed(this.feed, this.deleteArticlesCheckBox.isChecked(),
            new XWikiAsyncCallback(this.app) {
                public void onFailure(Throwable throwable)
                {
                    super.onFailure(throwable);
                }
                public void onSuccess(Object o)
                {
                    super.onSuccess(o);
                    endDialog2();
                    ((Watch)this.app).refreshOnFeedDelete(FeedDeleteDialog.this.feed,
                            FeedDeleteDialog.this.deleteArticlesCheckBox.isChecked());
                }
            });
    }

    protected void endDialog2()
    {
        super.endDialog();        
    }
}