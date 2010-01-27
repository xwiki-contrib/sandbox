package com.xpn.xwiki.watch.client.ui.dialog;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.*;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.watch.client.Watch;

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

public class AnalysisDialog extends Dialog {
    protected HTML analysisHTML;
    protected String[] languages;

    /**
     * Choice dialog
     * @param app  XWiki GWT App object to access translations and css prefix names
     * @param name dialog name
     * @param buttonModes button modes Dialog.BUTTON_CANCEL|Dialog.BUTTON_NEXT for Cancel / Next
     */
    public AnalysisDialog(XWikiGWTApp app, String name, int buttonModes) {
        this(app, name, buttonModes, new String[0]);
    }
    
    public AnalysisDialog(XWikiGWTApp app, String name, int buttonModes, String[] languages) {
        super(app, name, buttonModes);

        this.languages = languages;
        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getLanguageSelectionPanel());
        main.add(getAnalysisPanel());
        main.add(getActionsPanel());
        add(main);
    }
    
    protected Panel getLanguageSelectionPanel() {
        FlowPanel languagePanel = new FlowPanel();
        Label langLabel = new Label(app.getTranslation(getDialogTranslationName() + ".language"));
        languagePanel.add(langLabel);
        String langRadioGroupName = "langRadioGroup";
        for (int i = 0; i < this.languages.length; i++) {
            final String currentLanguage = this.languages[i];
            RadioButton langRadio = new RadioButton(langRadioGroupName, 
                    app.getTranslation("language." + currentLanguage));
            if (((Watch)app).getLocale().trim().toLowerCase().equals(currentLanguage.toLowerCase())) {
                langRadio.setChecked(true);
            }
            langRadio.addClickListener(new ClickListener() {
                private String language = currentLanguage;
                public void onClick(Widget widget) {
                    //activate current language
                    AnalysisDialog.this.fetchAnalysisHTML(language);
                }
            });
            languagePanel.add(langRadio);
        }
        languagePanel.addStyleName(getCssPrefix() + "-lang");
        return languagePanel;
    }

    protected Widget getAnalysisPanel() {
        analysisHTML = new HTML();
        analysisHTML.setStyleName(getCssPrefix() + "-html");
        this.fetchAnalysisHTML(null);
        return analysisHTML;
    }
    
    protected void fetchAnalysisHTML(final String language) {
        final Watch watch = (Watch)app;
        watch.getDataManager().getAnalysisHTML(watch.getFilterStatus(), language, 
                new XWikiAsyncCallback(watch) {
            public void onSuccess(Object result) {
                super.onSuccess(result);
                analysisHTML.setHTML((String) result);
                //get Element
                Element DOMEl = analysisHTML.getElement();
                DOM.sinkEvents(DOMEl, Event.ONCLICK);
                DOM.setEventListener(DOMEl, new EventListener() {
                    public void onBrowserEvent(Event event)
                    {
                        if (DOM.eventGetType(event) == Event.ONCLICK) {
                            Element eventTarget = DOM.eventGetTarget(event);
                            if (DOM.getElementProperty(eventTarget, "tagName").trim().equalsIgnoreCase("a")) {
                                //close the dialog
                                AnalysisDialog.this.cancelDialog();
                                //search
                                watch.refreshOnSearch(DOM.getInnerText(eventTarget));
                            }
                        }
                    }
                });
            }
        });        
    }
}
