package com.xpn.xwiki.calendar.client.ui;

import java.util.Date;

import org.gwtwidgets.client.util.SimpleDateFormat;


import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.calendar.client.Calendar;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.calendar.client.data.*;

/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 *
 */
public class TimeGridWidget extends Composite { 
	
	private VerticalPanel 	mainPanel;
	private SimplePanel     gridPanel;
	private SimplePanel     headerPanel;
	private Grid        	mainGrid;
	private Grid			headerGrid;
	private FlexTable 		mainTable;
	private Calendar        calendar;
	private XCalendar 		dtm;
	private int 			gridRowCount;
	private int 			gridColCount;
	
	private static int 		index = 0;
	
	public TimeGridWidget(){
		calendar = Calendar.getInstance();
		initialize();
	}
	
	public TimeGridWidget(int view, XCalendar manager){
		//Window.alert("helo from TimeGridWidget constructor");
		calendar = Calendar.getInstance();
		dtm = manager;
		mainPanel = new VerticalPanel();
		mainPanel.setWidth(XConfig.DefaultMaxWidth);
		initView(view);
		initWidget(mainPanel);
		setStyleName(XConfig.CalendarGridWidgetStyleName);
	}

	private void initialize() {
		mainPanel = new VerticalPanel();
		mainPanel.setWidth(XConfig.DefaultMaxWidth);
		gridRowCount = 24;
		gridColCount = 7;		
		initGrids();
		initWidget(mainPanel);
		setStyleName(XConfig.CalendarGridWidgetStyleName);
	}
	
	private void initView(int viewCode){
		switch(viewCode){
		case 0 : 					
			//day view 
			initDayView();
			break;
		case 1 : 					
			//week view
			initWeekView();
			break;
		case 2 : 					
			//month view	
			initMonthView();
			break;
		case 3 : 					
			//my planning view
			initMyPlanningView();
			break;
		default : return;
		}
	}
	
	private void initMyPlanningView() {
		gridRowCount = 1;
		gridColCount = 1;
		mainTable = new FlexTable();
		mainTable.setStyleName(XConfig.CalendarMainTableStyleName);
		gridPanel = new SimplePanel();
		gridPanel.setStyleName(XConfig.CalendarGridTableStyleName);
		gridPanel.add(mainTable);
		mainTable.setCellSpacing(0);		
		mainPanel.add(gridPanel);
	}
	
	public void addNewDayLine(String dayName){
		if( mainTable != null ){
			int row = mainTable.getRowCount();
			
			VerticalPanel panel = new VerticalPanel();
			panel.setStyleName(XConfig.CalendarMyPlanningEventPanelStyleName);
			panel.setSpacing(2);
			Label lDay = new Label(dayName);
			lDay.setStyleName(XConfig.CalendarMyPlanningEventHeadStyleName);
			mainTable.setWidget(row, 0, lDay);
			mainTable.setWidget(row, 1, panel);
			
		}
	}
	
	public HTML addDayEventLine(int row, Date d, String object){
		if( mainTable != null ){
			VerticalPanel panel = (VerticalPanel) mainTable.getWidget(row, 1);
			SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
			if( panel != null ){
				HTML hEvent = new HTML(fmt.format(d) + " " + object);
				hEvent.setStyleName(XConfig.CalendarMyPlanningEventEntryStyleName + "-" + (index%2));
				index++;
				panel.add(hEvent);
				return hEvent;
			}
			else
			{
				Window.alert("failed to load Panel");
			}
			
		}
		return null;
	}
	
	public FlexTable getFlexTable(){
		return mainTable;
	}

	private void initMonthView() {
		gridRowCount = 5;
		gridColCount = 7;
		HTML hd;
		initGrids();
		XCalendar cal = new XCalendar(dtm);
		cal.resetFromMonthStart();
		
		float cellWidth = 100 / gridColCount;
		String width = String.valueOf(cellWidth) + "%";
		for(int i = 0; i < gridColCount; i++){
			hd = new HTML(cal.getShortDayName(i));
			hd.setStyleName(XConfig.CalendarDayHeadStyleName);
			headerGrid.setWidget(0, i, hd);
			headerGrid.getCellFormatter().setWidth(0, i, width);
			//headerGrid.getCellFormatter().setStyleName(0, i, XConfig.CalendarDayHeadStyleName);
		}
		
		for(int i = 0; i < gridRowCount; i++){
			for(int j = 0; j < gridColCount; j++){
				
				String TimeSlot = XConfig.CalendarTimeSlotStyleName;
				
				if( cal.isToday()){
					TimeSlot = XConfig.CalendarTodayTimeSlotStyleName;
				}
				
				mainGrid.getCellFormatter().setWidth(i, j, width);
				mainGrid.getCellFormatter().setHeight(i, j, "100px");
				mainGrid.getCellFormatter().setStyleName(i, j, TimeSlot);
					
				Date sd = cal.getDate();
				Date ed = cal.getDayRange();
				
				TimeSlotWidget tsw = new TimeSlotWidget(sd, ed, true);
				mainGrid.setWidget(i, j, tsw);
				cal.nextDay();
			}
		}		
	}

	private void initDayView(){
		gridRowCount = XConfig.CalendarEndHours - XConfig.CalendarStartHours + 1;
		gridColCount = 2;
		initGrids();
		XCalendar cal = new XCalendar(dtm);
		cal.resetFromDayStart();
		
		HTML hd = new HTML(dtm.getLongDayName());
		
		float cellWidth = 96 / (gridColCount-1);
		String width = String.valueOf(cellWidth) + "%";
		
		String TimeSlot = XConfig.CalendarTimeSlotStyleName;
		String headSlot = XConfig.CalendarDayHeadStyleName;
		
		if( cal.isToday()){
			TimeSlot = XConfig.CalendarTodayTimeSlotStyleName;
			headSlot = XConfig.CalendarToDayHeadStyleName;
		}
		
		headerGrid.getCellFormatter().setWidth(0, 0, "4%");
		headerGrid.setWidget(0, 1, hd);	
		//headerGrid.getCellFormatter().setStyleName(0, 1, headSlot);
		hd.setStyleName(headSlot);
		int row = 0;
		for(int i = XConfig.CalendarStartHours; i <= XConfig.CalendarEndHours; i++){
			cal.setHours(i);
			mainGrid.getCellFormatter().setWidth(row, 0, "4%");
			mainGrid.setText(row, 0, cal.fHoursMinutes.format(cal.getDate()));
			mainGrid.getCellFormatter().setStyleName(row, 0, XConfig.CalendarTimeSlotHeadStyleName);
			mainGrid.getCellFormatter().setWidth(row, 1, width);
			//TODO verify the style of the time head......
		    mainGrid.getCellFormatter().setStyleName(row, 1, TimeSlot);
		
			Date sd = cal.getDate();
			Date ed = cal.getHourRange();
			TimeSlotWidget tsw = new TimeSlotWidget(sd, ed, false);
			mainGrid.setWidget(row, 1, tsw);
			row++;
		}		
	}
	
	private void initWeekView(){
		gridRowCount = XConfig.CalendarEndHours - XConfig.CalendarStartHours + 1;
		gridColCount = 8;
		HTML hd;
		initGrids();
		XCalendar tempCal = new XCalendar(dtm);
		tempCal.resetFromWeekStart();
		
		float cellWidth = 96 / (gridColCount-1);
		String width = String.valueOf(cellWidth) + "%";
		
		String TimeSlot = "";
		String headSlot = "";
		
		//setup the header grid
		for(int i = 0; i < gridColCount; i++){
			if( i == 0 ) {
				headerGrid.getCellFormatter().setWidth(0, 0, "4%");
			}else{
				//TODO verify the days order in the XCalendar
				headSlot = XConfig.CalendarDayHeadStyleName;
				
				if( tempCal.isToday()){
					headSlot = XConfig.CalendarToDayHeadStyleName;
				}
				hd = new HTML(tempCal.getDayAndMonth());
				hd.setStyleName(headSlot);
				headerGrid.setWidget(0, i, hd);
				headerGrid.getCellFormatter().setWidth(0, i, width);
				tempCal.nextDay();
			}
		}

		tempCal = new XCalendar(dtm);
		tempCal.resetFromWeekStart();
		//setup the main grid
		for(int j = 0; j < gridColCount; j++){
			if( j > 0 ){
				XCalendar cal = new XCalendar(tempCal);
				
				if( cal.isToday()){
					TimeSlot = XConfig.CalendarTodayTimeSlotStyleName;
				}else{
					TimeSlot = XConfig.CalendarTimeSlotStyleName;
				}
				int row = 0;
				for(int i = XConfig.CalendarStartHours; i <= XConfig.CalendarEndHours; i++){
					cal.setHours(i);
					mainGrid.getCellFormatter().setWidth(row, j, width);
					mainGrid.getCellFormatter().setStyleName(row, j, TimeSlot);
					Date sd = cal.getDate();
					Date ed = cal.getHourRange();
					TimeSlotWidget tsw = new TimeSlotWidget(sd, ed, false);
					mainGrid.setWidget(row, j, tsw);
					row++;
				}
				tempCal.nextDay();
			}
			else
			{
				XCalendar cal = new XCalendar(tempCal);
				
				mainGrid.getColumnFormatter().setStyleName(0, XConfig.CalendarTimeSlotHeadStyleName);
				mainGrid.getColumnFormatter().setWidth(0, "4%");
				int row = 0;
				for(int i = XConfig.CalendarStartHours; i <= XConfig.CalendarEndHours; i++){		
					cal.setHours(i);
					mainGrid.setText(row, 0, cal.fHoursMinutes.format(cal.getDate()));
					mainGrid.getCellFormatter().setStyleName(row, 0, XConfig.CalendarTimeSlotHeadStyleName);
					row++;
				}
			}
		}		
	}
	
	
	private void initGrids(){
		headerGrid = new Grid(2, gridColCount);
		mainGrid = new Grid(gridRowCount, gridColCount);
		
		mainGrid.setStyleName(XConfig.CalendarMainGridStyleName);
		headerGrid.setStyleName(XConfig.CalendarHeadGridStyleName);
		
		headerPanel = new SimplePanel();
		headerPanel.setStyleName(XConfig.CalendarHeadGridPanelStyleName);
		
		gridPanel = new SimplePanel();
		gridPanel.setStyleName(XConfig.CalendarGridPanelStyleName);
		
		gridPanel.add(mainGrid);
		headerPanel.add(headerGrid);
		
		mainGrid.setCellSpacing(0);
		headerGrid.setCellSpacing(0);
		
		mainPanel.add(headerPanel);
		mainPanel.add(gridPanel);
	}
	
	public void setHeaderCellText(int row, int col, String text){
		if( row >= 2 || col >= gridColCount ) return;
		headerGrid.setText(row, col, text);
	}
	
	public void setHeaderCellWidget(int row, int col, Widget widget){
		if( row >= 2 || col >= gridColCount ) return;
		headerGrid.setWidget(row, col, widget);		
	}
	
	public void setHeaderCellWidth(int row, int col, String width){
		if( row >= 2 || col >= gridColCount ) return;
		headerGrid.getCellFormatter().setWidth(row, col, width);		
	}
	
	public void setTimeSlotCellText(int row, int col, String text){
		if( row >= 2 || col >= gridColCount ) return;
		mainGrid.setText(row, col, text);
	}
	
	public void setTimeSlotCellWidget(int row, int col, Widget widget){
		if( row >= 2 || col >= gridColCount ) return;
		mainGrid.setWidget(row, col, widget);		
	}	
	
	public void setTimeSlotCellWidth(int row, int col, String width){
		if( row >= 2 || col >= gridColCount ) return;
		mainGrid.getCellFormatter().setWidth(row, col, width);		
	} 
	
	public int getGridRowCount() {
		return gridRowCount;
	}

	public int getGridColCount() {
		return gridColCount;
	}
	
	public Grid getMainGrid() {
		return mainGrid;
	}

	public Grid getHeaderGrid() {
		return headerGrid;
	}
	
	public void addMainTableListener(TableListener listener){
		mainGrid.addTableListener(listener);
	}
	
	public void addHeadTableListener(TableListener listener){
		headerGrid.addTableListener(listener);
	}
	
	public void onCellClicked(SourcesTableEvents sender, int row, int col) {
		// TODO Auto-generated method stub
		if(sender == mainGrid){
			Widget w = mainGrid.getWidget(row, col);
			if( w != null ){
				TimeSlotWidget tsw = (TimeSlotWidget) w;
				new CreateEventDialog(tsw.getStartDate(), tsw.getEndDate()).show();
				
			}
			
		}else if(sender == headerGrid){
			
		}		
	}
	

}
