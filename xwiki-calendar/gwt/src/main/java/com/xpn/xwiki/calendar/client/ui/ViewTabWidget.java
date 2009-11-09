package com.xpn.xwiki.calendar.client.ui;

import com.google.gwt.user.client.ui.TabBar;
import com.xpn.xwiki.calendar.client.config.XConfig;

public class ViewTabWidget extends TabBar {
	
	public ViewTabWidget(){
		initialize();
	}

	private void initialize() {
		// TODO Auto-generated method stub
		setWidth("100%");
		for(int i = 0; i < XConfig.CalendarViewLabels.length; i++){
			addTab(XConfig.CalendarViewLabels[i]);
		}
	}

}
