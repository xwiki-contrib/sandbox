package com.xpn.xwiki.calendar.client.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xpn.xwiki.calendar.client.Calendar;

public class XCacheManager {
	
	private static long from = 0;
	private static long to   = 0;
	private static boolean loaded = false;
	private static List eventList = new ArrayList();
	private static XDataManager dataManager;
	
	public static List getEventList() {
		return eventList;
	}

	public static void setEventList(List eventList) {
		XCacheManager.eventList = eventList;
	}
	
	public XCacheManager(){
	}
	
	public XCacheManager(XDataManager dtm){
		dataManager = dtm;
	}
	
	public void LoadEvents(final long from, final long to, final AsyncCallback cb){
		
		if( loaded == false || this.from > from || this.to < to ){
			this.from = from; 
			this.to = to;		
			dataManager.getUserEvents(Calendar.getUser(), from, to, new AsyncCallback(){

				public void onFailure(Throwable arg0) {
					Window.alert("failed to load events from cache!");
				}
				public void onSuccess(Object arg0) {
					eventList = (List) arg0;
					List events = new ArrayList();
					setupInternalData(from, to, true);
					
					for(int i = 0; i < eventList.size(); i++){
						XEvent ev = (XEvent) eventList.get(i);
						events.add(ev);
					}	
					cb.onSuccess(eventList);
				}
			});
		}else{
			List events = new ArrayList();
			for(int i = 0; i < eventList.size(); i++){
				XEvent ev = (XEvent)eventList.get(i);
				long evTime = Date.parse(ev.getStartDate());
				if( evTime >= from && evTime <= to ){
					events.add(ev);
				}
			}
			cb.onSuccess(events);			
		}
	}
	
	public void addEvent(XEvent event){
		if( event != null ){
			eventList.add(event);
		}
	}
	
	public void removeEvent(XEvent event){
		for(int i = 0; i < eventList.size(); i++){
			XEvent ev = (XEvent) eventList.get(i);
			if( ev.getName() == event.getName()){
				eventList.remove(i);
				break;
			}
		}
	}
	
	public void flush(){
		loaded = false;
		eventList.clear();
	}
	
	public void setupInternalData(long from, long to, boolean loaded){
		this.from = from;
		this.to = to;
		this.loaded = loaded;
	}

}
