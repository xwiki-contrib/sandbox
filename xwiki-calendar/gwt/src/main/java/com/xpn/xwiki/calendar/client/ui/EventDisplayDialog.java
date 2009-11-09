package com.xpn.xwiki.calendar.client.ui;

import java.util.Date;

import org.gwtwidgets.client.util.SimpleDateFormat;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.calendar.client.data.*;


public class EventDisplayDialog extends DialogBox {
	
	private FlexTable bckgrid;
	private Label  lObject;
	private Label  lDates;
	private Label  lCategory;
	private Label  lDescription;
	private Label  lLocation;
	private Button btnClose;
	private Button btnUpdate;
	private Button btnDelete;
	
	private XEvent event;
	
	
	public EventDisplayDialog(){
	}
	
	public EventDisplayDialog(XEvent ev){
		this.event = ev;
		initialize();
		setWidget(bckgrid);
		setStyleName(XConfig.CalendarEventDisplayDialogStyle);
		center();
	}
	
	public String getFormattedDate(XEvent event){
		XCalendar sc = new XCalendar(event.getStartDate());
		XCalendar ec = new XCalendar(event.getEndDate());
		
		String dt = "";
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
		if( sc.getYear() == ec.getYear() )
		{
			if( sc.getMonth() == ec.getMonth() )
			{
				if( sc.getDayOfMonth() == ec.getDayOfMonth())
				{
					dt = sc.getDayAndMonth() + ", " + fmt.format(sc.getDate()) + "-" + fmt.format(ec.getDate());
				}else{
					dt = sc.getDayAndMonth() + " " + fmt.format(sc.getDate()) + "-" + ec.getDayAndMonth() + " " + fmt.format(ec.getDate());
				}
			}else{
				dt = sc.getDayAndMonth() + "-" + ec.getDayAndMonth();
			}
		}else{
			dt = sc.getLongDate() + "-" + ec.getLongDate();
		}
		return dt;
	}
	
	public void initialize(){
		bckgrid = new FlexTable();
		bckgrid.setCellSpacing(4);
		
		setTitle("Informations de l'événement");
		
		lObject = new Label();
		lObject.setText(event.getObject());
		lObject.setStyleName(XConfig.CalendarEventDisplayTitleStyleName);
		
		lDates = new Label();
		String dt = getFormattedDate(event);
		lDates.setText(dt);
		
		lCategory = new Label();
		lCategory.setText(event.getCategory());
		
		lDescription = new Label();
		lDescription.setText(event.getDescription());
		
		lLocation = new Label();
		lLocation.setText(event.getLocation());
		
		btnClose = new Button("Fermer");
		btnUpdate = new Button("Modifier");
		btnDelete = new Button("Supprimer");
		
		bckgrid.setWidget(0, 0, lObject);
		bckgrid.setWidget(1, 0, lDates);
		bckgrid.setWidget(2, 0, lCategory);
		bckgrid.setWidget(3, 0, lDescription);
		bckgrid.setWidget(4, 0, lLocation);
		HorizontalPanel btnPanel = new HorizontalPanel();
		HTML spacer = new HTML();
		spacer.setWidth("100px");
		btnPanel.add(spacer);
		btnPanel.add(btnUpdate);
		btnPanel.add(btnDelete);
		btnPanel.add(btnClose);
		btnPanel.setStyleName(XConfig.CalendarEventDisplayButtonPanelStyleName);
		bckgrid.setWidget(5, 0, btnPanel);
	}
	
	public void addCloseButtonClickListener(ClickListener listener){
		btnClose.addClickListener(listener);
	}
	
	public void addUpdateButtonClickListener(ClickListener listener){
		btnUpdate.addClickListener(listener);
	}
	
	public void addDeleteButtonClickListener(ClickListener listener){
		btnDelete.addClickListener(listener);
	}

}
