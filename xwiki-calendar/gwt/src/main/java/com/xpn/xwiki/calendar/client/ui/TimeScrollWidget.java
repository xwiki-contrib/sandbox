package com.xpn.xwiki.calendar.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.xpn.xwiki.calendar.client.config.XConfig;

public class TimeScrollWidget extends Composite {
	
	private HorizontalPanel mainPanel;
	private Button 			btnNext;
	private Button  		btnPrev;
	private Button 			btnToday;
	private Label			lToday;	
	private Button 			btnSelect;
	
	private Image           imgPrev;
	private Image			imgNext;
	
	public Label getTodayLabel() {
		return lToday;
	}

	public void setTodayLabel(Label today) {
		lToday = today;
	}
	
	public TimeScrollWidget(){
		initialize(0);
	}
	
	public TimeScrollWidget(int view){
		initialize(view);
	}

	private void initialize(int view) {
//		btnNext = new Button("Next");
//		btnPrev = new Button("Prev.");
		lToday 	= new Label("4 aout 2008");
		btnToday = new Button("Aujourd'hui");
		btnSelect = new Button("Sélectionner");
		
		lToday.setStyleName(XConfig.CalendarTodayLabelStyleName);
		
		CalendarImageBundle imgBundle = (CalendarImageBundle) GWT.create(CalendarImageBundle.class);
		imgPrev = imgBundle.timescrollPrevImage().createImage();
		imgNext = imgBundle.timescrollNextImage().createImage();
		imgNext.setStyleName(XConfig.CalendarTimeScrollImgStyleName);
		imgPrev.setStyleName(XConfig.CalendarTimeScrollImgStyleName);
		
		mainPanel = new HorizontalPanel();
		//mainPanel.add(btnSelect);
		mainPanel.add(imgPrev);
		mainPanel.add(lToday);
		mainPanel.add(imgNext);
		//mainPanel.add(btnToday);
		
		initWidget(mainPanel);
		setStyleName(XConfig.CalendarDayscrollWidgetStyleName);
	}
	
	public void setView(int view){		
	}
	
	public void addNextClickListener(ClickListener listener){
		imgNext.addClickListener(listener);
	}
	
	public void addPrevClickListener(ClickListener listener){
		imgPrev.addClickListener(listener);
	}
	
	public void addTodayClickLitener(ClickListener listener){
		btnToday.addClickListener(listener);
	}
	
	public void addSelectClickListener(ClickListener listener){
		btnSelect.addClickListener(listener);
	}


}
