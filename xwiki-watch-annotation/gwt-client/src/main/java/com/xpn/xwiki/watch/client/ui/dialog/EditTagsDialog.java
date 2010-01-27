package com.xpn.xwiki.watch.client.ui.dialog;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.gwt.api.client.widgets.WordListSuggestOracle;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.Constants;

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

public class EditTagsDialog extends Dialog {
    protected SuggestBox tagsSuggestBox;
    protected String tags;

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     */
    public EditTagsDialog(XWikiGWTApp app, String name, int buttonModes, String tags) {
        super(app, name, buttonModes);
        this.tags = tags;

        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getParametersPanel());
        main.add(getActionsPanel());
        add(main);
    }

    protected boolean updateData() {
        String insertedTags =  tagsSuggestBox.getText().trim();
        // If there were no tags before and no tags were added, display an alert: the user is trying to add tags but no
        // tags were inserted
        if (insertedTags.length() == 0 && this.tags.trim().length() == 0) {
            Window.alert(app.getTranslation(getDialogTranslationName() + ".notags"));
            return false;
        }
        // Else, the user either deletes the tags, either adds tags and the tags list is non void 
        // update tags and return true
        this.tags = insertedTags;
        return true;
    }

    protected Widget getParametersPanel() {
        FlowPanel paramsPanel = new FlowPanel();
        Label tagsLabel = new Label();
        tagsLabel.setStyleName("tags-label");
        tagsLabel.setText(app.getTranslation(getDialogTranslationName() + ".tags"));
        paramsPanel.add(tagsLabel);
        TextBox tagsTextBox = new TextBox();
        tagsTextBox.setVisibleLength(30);
        tagsTextBox.setName("tags");
        WordListSuggestOracle tagListOracle = new WordListSuggestOracle(new TagListSuggestOracle((Watch)this.app),
                Constants.PROPERTY_TAGS_SEPARATORS_EDIT, true);
        tagsSuggestBox = new SuggestBox(tagListOracle, tagsTextBox);
        tagsTextBox.addStyleName(getCSSName("tags"));
        if (tags != null) {
            tagsTextBox.setText(tags);
        }
        paramsPanel.add(tagsSuggestBox);
        return paramsPanel;
    }

    protected void endDialog() {
        if (updateData()) {
            setCurrentResult(tags);
            super.endDialog();
        }
    }
}
