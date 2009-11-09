package com.xpn.xwiki.calendar.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.calendar.client.config.XConfig;

public class FormEntryWidget extends Composite {

	private HorizontalPanel mainPanel;
	private Widget 			wtitle;
	private Widget			wfield;
	
	public HorizontalPanel getMainPanel() {
		return mainPanel;
	}

	public void setMainPanel(HorizontalPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public Widget getTitleWidget() {
		return wtitle;
	}

	public void setTitleWidget(Widget title) {
		this.wtitle = title;
	}

	public Widget getFieldWidget() {
		return wfield;
	}

	public void setFieldWidget(Widget field) {
		this.wfield = field;
	}
	
	public FormEntryWidget(){
		initialize();
	}
	
	public FormEntryWidget(Widget title, Widget field){
		this.wtitle = title;
		this.wfield = field;
		initialize();
	}
	
	public FormEntryWidget(String title, Widget field){		
		this.wtitle = new HTML(title);
		this.wfield = field;
		this.wtitle.setStyleName(XConfig.CalendarFormTitleStyleName);
		this.wfield.setStyleName(XConfig.CalendarFormFieldStyleName);
	}
	
	public void initialize(){
		mainPanel = new HorizontalPanel();
		mainPanel.add(wtitle);
		mainPanel.add(wfield);
		initWidget(mainPanel);
		setStyleName(XConfig.CalendarFormStyleName);
	}
}
