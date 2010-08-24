package com.xpn.xwiki.calendar.client.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.xpn.xwiki.calendar.client.Calendar;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.calendar.client.data.*;

import org.gwtwidgets.client.util.SimpleDateFormat;
/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 *
 */
public class TimeSlotWidget extends Composite implements ClickListener {
	
	private VerticalPanel 	mainPanel;
	private VerticalPanel 	entryPanel;
	private XCalendar       calendar;
	private boolean			withHead;			
	private Date			startDate;
	private Date          	endDate;	
	private int  			hours;
	private int 			minutes;
	private List        	entrys;
	private Map				entrysMap;
	
	private int             currentView = -1;
	private int 			countDisplayed = -1;
	private int				index = 0;
	
	public TimeSlotWidget(){
		super();
		initialize();
	}
	
	public TimeSlotWidget(Date d, int h, int m, boolean withHead){
		calendar = new XCalendar(d);
		startDate = d;
		hours = h;
		minutes = m;
		entrys = new ArrayList();
		this.withHead = withHead;
		initialize();
	}
	
	public TimeSlotWidget(Date start, Date end, boolean withHead){
		//Window.alert("hello from timeslot constructor");
		calendar = new XCalendar(start);
		startDate = start;
		endDate = end;
		entrys = new ArrayList();
		this.withHead = withHead;
		initialize();
	}
	
	private void initialize(){
		mainPanel = new VerticalPanel();
		//entryPanel = new VerticalPanel();
		mainPanel.setWidth("100%");
		mainPanel.setHeight("100%");
		draw();
		//mainPanel.add(entryPanel);
		initWidget(mainPanel);
	}
	
	private void draw(){
		mainPanel.clear();
		
		if( withHead ){
			HTML head = new HTML(calendar.getNumericDayMonth());
			head.setStyleName(XConfig.CalendarTimeSlotHeadStyleName);
			head.setWidth("100%");
			mainPanel.add(head);
		}
		
		initScrollEntrys();
		
		for(int i = index; i < index+countDisplayed; i++){
			XEvent ev = (XEvent) entrys.get(i);
			HTML entry;
			if( withHead ){
				Date   dt = new Date(Date.parse(ev.getStartDate()));
				SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
				entry = new HTML( fmt.format(dt) + " - " + ev.getObject().substring(0, 12));
			}
			else
			{
				entry = new HTML( ev.getObject().substring(0, 30) );
			}
			
			ev.setRef(entry);
			entry.addClickListener(this);
			
			entry.setStylePrimaryName(XConfig.CalendarTimeSlotEntryStyleName);
			mainPanel.add(entry);
		}
		
		if( countDisplayed < entrys.size()){
			CalendarImageBundle imgBundle = (CalendarImageBundle) GWT.create(CalendarImageBundle.class);
			Image leftArrow = imgBundle.upArrow().createImage();
			Image rightArrow = imgBundle.downArrow().createImage();
			HorizontalPanel hp =  new HorizontalPanel();

			HTML sep = new HTML();
			sep.setWidth("100px");
			hp.add(leftArrow);
			hp.add(sep);
			hp.add(rightArrow);
			hp.setStyleName(XConfig.CalendarEventScrollStyleName);

			leftArrow.addClickListener(new ClickListener(){
				public void onClick(Widget arg0) {
						prevElements();	
						Calendar.getGuiManager().removeClickEvent();
				}
			});
			
			rightArrow.addClickListener(new ClickListener(){

				public void onClick(Widget arg0) {
						nextElements();			
						Calendar.getGuiManager().removeClickEvent();
				}
			});
			mainPanel.add(hp);
		}
	}
	
	private void nextElements(){
		if( index + countDisplayed < entrys.size()){
			index++;
			draw();
		}
	}
	
	private void prevElements(){
		if( index > 0 ){
			index--;
			draw();
		}
	}
	
	private int getMaxEntrys(){
		switch(CalendarWidget.getCurrentView()){
		case 0:
			return Math.min(XConfig.CalendarMaxDayEntrys, entrys.size());
		case 1: 
			return Math.min(XConfig.CalendarMaxWeekEntrys, entrys.size());
		case 2:
			return Math.min(XConfig.CalendarMaxMonthEntrys, entrys.size());
		default :	
			return -1;
		}
	}
	
	private void initScrollEntrys(){
		if( CalendarWidget.getCurrentView() != currentView || 
			countDisplayed != getMaxEntrys())
		{	
			countDisplayed = getMaxEntrys();
			currentView = CalendarWidget.getCurrentView();
			index = 0;
		}
	}

	public void addEntry(XEvent entry){
		entrys.add(entry);
		draw();
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date date) {
		this.startDate = date;
	}
	
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void onClick(Widget arg0) {
		HTML entry = (HTML) arg0;
		for(int i = 0; i < entrys.size(); i++){
			XEvent ev = (XEvent)entrys.get(i);
			if( ev.getRef() == entry ){
				Calendar.getGuiManager().onEventEntry_Click(entry, ev);
				break;
			}
		}	
	}

	public void removeEntry(XEvent event) {
		for(int i = 0; i < entrys.size(); i++){
			XEvent ev = (XEvent)entrys.get(i);
			if( ev.getName() == event.getName() ){
				entrys.remove(i);
				draw();
				break;
			}
		}
	}	

}
