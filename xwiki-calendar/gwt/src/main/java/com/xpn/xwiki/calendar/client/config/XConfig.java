package com.xpn.xwiki.calendar.client.config;

/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 *
 */
public class XConfig {
	
	
	public XConfig(){}
	
	public static final String CATEGORY_CLASS_NAME = "XWiki.XCalendarEventCategory";
	public static final String CATEGORY_DOCUMENT_NAME = "XWiki.XCalendarEventCategorys";
	public static final String CATEGORY_FIELD_NAME = "Name";
	public static final String CATEGORY_FIELD_DESC = "Description";
	
	public static final String EVENT_CLASS_NAME = "XWiki.CalendarEvent";
	public static final String EVENT_CLASS_SHEET = "XWiki.XCalendarEventSheet";
	
	public static final String EVENT_FILED_TITLE = "title";
	public static final String EVENT_FILED_DESCRIPTION = "description";
	public static final String EVENT_FILED_NAME = "name";
	public static final String EVENT_FILED_STARTDATE = "startDate";
	public static final String EVENT_FILED_ENDDATE = "endDate";
	public static final String EVENT_FILED_AUTHOR = "user";
	public static final String EVENT_FILED_LOCATION = "location";
	public static final String EVENT_FILED_URL = "url";	
	public static final String EVENT_FILED_CATEGORY = "category";
	public static final String EVENT_FILED_AGENDA = "agenda";
	public static final String EVENT_FILED_USER = "user";
	
	
	public static final String AGENDA_CLASS_NAME = "XWiki.XAgenda";
	public static final String AGENDA_CLASS_SHEET = "XWiki.XAgendaSheet"; //todo : create the sheet document
	public static final String AGENDA_FIELD_TITLE = "Name";
	public static final String AGENDA_FIELD_DESCRIPTION = "Description";
	public static final String AGENDA_FIELD_AUTHOR = "Author";
	public static final String AGENDA_FIELD_NAME = "Name";
	public static final String AGENDA_FIELD_VISIBILITY = "Visibility";
	
	public static final String VIEW_CLASS_NAME = "XWiki.XCalendarView";
	public static final String VIEW_DOCUMENT_NAME = "XWiki.XCalendarViews";
	
	public static final String VIEW_FIELD_NAME = "Name";
	public static final String VIEW_FIELD_TIMESCALE = "TimeScale";
	public static final String VIEW_FIELD_STARTAT = "StartAt";
	public static final String VIEW_FIELD_ENDAT = "EndAt";
	public static final String VIEW_FIELD_ACTIVE = "Active";
	
	public static final String CalendarSpaceParam = "calendarspace";
	public static final String CalendarRessourceParam = "resourcepath";
	
	public static final String CalendarViewLabels[] = {"Jour", "Semaine", "Mois", "Mon planning"};
	public static final String CalendarViewIDs[] = {"day", "week", "month", "myplanning"};
	
	public static final String DefaultLabelWidth = "40px";
	public static final String DefaultMaxWidth   = "100%";
	public static final int    CalendarStartTime = 0;
	public static final int    CalendarEndTime   = 24;
	
	public static final String CalendarTimeFormat = "dd/MM/yyyy HH:mm:ss";//"EEE MMM dd HH:mm:ss yyyy";
	public static final String CalendarTimeSimpleFormat = "dd/MM/yyyy HH:mm:ss";
	public static final String CalendarTimeSQLFormat = "yyyy-MM-dd HH:mm:ss";
	
	public static final int    CalendarStartHours = 0;
	public static final int    CalendarEndHours = 23;
	
	public static final int	   CalendarMaxMonthEntrys = 5;
	public static final int    CalendarMaxWeekEntrys = 2;
	public static final int    CalendarMaxDayEntrys = 2;
	
	//************************************************************************************ CSS classes
	
	public static final String CalendarWidgetStyleName = "xgwt-calendar-widget";
	public static final String CalendarGridWidgetStyleName = "xgwt-grid-widget";
	public static final String CalendarGridPanelStyleName = "xgwt-grid-panel";
	public static final String CalendarGridTableStyleName = "xgwt-grid-table";
	public static final String CalendarHeadGridPanelStyleName = "xgwt-headgrid-panel";
	public static final String CalendarDayscrollWidgetStyleName = "xgwt-dayscroll-widget";
	
	public static final String CalendarTimeSlotStyleName = "xgwt-dayslot-widget";
	public static final String CalendarTodayTimeSlotStyleName = "xgwt-todayslot-widget";
	public static final String CalendarTimeSlotHeadStyleName = "xgwt-dayslot-head";
	public static final String CalendarTodayTimeSlotHeadStyleName = "xgwt-todayslot-head";
	
	public static final String CalendarTimeSlotEntryStyleName = "xgwt-dayslot-entry";
	public static final String CalendarTimeSlotTodayEntryStyleName = "xgwt-todayslot-widget";
	
	public static final String CalendarCreateEventDialogStyleName = "xgwt-create-event-dialog";
	public static final String CalendarEventDisplayDialogStyle = "xgwt-event-display-dialog";
	public static final String CalendarEventDisplayButtonPanelStyleName = "xgwt-event-button-panel-dialog";
	public static final String CalendarEventDisplayTitleStyleName = "gwt-Label-title";
	public static final String CalendarEventLabelFieldStyleName = "gwt-Label-Field";
	
	public static final String CalendarDayHeadStyleName = "xgwt-dayhead";
	public static final String CalendarToDayHeadStyleName = "xgwt-todayhead";
	public static final String CalendarTimeScaleStyleName = "xgwt-timescale";
	
	public static final String CalendarMainGridStyleName = "xgwt-maingrid";
	public static final String CalendarHeadGridStyleName = "xgwt-headgrid";
	public static final String CalendarMainTableStyleName = "xgwt-maintable";
	
	public static final String CalendarTodayLabelStyleName = "xgwt-today-label";
	public static final String CalendarTimeScrollImgStyleName = "xgwt-timescoll-img";
	
	public static final String CalendarFormStyleName = "xgwt-formentry";
	public static final String CalendarFormTitleStyleName = "xgwt-formentry-title";
	public static final String CalendarFormFieldStyleName = "xgwt-formentry-field";
	
	public static final String CalendarEventScrollStyleName = "xgwt-eventscroll";
	
	public static final String CalendarMyPlanningEventPanelStyleName = "xgwt-myplanning-events-panel";
	public static final String CalendarMyPlanningEventEntryStyleName = "xgwt-myplanning-event";
	public static final String CalendarMyPlanningEventHeadStyleName  = "xgwt-myplanning-event-head";
	
	
	
}
