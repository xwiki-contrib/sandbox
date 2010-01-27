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

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.data.FilterStatus;
import com.xpn.xwiki.watch.client.ui.WatchWidget;

/**
 * Encapsulates the checkbox filter widgets, for selecting read / unread and flagged / trashed articles.
 */
public class StateSelectorsWidget extends WatchWidget
{
    protected CheckBox readCheckBox;

    protected CheckBox unreadCheckBox;

    protected CheckBox flaggedCheckBox;

    protected CheckBox trashedCheckBox;

    public CheckBox getReadCheckBox()
    {
        return readCheckBox;
    }

    public CheckBox getUnreadCheckBox()
    {
        return unreadCheckBox;
    }

    public CheckBox getFlaggedCheckBox()
    {
        return flaggedCheckBox;
    }

    public CheckBox getTrashedCheckBox()
    {
        return trashedCheckBox;
    }

    public StateSelectorsWidget()
    {
        super();
    }

    public String getName()
    {
        return "stateselectors";
    }

    public StateSelectorsWidget(Watch w)
    {
        super(w);
        setPanel(new FlowPanel());
        initWidget(panel);
        init();
        panel.setStyleName(watch.getStyleName("filter", "filter"));
        panel.add(getSeeOnlyTitlePanel());
        this.flaggedCheckBox = new CheckBox();
        panel.add(getCheckBoxPanel(this.flaggedCheckBox, "flagged", (watch.getFilterStatus().getFlagged() == 1),
            new ClickListener()
            {
                public void onClick(Widget widget)
                {
                    if (((CheckBox) widget).isChecked()) {
                        watch.refreshOnShowOnlyFlaggedArticles();
                        // Uncheck the trashed checkbox
                        CheckBox trashedCheckBox = getTrashedCheckBox();
                        if (trashedCheckBox.isChecked()) {
                            trashedCheckBox.setChecked(false);
                        }
                    } else {
                        watch.refreshOnNotShowOnlyFlaggedArticles();
                    }
                }
            }));
        this.readCheckBox = new CheckBox();
        panel.add(getCheckBoxPanel(this.readCheckBox, "read", (watch.getFilterStatus().getRead() == 1),
            new ClickListener()
            {
                public void onClick(Widget widget)
                {
                    if (((CheckBox) widget).isChecked()) {
                        watch.refreshOnShowOnlyReadArticles();
                        // Uncheck the unread checkbox
                        CheckBox unreadCheckBox = getUnreadCheckBox();
                        if (unreadCheckBox.isChecked()) {
                            unreadCheckBox.setChecked(false);
                        }
                    } else {
                        watch.refreshOnNotShowOnlyReadArticles();
                    }
                }
            }));
        this.unreadCheckBox = new CheckBox();
        panel.add(getCheckBoxPanel(this.unreadCheckBox, "unread", (watch.getFilterStatus().getRead() == -1),
            new ClickListener()
            {
                public void onClick(Widget widget)
                {
                    if (((CheckBox) widget).isChecked()) {
                        watch.refreshOnShowOnlyUnReadArticles();
                        // Uncheck the read articles checkbox
                        CheckBox readCheckBox = getReadCheckBox();
                        if (readCheckBox.isChecked()) {
                            readCheckBox.setChecked(false);
                        }
                    } else {
                        watch.refreshOnNotShowOnlyUnReadArticles();
                    }
                }
            }));
        this.trashedCheckBox = new CheckBox();
        panel.add(getCheckBoxPanel(this.trashedCheckBox, "trashed", (watch.getFilterStatus().getTrashed() == 1),
            new ClickListener()
            {
                public void onClick(Widget widget)
                {
                    if (((CheckBox) widget).isChecked()) {
                        watch.refreshOnShowOnlyTrashedArticles();
                        // Uncheck the flagged checkbox
                        CheckBox flaggedCheckBox = getFlaggedCheckBox();
                        if (flaggedCheckBox.isChecked()) {
                            flaggedCheckBox.setChecked(false);
                        }
                    } else {
                        watch.refreshOnNotShowOnlyTrashedArticles();
                    }
                }
            }));
    }

    private Widget getCheckBoxPanel(CheckBox checkBox, String name, boolean checked, ClickListener clickListener)
    {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("filter", "seeonly-" + name));
        if (checked)
            checkBox.setChecked(true);
        checkBox.setHTML(watch.getTranslation("filter.seeonly." + name));
        checkBox.addClickListener(clickListener);
        p.add(checkBox);
        return p;
    }

    private Widget getSeeOnlyTitlePanel()
    {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("filter", "seeonly-title"));
        HTML titleHTML = new HTML(watch.getTranslation("filter.seeonly.title"));
        titleHTML.setStyleName(watch.getStyleName("filter", "title-seeonly-text"));
        p.add(titleHTML);
        return p;
    }

    public void resetSelections()
    {
        FilterStatus filterStatus = watch.getFilterStatus();
        this.readCheckBox.setChecked((filterStatus.getRead() == 1) ? true : false);
        this.unreadCheckBox.setChecked((filterStatus.getRead() == -1) ? true : false);
        this.flaggedCheckBox.setChecked((filterStatus.getFlagged() == 1) ? true : false);
        this.trashedCheckBox.setChecked((filterStatus.getTrashed() == 1) ? true : false);
    }
}
