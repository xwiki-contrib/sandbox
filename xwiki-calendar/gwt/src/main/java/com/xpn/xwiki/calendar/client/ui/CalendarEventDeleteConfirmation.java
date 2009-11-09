package com.xpn.xwiki.calendar.client.ui;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.xpn.xwiki.calendar.client.config.XConfig;
import com.xpn.xwiki.calendar.client.data.XEvent;

public class CalendarEventDeleteConfirmation extends DialogBox {
	
	private FlexTable bckgrid;
	private Label  lObject;
	private Button btnClose;
	private Button btnDelete;
	
	private XEvent event;
	
	public XEvent getEvent() {
		return event;
	}


	public void setEvent(XEvent event) {
		this.event = event;
	}


	public CalendarEventDeleteConfirmation(XEvent ev){
		this.event = ev;
		initialize();
		setWidget(bckgrid);
		setStyleName(XConfig.CalendarEventDisplayDialogStyle);
		center();
	}
	
	
	public void initialize(){
		bckgrid = new FlexTable();
		bckgrid.setCellSpacing(4);
		
		setTitle("Confirmation de suppression de l'événement");
		
		lObject = new Label();
		lObject.setText("Voullez vous vraiment supprimer l'evenement '" + event.getObject() + "' ?");
		//lObject.setStyleName(XConfig.CalendarEventDisplayTitleStyleName);
		
		btnClose = new Button("Annuler");
		btnDelete = new Button("Supprimer");
		
		bckgrid.setWidget(0, 0, lObject);

		HorizontalPanel btnPanel = new HorizontalPanel();
		HTML spacer = new HTML();
		spacer.setWidth("100px");
		btnPanel.add(spacer);
		btnPanel.add(btnDelete);
		btnPanel.add(btnClose);
		btnPanel.setStyleName(XConfig.CalendarEventDisplayButtonPanelStyleName);
		bckgrid.setWidget(2, 0, btnPanel);
	}

	public void addCloseButtonClickListener(ClickListener listener){
		btnClose.addClickListener(listener);
	}
	
	public void addDeleteButtonClickListener(ClickListener listener){
		btnDelete.addClickListener(listener);
	}


}
