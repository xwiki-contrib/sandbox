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

import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.data.Group;
import com.xpn.xwiki.watch.client.data.Keyword;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Map;
import java.util.Iterator;

public class KeywordDialog extends ValidatingDialog {
    protected TextBox keywordTextBox = new TextBox();
    protected ListBox groupListBox = new ListBox();
    protected Keyword keyword;

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     */
    public KeywordDialog(XWikiGWTApp app, String name, int buttonModes, Keyword keyword) {
        super(app, name, buttonModes);
        this.keyword = keyword;

        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getParametersPanel());
        main.add(getActionsPanel());
        add(main);
    }

    protected void updateData() {
        String keywordString = keywordTextBox.getText();
        //no keyword by default
        String groupPageName = "";
        int selectedIndex = (groupListBox==null) ? -1 : groupListBox.getSelectedIndex();
        // If the All item is selected set no group
        if (selectedIndex > 0) {
            groupPageName = groupListBox.getValue(selectedIndex);
        }
        this.keyword.setName(keywordString);
        this.keyword.setGroup(groupPageName);
    }

    protected void validateDialogData(final AsyncCallback cb) {
        String keyword = this.keywordTextBox.getText();
        final DialogValidationResponse response = new DialogValidationResponse();
        if (keyword.equals("")) {
            response.setValid(false);
            response.setMessage(app.getTranslation(getDialogTranslationName() + ".nokeyword"));
            cb.onSuccess(response);
            return;
        }

        //also eliminate the all group
        String group = this.groupListBox.getSelectedIndex() < 1 ? null
                : this.groupListBox.getValue(this.groupListBox.getSelectedIndex());

        String newGroupName = group == null ? "" : group.trim();
        String oldGroupName = this.keyword.getGroup() == null ? "" : this.keyword.getGroup().trim();

        //if nothing changed, don't do the checking
        if (newGroupName.equalsIgnoreCase(oldGroupName)
            && this.keywordTextBox.getText().trim().equalsIgnoreCase(this.keyword.getName().trim())) {
            response.setValid(true);
            cb.onSuccess(response);
        } else {
            ((Watch)this.app).getDataManager().existsKeyword (keyword, group, new XWikiAsyncCallback(this.app) {
                public void onFailure(Throwable throwable)
                {
                    super.onFailure(throwable);
                    cb.onFailure(throwable);
                }

                public void onSuccess(Object o)
                {
                    super.onSuccess(o);
                    Boolean checkResponse = (Boolean)o;
                    if (checkResponse.booleanValue()) {
                        response.setValid(false);
                        response.setMessage(
                            app.getTranslation(getDialogTranslationName() + ".notuniquename"));
                    } else {
                        response.setValid(true);
                    }
                    cb.onSuccess(response);
                }
            });
        }
        return;
    }

    public void onValid(final DialogValidationResponse response)
    {
        updateData();
        setCurrentResult(keyword);
        //if the kw has just been created, add it
        if (KeywordDialog.this.keyword.getPageName().equals("")) {
            ((Watch)app).addKeyword(KeywordDialog.this.keyword, new AsyncCallback() {
                public void onFailure(Throwable throwable)
                {
                    // There should already have been an error display
                }

                public void onSuccess(Object object)
                {
                    KeywordDialog.this.endSuperDialog();
                }
            });
        } else {
            this.endSuperDialog();
        }        
    }

    protected Widget getParametersPanel() {
        FlowPanel paramsPanel = new FlowPanel();
        Label keywordLabel = new Label();
        keywordLabel.setStyleName("keyword-label");
        keywordLabel.setText(app.getTranslation(getDialogTranslationName() + ".keyword"));
        paramsPanel.add(keywordLabel);
        if (keyword != null) {
            keywordTextBox.setText(keyword.getName());
        }
        keywordTextBox.setVisibleLength(20);
        keywordTextBox.setName("keyword");
        keywordTextBox.setStyleName(getCSSName("keyword"));
        paramsPanel.add(keywordTextBox);
        paramsPanel.add(getGroupsFields());
        return paramsPanel;
    }

    protected Widget getGroupsFields() {
        FlowPanel groupsPanel = new FlowPanel();
        Label groupLabel = new Label();
        groupLabel.setStyleName("groups-label");
        groupLabel.setText(app.getTranslation(getDialogTranslationName() + ".groups"));
        groupsPanel.add(groupLabel);
        groupListBox.setMultipleSelect(false);
        Map groupMap = ((Watch)app).getConfig().getGroups();
        Iterator it = groupMap.keySet().iterator();
        boolean selected = false;
        String all = ((Watch)app).getTranslation("all");
        groupListBox.addItem("all", all);
        while (it.hasNext()) {
            String groupname = (String) it.next();
            if (!groupname.equals(all)) {
                //get group for this key
                Group currentGroup = (Group)groupMap.get(groupname);
                //don't add it unless it is a real group
                if (!currentGroup.getPageName().equals("") 
                    || this.keyword.getGroup().equals(groupname)) {
                    String grouptitle = currentGroup.getName();
                    groupListBox.addItem(grouptitle,groupname);
                    if (this.keyword.getGroup().equals(groupname)) {
                        selected = true;
                        groupListBox.setItemSelected(groupListBox.getItemCount() - 1, true);
                    }
                }
            }
        }
        // we need to select the first item (All) if none is selected
        if (selected==false) {
            groupListBox.setItemSelected(0, true);
        }
        groupsPanel.add(groupListBox);
        return groupsPanel;
    }
}
