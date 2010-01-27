package com.xpn.xwiki.watch.client.ui.dialog;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.data.Group;


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

public class GroupDialog extends ValidatingDialog {
    protected TextBox groupTextBox;
    protected Group group;

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     */
    public GroupDialog(XWikiGWTApp app, String name, int buttonModes, Group group) {
        super(app, name, buttonModes);
        this.group = group;

        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getParametersPanel());
        main.add(getActionsPanel());
        add(main);
    }

    protected void validateDialogData(final AsyncCallback cb) {
        String groupName = groupTextBox.getText();
        final DialogValidationResponse response = new DialogValidationResponse();
        if (groupName.equals("")) {
            response.setValid(false);
            response.setMessage(app.getTranslation(getDialogTranslationName() + ".nogroup"));
            cb.onSuccess(response);
            return;
        }

        //only do unicity check if the name of the group has changed
        String oldGroupName = this.group.getName().trim();
        if (oldGroupName.equalsIgnoreCase(groupName)) {
            response.setValid(true);
            cb.onSuccess(response);
        } else {
            ((Watch)this.app).getDataManager().existsGroup(groupName, new XWikiAsyncCallback(this.app){
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

    protected void updateData() {
        this.group.setName(groupTextBox.getText().trim());
    }

    protected Widget getParametersPanel() {
        FlowPanel paramsPanel = new FlowPanel();
        Label groupLabel = new Label();
        groupLabel.setStyleName("group-label");
        groupLabel.setText(app.getTranslation(getDialogTranslationName() + ".group"));
        paramsPanel.add(groupLabel);
        groupTextBox = new TextBox();
        if (group!=null)
            groupTextBox.setText(group.getName());
        groupTextBox.setVisibleLength(20);
        groupTextBox.setName("group");
        groupTextBox.setStyleName(getCSSName("group"));
        paramsPanel.add(groupTextBox);
        paramsPanel.add(getGroupsFields());
        return paramsPanel;
    }

    protected Widget getGroupsFields() {
        return new FlowPanel();
    }

    public void onValid(final DialogValidationResponse response)
    {
        updateData();
        setCurrentResult(group);
        if (GroupDialog.this.group.getPageName().equals("")) {
            ((Watch)app).addGroup(group, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    // There should already have been an error display
                    ((Watch)app).refreshFeedTree();
                }

                public void onSuccess(Object object) {
                    GroupDialog.this.endSuperDialog();
                    ((Watch)app).refreshFeedTree();
                }
            });
        } else {
            this.endSuperDialog();
        }            
    }
}
