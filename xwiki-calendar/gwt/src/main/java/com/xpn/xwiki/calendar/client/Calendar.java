package com.xpn.xwiki.calendar.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.calendar.client.data.*;
import com.xpn.xwiki.calendar.client.ui.*;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTDefaultApp;

/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 * 
 */
public class Calendar extends XWikiGWTDefaultApp implements EntryPoint {

	private static Calendar instance;
	private static CalendarWidget cui;
	private static XDataManager dataManager;
	private static XCacheManager cacheManager;
	private static XCalendar dateManager;
	private static String user;
	private static String space;
	private static List eventCategorys = new ArrayList();
	private static List agendaList = new ArrayList();
	
	public static String getUser() {
		return user;
	}
	
	public static String getSpace() {
		return space;
	}

	public static XCalendar getDateManager() {
		return dateManager;
	}

	public static XCacheManager getCacheManager() {
		return cacheManager;
	}

	public static void setCacheManager(XCacheManager cacheManager) {
		Calendar.cacheManager = cacheManager;
	}

	public static Calendar getInstance() {
		return instance;
	}

	public static CalendarWidget getGuiManager() {
		return cui;
	}
	
	public Calendar(){
	}

	public void onModuleLoad() {
		instance = this;
		space = getParam(XConfig.CalendarSpaceParam, "Calendar");
		initialize();
		RootPanel.get("Calendar").add(cui);
	}

	private void initialize() {
		dateManager = new XCalendar();
		dataManager = new XDataManager();
		cacheManager = new XCacheManager(dataManager);
		loadCategorys();
		loadAgendaList();
		loadUser();
		cui = new CalendarWidget();
	}
	
	public void loadUser(){
		getXWikiServiceInstance().getUser(new AsyncCallback(){

			public void onFailure(Throwable arg0) {
				Window.alert("failed to get user");
			}

			public void onSuccess(Object arg0) {
				user = ((Document) arg0).getFullName();
			}
		});
	}

	public void loadCategorys() {
		dataManager.getCategorys(new AsyncCallback() {
			public void onFailure(Throwable arg0) {
				Window.alert("Failed to load Event-Categorys");
			}

			public void onSuccess(Object arg0) {
				setCategorys((List) arg0);
			}
		});
	}
	
	public void loadAgendaList(){
		dataManager.getAgendas(new AsyncCallback(){
			public void onFailure(Throwable arg0) {
				Window.alert("Failed to load Agenda list");
			}
			public void onSuccess(Object arg0) {
				List lAgenda = (ArrayList) arg0;
				setAgendaList(lAgenda);
			}
		});
	}
	
//	public void LoadUserEvents(){
//		long from = 0;
//		long to = 0;
//
//		XCalendar xc = new XCalendar(getDateManager());
//		xc.resetFromMonthStart();
//		from = xc.getDate().getTime();
//		xc.nextMonth();
//		to = xc.getDate().getTime();
//		getCacheManager().LoadEvents(from, to, new AsyncCallback(){
//
//			public void onFailure(Throwable arg0) {
//				Window.alert("failed to load events");
//			}
//
//			public void onSuccess(Object result) {
//				//List events = (List) result;
//				//Window.alert( "there is " + events.size() + " on the db!");
//			}
//			
//		});
//	}
	
	public void LoadUserEvents(int from, int to, final AsyncCallback cb){
		getCacheManager().LoadEvents(from, to, cb );
	}

	public List getCategorys() {
		return eventCategorys;
	}

	protected void setCategorys(List categorys) {
		eventCategorys = categorys;
	}

	public static List getAgendaList() {
		return agendaList;
	}

	protected static void setAgendaList(List agenda) {
		agendaList = agenda;	
	}
	
	public static XDataManager getDataManager() {
		return dataManager;
	}
}
