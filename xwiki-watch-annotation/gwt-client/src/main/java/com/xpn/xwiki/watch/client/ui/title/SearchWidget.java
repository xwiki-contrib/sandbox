package com.xpn.xwiki.watch.client.ui.title;

import com.xpn.xwiki.watch.client.ui.WatchWidget;
import com.xpn.xwiki.watch.client.ui.utils.ButtonClickReturnHandler;
import com.xpn.xwiki.watch.client.Watch;
import com.google.gwt.user.client.ui.*;

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

public class SearchWidget extends WatchWidget {
    protected TextBox searchBox = new TextBox();

    public SearchWidget() {
        super();
    }

    public String getName() {
        return "search";
    }
    
    public SearchWidget(Watch watch) {
        super(watch);
        setPanel(new FlowPanel());
        initWidget(panel);
    }

    public void init() {
        super.init();
        HTML html = new HTML(watch.getTranslation("search"));
        html.setStyleName(watch.getStyleName("search-text"));
        panel.add(html);
        panel.add(searchBox);
        searchBox.setStyleName(watch.getStyleName("search-box"));
        searchBox.setVisibleLength(15);
        Button okButton = new Button(watch.getTranslation("ok"));
        okButton.setStyleName(watch.getStyleName("search-button"));
        okButton.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                String text = searchBox.getText();
                if (!text.equals("")) {
                    watch.refreshOnSearch(text);   
                } else {
                    watch.refreshOnSearch(null);
                }
            }
        });
        searchBox.addKeyboardListener(new ButtonClickReturnHandler(okButton));
        panel.add(okButton);
    }

    public void resetSelections()
    {
        super.resetSelections();
        //put the keyword text in the text input
        this.searchBox.setText(watch.getFilterStatus().getKeyword());
    }
}
