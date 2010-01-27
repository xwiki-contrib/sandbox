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
package com.xpn.xwiki.watch.client.ui.filterbar;

import com.google.gwt.user.client.ui.*;
import com.xpn.xwiki.watch.client.Constants;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.ui.WatchWidget;
import com.xpn.xwiki.watch.client.ui.utils.LoadingAsyncCallback;
import com.xpn.xwiki.watch.client.ui.utils.LoadingWidget;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class TagCloudWidget extends WatchWidget {
    private Map tagsLink = new HashMap();

    public TagCloudWidget() {
        super();
    }

    public String getName() {
        return "tagcloud";
    }
    
    public TagCloudWidget(Watch watch) {
        super(watch);
        Image loadingImage = new Image(watch.getSkinFile(Constants.IMAGE_LOADING_SPINNER));
        Panel loadingWidgetPanel = new FlowPanel();
        loadingWidgetPanel.add(loadingImage);
        loadingWidgetPanel.addStyleName(watch.getStyleName("tagcloud-loading"));
        Panel tagsPanel = new FlowPanel();
        tagsPanel.add(getTitlePanel());
        panel = new LoadingWidget(tagsPanel, loadingWidgetPanel);
        initWidget(panel);
        init();
    }

    private Widget getTitlePanel() {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("filter", "tagcloud-title"));
        HTML titleHTML = new HTML(watch.getTranslation("filter.tagcloud.title"));
        titleHTML.setStyleName(watch.getStyleName("filter", "title-tagcloud-text"));
        p.add(titleHTML);
        return p;
    }


    public void refreshData() {
        // Load the tags list
        watch.getDataManager().getTagsList(new LoadingAsyncCallback((LoadingWidget)panel) {
            public void onSuccess(Object result) {
                super.onSuccess(result);
                updateTagsList((List) result);
            }
            public void onFailure(Throwable t)
            {
                super.onFailure(t);
            }
        });
    }

    public void resetSelections() {
        setActiveTags((Object[]) watch.getFilterStatus().getTags().toArray());
    }

    public void updateTagsList(List list) {
        Panel tagsPanel = (Panel)((LoadingWidget)panel).getMainWidget();
        tagsPanel.clear();
        tagsPanel.add(getTitlePanel());
        tagsLink.clear();
        if (list!=null) {
            for (int i=0;i<list.size();i++) {
                List result = (List) list.get(i);
                final String name = (String) result.get(0);
                int count = ((Number)result.get(1)).intValue();
                Label label = new Label(name);
                int pixels = 9 + count;
                if (pixels
                    >15)
                 pixels = 15;
                label.addStyleName(watch.getStyleName("tagscloud", "link"));
                label.addStyleName(watch.getStyleName("tagscloud", "" + pixels));
                tagsLink.put(name, label);
                label.addClickListener(new ClickListener() {
                    public void onClick(Widget widget) {
                            watch.refreshOnTagActivated(name);
                        }
                    });
                tagsPanel.add(label);
            }
        }
    }

    public void setActiveTags(Object[] tags) {
        Iterator it = tagsLink.values().iterator();
        while (it.hasNext()) {
            ((Widget)it.next()).removeStyleName(watch.getStyleName("tagscloud", "active"));
        }
        for (int i=0;i<tags.length;i++) {
            Widget tag = (Widget) tagsLink.get(tags[i]);
            if (tag != null) {
                tag.addStyleName(watch.getStyleName("tagscloud", "active"));
            }
        }
    }

}
