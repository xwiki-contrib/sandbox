package com.xpn.xwiki.watch.client.ui.dialog;

import com.xpn.xwiki.watch.client.Constants;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.gwt.api.client.dialog.ModalMessageDialog;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;

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

public class PressReviewMailDialog extends Dialog {
    protected TextBox mailSubjectTextBox;
    protected TextBox mailToTextBox;
    protected TextArea mailContentTextArea;
    protected CheckBox articlesContentCheckBox;
    protected CheckBox articlesCommentsCheckBox;
    
    public PressReviewMailDialog(Watch watch, String dialogName, int buttonModes) {
        super(watch, dialogName, buttonModes);
        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getParametersPanel());
        main.add(getActionsPanel());
        add(main);

    }

    protected Widget getParametersPanel() {
        FlowPanel paramsPanel = new FlowPanel();
        Label mailSubjectLabel = new Label();
        mailSubjectLabel.setStyleName("mailsubject-label");
        mailSubjectLabel.setText(app.getTranslation(getDialogTranslationName() + ".mailsubject"));
        paramsPanel.add(mailSubjectLabel);

        mailSubjectTextBox = new TextBox();
        mailSubjectTextBox.setVisibleLength(60);
        mailSubjectTextBox.setName("mailsubject");
        mailSubjectTextBox.setStyleName(getCSSName("mailsubject"));
        mailSubjectTextBox.setText(app.getTranslation(getDialogTranslationName() + ".mailsubjectdefault"));
        paramsPanel.add(mailSubjectTextBox);

        Label mailToLabel = new Label();
        mailToLabel.setStyleName("mailto-label");
        mailToLabel.setText(app.getTranslation(getDialogTranslationName() + ".mailto"));
        paramsPanel.add(mailToLabel);

        mailToTextBox = new TextBox();
        mailToTextBox.setVisibleLength(60);
        mailToTextBox.setName("mailto");
        mailToTextBox.setStyleName(getCSSName("mailto"));
        paramsPanel.add(mailToTextBox);

        Label mailContentLabel = new Label();
        mailContentLabel.setStyleName("mailcontent-label");
        mailContentLabel.setText(app.getTranslation(getDialogTranslationName() + ".mailcontent"));
        paramsPanel.add(mailContentLabel);

        mailContentTextArea = new TextArea();
        mailContentTextArea.setVisibleLines(5);
        mailContentTextArea.setName("mailcontent");
        mailContentTextArea.setStyleName(getCSSName("mailcontent"));
        mailContentTextArea.setText(app.getTranslation(getDialogTranslationName() + ".mailcontentdefault"));
        paramsPanel.add(mailContentTextArea);
        
        articlesContentCheckBox = new CheckBox();
        articlesContentCheckBox.setText(app.getTranslation(getDialogTranslationName() 
                                           + ".articleswithcontent"));
        articlesContentCheckBox.setStyleName(getCSSName("withcontent"));
        paramsPanel.add(articlesContentCheckBox);
        
        articlesCommentsCheckBox = new CheckBox();
        articlesCommentsCheckBox.setText(app.getTranslation(getDialogTranslationName() 
                                           + ".articleswithcomments"));
        articlesCommentsCheckBox.setStyleName(getCSSName("withcomments"));
        paramsPanel.add(articlesCommentsCheckBox);
        
        return paramsPanel;
    }
    
    protected boolean validateEmailDialog() {    
        if (this.mailToTextBox.getText().trim() == "") {
            Window.alert(app.getTranslation(getDialogTranslationName() + ".noaddress"));
            return false;
        }
        return true;
    }

    protected void endDialog() {
        if(validateEmailDialog()) {
            //send email, all is ok
            //parse emails
            String[] emailsArray = this.mailToTextBox.getText().trim().split(", ");
            String mailSubject = this.mailSubjectTextBox.getText();
            String mailContent = this.mailContentTextArea.getText();
            boolean withContent = this.articlesContentCheckBox.isChecked();
            boolean withComments = this.articlesCommentsCheckBox.isChecked();
            ((Watch)app).getDataManager().sendEmail(((Watch)app).getFilterStatus(),
                    Constants.DEFAULT_CODE_SPACE + "." + Constants.PAGE_EMAIL_PRESSREVIEW, 
                    mailSubject, emailsArray, mailContent, withContent, withComments, 
                    new XWikiAsyncCallback(app) {
                public void onSuccess(Object result) {
                    super.onSuccess(result);
                    setCurrentResult(result);
                    String resultText = (String) result;
                    endDialog2();
                    ModalMessageDialog mmdb = new ModalMessageDialog(app, 
                        app.getTranslation(getDialogTranslationName() + ".dialogtitle"), 
                        resultText);
                }
                public void onFailure(Throwable caught) {
                    super.onFailure(caught);
                }
            });
        }
    }
    
    protected void endDialog2() {
        super.endDialog();
    }

}
