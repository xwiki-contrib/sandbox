/*
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

import org.gwtwidgets.client.ui.cal.CalendarDate;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.ui.WatchWidget;
import com.google.gwt.user.client.ui.*;


public class FilterBarWidget  extends WatchWidget {

    protected TagCloudWidget tagCloudWidget;
    protected KeywordsWidget keywordsWidget;
    protected StateSelectorsWidget filterWidget;

    public FilterBarWidget() {
        super();
    }

    public String getName() {
        return "filterbar";
    }
    
    public FilterBarWidget(Watch watch) {
        super(watch);
        setPanel(new FlowPanel());
        initWidget(panel);
        init();
        panel.add(getTitlePanel());
        panel.add(getInitFilterPanel());
        panel.add(getFilterPanel());
        panel.add(getKeywordsPanel());
        panel.add(getTagCloudPanel());
        panel.add(getDatesPanel());
    }

    public void refreshData() {
    }

    public void resetSelections() {
        panel.clear();
        panel.add(getTitlePanel());
        panel.add(getInitFilterPanel());
        panel.add(getFilterPanel());
        panel.add(getKeywordsPanel());
        panel.add(getTagCloudPanel());
        panel.add(getDatesPanel());
    }

    private Widget getTagCloudPanel() {
        if (tagCloudWidget==null)
         tagCloudWidget =  new TagCloudWidget(watch);
        return tagCloudWidget;
    }

    private Widget getKeywordsPanel() {
        if (keywordsWidget==null)
         keywordsWidget = new KeywordsWidget(watch);
        return keywordsWidget;
    }

    private Widget getFilterPanel()
    {
        if (this.filterWidget == null) {
            this.filterWidget = new StateSelectorsWidget(watch);
        }
        return this.filterWidget;
    }

    private Widget getInitFilterPanel() {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("filter", "init"));
        HTML textHTML = new HTML(watch.getTranslation("filter.initfilter"));
        textHTML.setStyleName(watch.getStyleName("filter", "init-text"));
        textHTML.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                watch.refreshOnResetFilter();                
            }
        });
        p.add(textHTML);
        return p;
    }

    private Widget getTitlePanel() {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("filter", "title"));
        HTML titleHTML = new HTML(watch.getTranslation("filter.title"));
        titleHTML.setStyleName(watch.getStyleName("filter", "title-text"));
        titleHTML.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                // Show hide filter zone
            }
        });
        p.add(titleHTML);
        return p;
    }
    
    private Widget getDatesPanel() {
        FlowPanel p = new FlowPanel();
        FlowPanel titlePanel = new FlowPanel();
        titlePanel.setStyleName(watch.getStyleName("filter", "dates-title"));
        HTML titleHTML = new HTML(watch.getTranslation("filter.dates.title"));
        titleHTML.setStyleName(watch.getStyleName("filter", "title-dates-text"));
        titlePanel.add(titleHTML);
        p.add(titlePanel);
        String startLabel = watch.getTranslation("filter.dates.startDate");
        DatePickerWidget startDateWidget = new DatePickerWidget(watch, startLabel) {
            public void onDateClick(CalendarDate date) {
                super.onDateClick(date);
                watch.refreshOnDateStartChange(date.getDate());
            }
            public String getName() {
                return "datestartwidget";
            }
        };
        String endLabel = watch.getTranslation("filter.dates.endDate");
        DatePickerWidget endDateWidget = new DatePickerWidget(watch, endLabel) {
            public void onDateClick(CalendarDate date) {
                super.onDateClick(date);
                watch.refreshOnDateEndChange(date.getDate());
            }
            public String getName() {
                return "dateendwidget";
            }
        };
        p.add(startDateWidget);
        p.add(endDateWidget);
        return p;
    }

    public void resizeWindow() {
        // Watch.setMaxHeight(panel);
    }
}
