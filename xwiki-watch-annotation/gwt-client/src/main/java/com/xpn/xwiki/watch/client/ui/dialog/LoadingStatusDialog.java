package com.xpn.xwiki.watch.client.ui.dialog;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
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

public class LoadingStatusDialog extends Dialog {
    protected HTML contentHtml;

    public LoadingStatusDialog(XWikiGWTApp app, String name, int buttonModes) {
        super(app, name, buttonModes);

        FlowPanel main = new FlowPanel();
        main.addStyleName(getCSSName("main"));

        HTMLPanel invitationPanel = new HTMLPanel(app.getTranslation(getDialogTranslationName() + ".invitation"));
        invitationPanel.addStyleName(getCssPrefix() + "-invitation");
        main.add(invitationPanel);
        main.add(getContentPanel());
        main.add(getActionsPanel());
        add(main);
    }

    protected Widget getContentPanel() {
        contentHtml = new HTML();
        Watch watch = (Watch) app;
        Map paramMap = new HashMap();
        paramMap.put("xpage", "plain");
        paramMap.put("space", watch.getWatchSpace());
        paramMap.put("showstats", "1");
        // Get the content
        watch.getXWikiServiceInstance().getDocumentContent(
            Constants.DEFAULT_CODE_SPACE + "." + Constants.PAGE_LOADING_STATUS, true, paramMap,
            new AsyncCallback() {
                public void onSuccess(Object result) {
                    // Put the result in the content html
                    contentHtml.setHTML((String)result);
                }
                public void onFailure(Throwable caught) {}
            });
        contentHtml.setStyleName(getCssPrefix() + "-content");
        return contentHtml;
    }
}
