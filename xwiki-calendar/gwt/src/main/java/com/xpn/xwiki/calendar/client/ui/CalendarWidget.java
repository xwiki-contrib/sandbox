package com.xpn.xwiki.calendar.client.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.gwtwidgets.client.util.SimpleDateFormat;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.calendar.client.Calendar;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.calendar.client.data.*;

/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 *
 */
public class CalendarWidget extends Composite implements TabListener, TableListener {

	private Calendar 		 calendar;
	private DockPanel 		 mainPanel;
	private TimeGridWidget   timeGrid;
	private ViewTabWidget    viewTab;
	private HorizontalPanel  topPanel;
	private TimeScrollWidget timeScroll;
	
	private static int currentView = 1;     	
	private int tgRowCount;
	private int tgColCount;
	
	private static Map eventPosMap = new HashMap();
	
	private boolean treatClickEvent = true;
	
	
	public boolean isTreatClickEvent() {
		return treatClickEvent;
	}

	public void setTreatClickEvent(boolean treatClickEvent) {
		this.treatClickEvent = treatClickEvent;
	}

	public CalendarWidget(){
		calendar = Calendar.getInstance();
		initialize();	
	}

	private void initialize() {
		mainPanel = new DockPanel();
		mainPanel.setWidth(XConfig.DefaultMaxWidth);
		initViewTabWidget();
		//setView(0);
		viewTab.selectTab(0);
		mainPanel.add(topPanel, mainPanel.NORTH);
		initWidget(mainPanel);
		setStyleName(XConfig.CalendarWidgetStyleName);
	}
	
	private void initTimeGridWidget(){
		timeGrid = new TimeGridWidget(currentView, Calendar.getDateManager());
		if( currentView != 3 ){
			timeGrid.addHeadTableListener(this);
			timeGrid.addMainTableListener(this);			
		}else{
			
		}
	}
	
	private void initTimeScrollWidget(){
		String dt;
		switch(currentView){
		case 0:   	//day view mode
			dt = Calendar.getDateManager().getDayAndMonth() + " " + Calendar.getDateManager().getYear();
			timeScroll.getTodayLabel().setText(dt);
			break;
		case 1: 	//week view mode
			XCalendar c1 = new XCalendar(Calendar.getDateManager());
			c1.resetFromWeekStart();
			XCalendar c2 = new XCalendar(c1);
			c2.endofWeek();
			dt = c1.getNumericDayMonth() + " au " + c2.getNumericDayMonth() + " " + c2.getYear();
			timeScroll.getTodayLabel().setText(dt);
			break;
		case 2: 	//month view mode
			dt = Calendar.getDateManager().getLongMonthName()+ " " + Calendar.getDateManager().getYear();
			timeScroll.getTodayLabel().setText(dt);
			break;
		case 3:
			dt = Calendar.getDateManager().getYear() + " ";
			timeScroll.getTodayLabel().setText(dt);
			break;
		default:
		}
	}
	
	public XCalendar getXCalendar() {
		return Calendar.getDateManager();
	}

	private void initViewTabWidget(){
		viewTab = new ViewTabWidget();
		viewTab.addTabListener(this);		
		
		topPanel = new HorizontalPanel();
		topPanel.setWidth("100%");
		
		timeScroll = new TimeScrollWidget(currentView);
		timeScroll.addNextClickListener(new ClickListener(){
			public void onClick(Widget arg0) {
				timescroller_NextClickListener(arg0);
			}
		});
		
		timeScroll.addPrevClickListener(new ClickListener(){
			public void onClick(Widget arg0) {
				timescroller_PrevClickListener(arg0);
			}
		});
				
		topPanel.add(timeScroll);
		topPanel.add(viewTab);
		topPanel.setCellWidth(timeScroll, "80%");
	}
	
	private void setView(int view){
		if( view < 0 || view >= XConfig.CalendarViewIDs.length ) return;
		currentView = view;
		if( timeGrid != null ) mainPanel.remove(timeGrid);
		initTimeGridWidget();
		initTimeScrollWidget();
		mainPanel.add(timeGrid, mainPanel.CENTER);
		loadEvents();
	}
	
	private TimeRange getCalendarRange(){
		XCalendar xc = new XCalendar(Calendar.getDateManager());
		long from = 0, to = 0;
		
		switch(currentView){
		case 0:
			xc.resetFromDayStart();
			from = xc.getDate().getTime();
			to  = xc.getDayRange().getTime();
			break;
		case 1:
			xc.resetFromWeekStart();
			from = xc.getDate().getTime();
			to  = xc.getWeekRange().getTime();
			break;
		case 2: 
			xc.resetFromMonthStart();
			from = xc.getDate().getTime();
			xc.add(xc.DAY_OF_MONTH, 35);
			to = xc.getDate().getTime();
			break;
		case 3: 
			xc.resetFromYearStart();
			from = xc.getDate().getTime();
			to = xc.getYearRange();
			break;
			
		default : 
			return null;
		}
		
		return new TimeRange(from, to);
	}
	
	private void loadEvents(){
		
		TimeRange range = getCalendarRange();	
		eventPosMap.clear();
		Calendar.getCacheManager().LoadEvents(range.getFrom(), range.getTo(), new AsyncCallback(){

			public void onFailure(Throwable arg0) {
				Window.alert("failed to load events");
			}

			public void onSuccess(Object result) {
				List events = (List) result;
				if(currentView != 3){
					for(int i = 0; i < events.size(); i++){
						XEvent ev = (XEvent) events.get(i);
						EventPosition evPos = getEventPosition(ev);
						eventPosMap.put(ev.getName(), evPos);
						putEventOnTimeGrid(ev);
					}
				}else{
					putEventsOnTabel(events);
				}				
			}
			
		});
	}
	
	private void putEventsOnTabel(List events){
		int  i = 0, cRow = -1;
		Date oldEvDate = new Date(0);
		SimpleDateFormat fmt = new SimpleDateFormat(XConfig.CalendarTimeFormat);
		while( i < events.size()){
			XEvent event = (XEvent) events.get(i);
			Date evDate = new Date(Date.parse(event.getStartDate()));
			//Window.alert("date parssed "+ event.getStartDate());
			
			if( oldEvDate.getYear() != evDate.getYear() ||
				oldEvDate.getMonth() != evDate.getMonth() ||
				oldEvDate.getDate() != evDate.getDate())
			{
				cRow++;
				XCalendar c = new XCalendar(evDate);
				String dayName = c.getDayAndMonth();
				timeGrid.addNewDayLine(dayName);
				HTML hEvent = timeGrid.addDayEventLine(cRow, evDate, event.getObject() + "-" + event.getLocation());
				oldEvDate = evDate;
			}
			else
			{
				HTML hEvent = timeGrid.addDayEventLine(cRow, evDate, event.getObject() + "-" + event.getLocation());
			}					
			i++;
		}
	}
	
	private EventPosition getEventPosition(XEvent event){
		
		XCalendar xc = new XCalendar(Calendar.getDateManager());
		int  i = 0, j = 0;
		long timeRange, 
			 calTime, 
			 evTime, 
			 index;
				
		switch(currentView){
		case 0:
			xc.resetFromDayStart();
			//xc.setHours(XConfig.CalendarStartHours);
			timeRange = 0x36EE80;
			calTime = xc.getDate().getTime();
			evTime = Date.parse(event.getStartDate());
			i = (int) Math.ceil((evTime - calTime) / timeRange);
			j = 1;
			break;
		case 1:
			timeRange = 0x5265C00;
			xc.resetFromWeekStart();
			//xc.setHours(XConfig.CalendarStartHours);
			calTime = xc.getDate().getTime();
			evTime = Date.parse(event.getStartDate());
			index = (evTime - calTime) / timeRange;
			j = (int) Math.ceil(((evTime - calTime) / timeRange)) + 1;
			i = (int) Math.ceil((evTime - calTime) % timeRange) / 0x36EE80;
			break;
		case 2:
			timeRange = 0x5265C00;
			xc.resetFromMonthStart();
			calTime = xc.getDate().getTime();
			evTime = Date.parse(event.getStartDate());
			index = (evTime - calTime) / timeRange;
			i = (int) index / 7;
			j = (int)(index % 7);
			break;
		case 3:
			return null;
		}
		
		return new EventPosition(i, j);
	}
	
	private void putEventOnTimeGrid(XEvent event){
		TimeSlotWidget tsw;
		EventPosition evPos = (EventPosition) eventPosMap.get(event.getName());
		if(evPos != null){
			tsw = (TimeSlotWidget)timeGrid.getMainGrid().getWidget(evPos.getRow(), evPos.getCol());
			if( tsw !=  null ){ 
				tsw.addEntry(event);
			}
		}
	}
	
	private void removeEventFromTimeGrid(XEvent event){
		TimeSlotWidget tsw;		
		EventPosition evPos = (EventPosition) eventPosMap.get(event.getName());
		if(evPos != null){
			tsw = (TimeSlotWidget)timeGrid.getMainGrid().getWidget(evPos.getRow(), evPos.getCol());
			if( tsw != null) {
				tsw.removeEntry(event);
				eventPosMap.remove(event.getName());
			}
		}
	}

	public void removeClickEvent(){
		setTreatClickEvent(false);		
	}

	public void deleteEvent(final XEvent event){
		Calendar.getDataManager().removeEvent(event, new AsyncCallback(){

			public void onFailure(Throwable arg0) {
				Window.alert("failed to remove event!");
			}

			public void onSuccess(Object arg0) {
				Calendar.getCacheManager().removeEvent(event);
				removeEventFromTimeGrid(event);
			}
			
		});
	}
	
	public static int getCurrentView() {
		return currentView;
	}

	public static void setCurrentView(int currentView) {
		CalendarWidget.currentView = currentView;
	}
	
	/****************************************************************
	/* EVENT'S HANDLERS
	/***************************************************************
	/*/

	public boolean onBeforeTabSelected(SourcesTabEvents arg0, int tabIndex) {
		if( tabIndex != currentView ){
			return true;
		}
		
		return false;
	}
	
	public void onTabSelected(SourcesTabEvents arg0, int tabIndex) {
		setView(tabIndex);
	}

	public void onCellClicked(SourcesTableEvents sender, int row, int col) {
		if( treatClickEvent ){
			if( sender.equals(timeGrid.getMainGrid()))
			{
				final Widget w = timeGrid.getMainGrid().getWidget(row, col);
				if( w != null ){
					final TimeSlotWidget tsw = (TimeSlotWidget) w;
					final CreateEventDialog dialog = new CreateEventDialog(tsw.getStartDate(), tsw.getEndDate(), currentView);
					dialog.setAgendaList(calendar.getAgendaList());
					dialog.addClickListener(new ClickListener(){
	
						public void onClick(Widget arg0) {
							onCreateEvent_OkBtnClick(dialog, arg0, tsw);
						}
					});
					dialog.addDetailClickListener(new ClickListener(){
						public void onClick(Widget sender){
							onCreateEvent_DetailBtnClick(dialog, sender);
						}
					});			
				}
			}
		}
		else
		{
			setTreatClickEvent(true);
		}
	}	
	
	public void onCreateEvent_Request(final TimeSlotWidget tsw){
		
		final CreateEventDialog dialog = new CreateEventDialog(tsw.getStartDate(), tsw.getEndDate(), currentView);
		dialog.setAgendaList(calendar.getAgendaList());
		dialog.addClickListener(new ClickListener(){

			public void onClick(Widget arg0) {
				onCreateEvent_OkBtnClick(dialog, arg0, tsw);
			}
		});
		dialog.addDetailClickListener(new ClickListener(){
			public void onClick(Widget sender){
				onCreateEvent_DetailBtnClick(dialog, sender);
			}
		});
	}
	
	public void onEventEntry_Click(final Widget w, final XEvent ev){
		removeClickEvent();
		
		final EventDisplayDialog eventDlg = new EventDisplayDialog(ev);
		eventDlg.addCloseButtonClickListener(new ClickListener(){

			public void onClick(Widget arg0) {
				eventDlg.hide();
			}			
		});
		
		eventDlg.addDeleteButtonClickListener(new ClickListener(){

			public void onClick(Widget arg0) {
				final CalendarEventDeleteConfirmation confirm = new CalendarEventDeleteConfirmation(ev);
				
				confirm.addCloseButtonClickListener(new ClickListener(){

					public void onClick(Widget arg0) {
						confirm.hide();
					}
				});
				
				confirm.addDeleteButtonClickListener(new ClickListener(){

					public void onClick(Widget arg0) {
						eventDlg.hide();
						confirm.hide();
						deleteEvent(ev);
					}
					
				});
			}
			
		});
		
		eventDlg.addUpdateButtonClickListener(new ClickListener(){

			public void onClick(Widget arg0) {
				eventDlg.hide();
				final CreateEventDetailDialog evUpdate = new CreateEventDetailDialog();				
				evUpdate.setEvent(ev);
				evUpdate.addOkClickListener(new ClickListener(){

					public void onClick(Widget arg0) {
						onUpdateEvent_ButtonClick(evUpdate, arg0, ev);
					}
				});
				
				evUpdate.addCancelClickListener(new ClickListener(){

					public void onClick(Widget arg0) {
						evUpdate.hide();
					}					
				});
			}			
		});
		
		eventDlg.show();
	}
	
	public void onUpdateEvent_ButtonClick(final CreateEventDetailDialog dialog, Widget arg0, final XEvent ev){
		dialog.hide();
		Calendar.getCacheManager().removeEvent(ev);
		removeEventFromTimeGrid(ev);
		ev.setObject(dialog.getObject());
		ev.setAgenda(dialog.getAgenda());
		ev.setAuthor(Calendar.getUser());
		ev.setAgenda(dialog.getAgenda());
		ev.setCategory(dialog.getCategory());
		ev.setDescription(dialog.getDescription());
		ev.setLocation(dialog.getLocation());
		
		SimpleDateFormat srcfmt = new SimpleDateFormat(XConfig.CalendarTimeSimpleFormat);
		SimpleDateFormat desfmt = new SimpleDateFormat(XConfig.CalendarTimeFormat);
		
		ev.setStartDate(desfmt.format(srcfmt.parse(dialog.getStartDate())));
		ev.setEndDate(desfmt.format(srcfmt.parse(dialog.getEndDate())));
		
		
		Calendar.getDataManager().updateEvent(ev, new AsyncCallback(){

			public void onFailure(Throwable arg0) {
				Window.alert("failed to update event");
			}

			public void onSuccess(Object arg0) {
				Calendar.getDataManager().getEvent((String) arg0, new AsyncCallback(){

					public void onFailure(Throwable arg0) {
						Window.alert("failed to load event after update");
					}

					public void onSuccess(Object arg0) {
						Calendar.getCacheManager().addEvent((XEvent) arg0);
						EventPosition evPos = getEventPosition((XEvent) arg0);
						eventPosMap.put(((XEvent)arg0).getName(), evPos);
						putEventOnTimeGrid((XEvent) arg0);
					}
				});
			}
		});
	}
	
	
	public void onDeleteEvent_ButtonClick(final CreateEventDetailDialog dialog, Widget arg0, XEvent ev){
		
	}
	
	public void onCreateEvent_OkBtnClick(final CreateEventDialog dialog, Widget arg0, TimeSlotWidget tsw){
		XEvent ev = new XEvent();
		ev.setObject(dialog.getObject());
		ev.setAgenda(dialog.getAgenda());
		ev.setAuthor(Calendar.getUser());
		SimpleDateFormat fmt = new SimpleDateFormat(XConfig.CalendarTimeFormat);
				
		ev.setStartDate(fmt.format(tsw.getStartDate()));
		ev.setEndDate(fmt.format(tsw.getEndDate()));			
						
		String pageName = Calendar.getDataManager().encodeEventPageName(ev, tsw.getStartDate().getTime(), tsw.getEndDate().getTime());
		ev.setName(pageName);
		
		Calendar.getDataManager().createEvent(ev, new AsyncCallback(){

			public void onFailure(Throwable arg0) {
				Window.alert("failed to create event");
			}

			public void onSuccess(Object arg0) {
				dialog.hide();				
				String pageName = (String)arg0;
				Calendar.getDataManager().getEvent(pageName, new AsyncCallback(){

					public void onFailure(Throwable arg0) {
						Window.alert("failed to create event");
					}

					public void onSuccess(Object arg0) {
						Calendar.getCacheManager().addEvent((XEvent) arg0);
						EventPosition evPos = getEventPosition((XEvent) arg0);
						eventPosMap.put(((XEvent)arg0).getName(), evPos);
						putEventOnTimeGrid((XEvent) arg0);
					}
				});
			}
		});
	}
	
	public void onCreateEvent_DetailBtnClick(final CreateEventDialog dialog, Widget sender){
		final Date sdt = dialog.getStartDate();
		final Date edt = dialog.getEndDate();
		String object = dialog.getObject();
		//TODO: create the method that set the agenda in the dialogbox.
		String agenda = dialog.getAgenda();
		dialog.hide();	
		final CreateEventDetailDialog dlg = new CreateEventDetailDialog();
		dlg.setObject(object);
		final SimpleDateFormat desfmt = new SimpleDateFormat(XConfig.CalendarTimeSimpleFormat);
		final SimpleDateFormat srcfmt = new SimpleDateFormat(XConfig.CalendarTimeFormat);
		dlg.setStartDate(desfmt.format(sdt));
		dlg.setEndDate(desfmt.format(edt));
		
		dlg.addOkClickListener(new ClickListener(){
			public void onClick(Widget sender){
				dlg.hide();
				
				XEvent ev = new XEvent();
				ev.setObject(dlg.getObject());
				ev.setAgenda(dlg.getAgenda());
				ev.setAuthor(Calendar.getUser());
				ev.setAgenda(dlg.getAgenda());
				ev.setCategory(dlg.getCategory());
				ev.setStartDate(srcfmt.format(desfmt.parse(dlg.getStartDate())));
				ev.setEndDate(srcfmt.format(desfmt.parse(dlg.getEndDate())));
				ev.setDescription(dlg.getDescription());
				ev.setLocation(dlg.getLocation());
				
				String pageName = Calendar.getDataManager().encodeEventPageName(ev, sdt.getTime(), edt.getTime());
				ev.setName(pageName);
				Calendar.getDataManager().createEvent(ev, new AsyncCallback(){

					public void onFailure(Throwable arg0) {
						Window.alert("failed to create event");
					}

					public void onSuccess(Object arg0) {
						dialog.hide();
						String pageName = (String)arg0;
						Calendar.getDataManager().getEvent(pageName, new AsyncCallback(){

							public void onFailure(Throwable arg0) {
								Window.alert("failed to create event");
							}

							public void onSuccess(Object arg0) {
								Calendar.getCacheManager().addEvent((XEvent) arg0);
								putEventOnTimeGrid((XEvent) arg0);
							}							
						});						
					}
				});	
			}
		});
		
		dlg.addCancelClickListener(new ClickListener(){
			public void onClick(Widget sender){
				dlg.hide();
			}
		});
	}
	
	public void timescroller_NextClickListener(Widget sender){
		switch(currentView){
		case 0:
			Calendar.getDateManager().nextDay();
			setView(currentView);
			break;
		case 1: 
			Calendar.getDateManager().nextWeek();
			setView(currentView);
			break;
		case 2: 
			Calendar.getDateManager().nextMonth();
			setView(currentView);
			break;
		case 3:
			Calendar.getDateManager().nextYear();
			setView(currentView);
			break;
		}
	}
	
	public void timescroller_PrevClickListener(Widget sender){
		switch(currentView){
		case 0:
			Calendar.getDateManager().prevDay();
			setView(currentView);
			break;
		case 1: 
			Calendar.getDateManager().prevWeek();
			setView(currentView);
			break;
		case 2: 
			Calendar.getDateManager().prevMonth();
			setView(currentView);
			break;
		case 3:
			Calendar.getDateManager().prevYear();
			setView(currentView);
			break;
		}
	}
	


}
