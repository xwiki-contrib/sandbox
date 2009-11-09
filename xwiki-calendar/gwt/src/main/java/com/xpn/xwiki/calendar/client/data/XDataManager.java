package com.xpn.xwiki.calendar.client.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gwtwidgets.client.util.SimpleDateFormat;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xpn.xwiki.calendar.client.*;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XObject;
import com.xpn.xwiki.gwt.api.client.XWikiGWTException;
import com.xpn.xwiki.gwt.api.client.XWikiServiceAsync;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;

/**
 * @author samir CHAKOUR
 *
 */
public class XDataManager {
	
	private Calendar 		calendarManager;
	
	public XDataManager(){
		calendarManager = Calendar.getInstance();
	}
	
	public XDataManager(Calendar manager){
		this();
		calendarManager = manager;
	}
	
	public String encodeEventPageName(XEvent ev, long stime, long etime){
		String pageName = ev.getObject().replace(' ', '_');
		pageName += "-"+Long.toHexString(stime);
		pageName += "-"+Long.toHexString(etime);
		return pageName;
	}
	
	public void createEvent(final XEvent event, final AsyncCallback cb)
	{	
		if( event == null) {
			cb.onFailure(null);
			return;
		}
		final XWikiServiceAsync xService = calendarManager.getXWikiServiceInstance();
		final String eventName = event.getName();
		
		xService.getUniquePageName(Calendar.getSpace(), 
								   eventName, 
								   new XWikiAsyncCallback(calendarManager) {
			
			public void onFailure(Throwable caught){
				super.onFailure(caught);
			}
			
			public void onSuccess(Object result){
				super.onSuccess(result);
				final String pageName = Calendar.getSpace() + "." + result.toString();
				
				String defaultContent = "#includeForm('"+XConfig.EVENT_CLASS_SHEET+"')";
				xService.saveDocumentContent(pageName, defaultContent, new AsyncCallback(){
					public void onFailure(Throwable caught){
						cb.onFailure(caught);
					}
					
					public void onSuccess(Object result){
						if (!((Boolean)result).booleanValue()) {
							cb.onFailure(getAccessDeniedException("Event Insertion Error!", "Save document failure"));
						}
						else
						{
							event.setName(pageName);
							XObject o =event.toxo();
							
							xService.saveObject(o, new AsyncCallback() {
								
								public void onFailure(Throwable caught){
									//call the user on failure method
									cb.onFailure(caught);	
								}
								
								public void onSuccess(Object result){
									if(!((Boolean)result).booleanValue())
									{
										cb.onFailure(getAccessDeniedException("Event Insertion Error!", "Save object failure"));
									}
									else
									{
										cb.onSuccess(pageName);		
									}
								}
							});
						}
					}		
				});	
			}			
		});
	}

	
	public void updateEvent(final XEvent event, final AsyncCallback cb){
		
		final XWikiServiceAsync xService = calendarManager.getXWikiServiceInstance();
		final String pageName = event.getName();
		XObject eventObj = event.toXObject();
		xService.saveObject(eventObj, new AsyncCallback() {
			public void onFailure(Throwable caught){
				cb.onFailure(caught);
			}
			
			public void onSuccess(Object result){
				 if (!((Boolean)result).booleanValue()) {
	                    String errorMessage = "Access denied!"; //.getTranslation("accessdenied");
	                    cb.onFailure(getAccessDeniedException(errorMessage, errorMessage));
	                } else {
	                    cb.onSuccess(pageName);
	                }
			}
		});
	}
	
	public void removeEvent(final XEvent event, final AsyncCallback cb){
		
		final String pageName = event.getName();
		final XWikiServiceAsync xService = calendarManager.getXWikiServiceInstance();
		
		try {
			if(pageName == null || pageName.equals(""))
				cb.onFailure(null);
			else
			{
				xService.deleteDocument(pageName, new AsyncCallback(){
					public void onFailure(Throwable caught){
						cb.onFailure(caught);
					}
					
					public void onSuccess(Object result){
						if (!((Boolean)result).booleanValue()) {
	                        String errorString = "Access denied!";//.getTranslation("removekeyword.accessdenied");
	                        cb.onFailure(getAccessDeniedException(errorString, errorString));
	                    } else {
	                        cb.onSuccess(result);
	                    }
					}
				});
			}
		}
		catch(Exception ex){
			cb.onFailure(ex);
		}
	}
	
	public void getEvent(String pageName, final AsyncCallback cb){
		
		calendarManager.getXWikiServiceInstance().getDocument(pageName, true, false, false, new XWikiAsyncCallback(calendarManager) {
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                // We encapsulate the result in a FeedArticle object
                XEvent event = new XEvent((Document) result);
                cb.onSuccess(event);
            }
		});
	}
	
	/**
	 * @param userName a valide XWiki username 
	 * @param cb an asynch callback object
	 * 
	 * @return return an event list to the asynch callback method
	 */
	public void getUserEvents(String userName, final AsyncCallback cb){
		//TODO : filter event-document by user.
		String hsql = ", BaseObject as obj where doc.fullName=obj.name and obj.className = '" + XConfig.EVENT_CLASS_NAME + "'"; 
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, false, false, new XWikiAsyncCallback(calendarManager){
			
			public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                List evDocs = (List) result;
				List events = new ArrayList();
				
				for(int i = 0; i < evDocs.size(); i++){
					XEvent evObj = new XEvent(((Document)evDocs.get(i)).getObject(XConfig.EVENT_CLASS_NAME));
					events.add(evObj);
				}
				
				cb.onSuccess(events);
            }	
		});
	}
	
	public void getUserEvents(final String username, final long start, final long end, final AsyncCallback cb){
		SimpleDateFormat sdf = new SimpleDateFormat(XConfig.CalendarTimeSQLFormat);
		String hsql = ", BaseObject as obj, DateProperty as dtp, StringProperty as username where doc.fullName=obj.name and obj.className = '" + 
						XConfig.EVENT_CLASS_NAME + 	"' and doc.name <> '" + XConfig.EVENT_CLASS_SHEET + 
						"' and dtp.id.id = obj.id and dtp.name = '"+ XConfig.EVENT_FILED_STARTDATE +"' and dtp.value >= '"+sdf.format(new Date(start)) + 
						"' and dtp.value <= '" + sdf.format(new Date(end)) +"' and username.id.id = obj.id and" +
						" username.name = '"+XConfig.EVENT_FILED_AUTHOR + "' and username.value = '"+username+"' order by dtp.value";
				
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, false, false, 
				new XWikiAsyncCallback(calendarManager){
			
			public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                //Window.alert("getUserEvents " + result.toString());
                
                List evDocs = (List) result;
				List events = new ArrayList();
				
				for(int i = 0; i < evDocs.size(); i++){
					Document doc = (Document)evDocs.get(i);
					XEvent evObj = new XEvent(doc.getObject(XConfig.EVENT_CLASS_NAME));
					events.add(evObj);
				}
				
				cb.onSuccess(events);
            }	
		});		
	}
	
	public void getUserEvents(String userName, String space, final AsyncCallback cb){
		//TODO : filter event-document by user/space.
		String hsql = ", BaseObject as obj, StringProperty as username where doc.fullName=obj.name and obj.className = '" + 
		XConfig.EVENT_CLASS_NAME + "' and username.id.id = obj.id and" +
		" username.name = '"+XConfig.EVENT_FILED_AUTHOR + "' and username.value = '"+userName+"'"; 
		
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, false, false, 
				new XWikiAsyncCallback(calendarManager){
			
			public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                
                List evDocs = (List) result;
				List events = new ArrayList();
				
				for(int i = 0; i < evDocs.size(); i++){
					Document doc = (Document)evDocs.get(i);
					XEvent evObj = new XEvent(doc.getObject(XConfig.EVENT_CLASS_NAME));
					events.add(evObj);
				}
				
				cb.onSuccess(events);
            }	
		});
	}
	
	public void getCategorys(final AsyncCallback cb){
		calendarManager.getXWikiServiceInstance().getDocument(XConfig.CATEGORY_DOCUMENT_NAME, true, false, false, 
			new XWikiAsyncCallback(calendarManager) {
	            public void onFailure(Throwable caught) {
	                super.onFailure(caught);
	                cb.onFailure(caught);
	            }
	
	            public void onSuccess(Object result) {
	            	super.onSuccess(result);
	                List catList = ((Document) result).getObjects(XConfig.CATEGORY_CLASS_NAME);
	                List retList = new ArrayList();
	                for(int i = 0; i < catList.size(); i++){
	                	retList.add(new XCategory((XObject)catList.get(i)));
	                }
	                cb.onSuccess(retList);
	            }
		});
	}
	
	
	public void getAgendaEvents(String agenda, final AsyncCallback cb){
		//TODO : filter event-document by agenda.
		String hsql = ", BaseObject as obj where doc.fullName=obj.name and obj.className = '" + XConfig.EVENT_CLASS_NAME + "'"; 
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, false, false, cb);
	}
	
	public void getAgendaEvents(String agenda, String username, final AsyncCallback cb){
		//TODO : filter event-document by agenda.
		String hsql = ", BaseObject as obj where doc.fullName=obj.name and obj.className = '" + XConfig.EVENT_CLASS_NAME + "'"; 
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, false, false, cb);
	}

	public void createAgenda(final XAgenda agenda, final AsyncCallback cb){
		if( agenda == null )
		{
			cb.onFailure(null);
			return;
		}
		
		final XWikiServiceAsync xService = calendarManager.getXWikiServiceInstance();
		final String agendaName = agenda.getName();
		xService.getUniqueDocument(Calendar.getSpace(), agendaName, new XWikiAsyncCallback(calendarManager) {

				public void onFailure(Throwable caught){
				
				}
				
				public void onSuccess(Object result){
				final String pageName = Calendar.getSpace() + "." + result;
				
				xService.saveObject(agenda.toxo(), new AsyncCallback() {
					
					public void onFailure(Throwable caught){
						cb.onFailure(caught);	
					}
					
					public void onSuccess(Object result){
						if(!((Boolean)result).booleanValue())
						{
							cb.onFailure(getAccessDeniedException("Event Insertion Error!", "Save object failure"));
						}
						else
						{
							String defaultContent = "#includeForm('"+XConfig.AGENDA_CLASS_SHEET+"')";
							xService.saveDocumentContent(pageName, defaultContent, new AsyncCallback(){
								public void onFailure(Throwable caught){
									cb.onFailure(caught);
								}
								
								public void onSuccess(Object result){
									if (!((Boolean)result).booleanValue()) {
										cb.onFailure(getAccessDeniedException("Event Insertion Error!", "Save document failure"));
									}
									else
									{
										cb.onSuccess(pageName);									
									}
								}
							});
						}
					}
				});	
			}			
		});
	}
	
	public void updateAgenda(final XAgenda agenda, final AsyncCallback cb){
		
		final XWikiServiceAsync xService = calendarManager.getXWikiServiceInstance();
		final String pageName = agenda.getName();
		XObject agendaObj = agenda.toxo();
		xService.saveObject(agendaObj, new AsyncCallback() {
			public void onFailure(Throwable caught){
				cb.onFailure(caught);
			}
			
			public void onSuccess(Object result){
				 if (!((Boolean)result).booleanValue()) {
	                    String errorMessage = "Access denied!"; //.getTranslation("accessdenied");
	                    cb.onFailure(getAccessDeniedException(errorMessage, errorMessage));
	                } else {
	                    cb.onSuccess(pageName);
	                }
			}
		});		
	}
	
	public void removeAgenda(final XAgenda agenda, final AsyncCallback cb){
		
		final String pageName = agenda.getName();
		final XWikiServiceAsync xService = calendarManager.getXWikiServiceInstance();
		
		try {
			if(pageName == null || pageName.equals(""))
				cb.onFailure(null);
			else
			{
				xService.deleteDocument(pageName, new AsyncCallback(){
					public void onFailure(Throwable caught){
						cb.onFailure(caught);
					}
					
					public void onSuccess(Object result){
						if (!((Boolean)result).booleanValue()) {
	                        String errorString = "Access denied!";//.getTranslation("removekeyword.accessdenied");
	                        cb.onFailure(getAccessDeniedException(errorString, errorString));
	                    } else {
	                        cb.onSuccess(result);
	                    }
					}
				});
			}
		}
		catch(Exception ex){
			cb.onFailure(ex);
		}

	}
	
	public void getAgenda(String pageName, final AsyncCallback cb){
		
		calendarManager.getXWikiServiceInstance().getDocument(pageName, true, true, false, new XWikiAsyncCallback(calendarManager) {
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                // We encapsulate the result in a FeedArticle object
                XAgenda agenda = new XAgenda((Document) result);
                cb.onSuccess(agenda);
            }
		});
	}
	
	
	public void getAgendas(final AsyncCallback cb){
		//all agenda-document.
		String hsql = ", BaseObject as obj where doc.fullName=obj.name and obj.className = '" + XConfig.AGENDA_CLASS_NAME + "'"; 
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, true, false, 
				new XWikiAsyncCallback(calendarManager){
			public void onFailure(Throwable arg0) {
				super.onFailure(arg0);
				cb.onFailure(arg0);
			}

			public void onSuccess(Object arg0) {
				super.onSuccess(arg0);
				List docs = (List) arg0;
				List agenda = new ArrayList();
				
				for(int i = 0; i < docs.size(); i++){
					XAgenda agObj = new XAgenda(((Document)docs.get(i)).getObject(XConfig.AGENDA_CLASS_NAME));
					
					agenda.add(agObj);
				}
				
				cb.onSuccess(agenda);
			}
			
		});
		
	}
	
	public void getSpaceAgenda(String space, final AsyncCallback cb){
		//todo: filter agenda-document by space.
		String hsql = ", BaseObject as obj where doc.fullName=obj.name and obj.className = '" + XConfig.AGENDA_CLASS_NAME + "'"; 
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, true, false, cb);
	}
	
	public void getUserAgenda(String username, final AsyncCallback cb){
		//todo: filter agenda-document by user.
		String hsql = ", BaseObject as obj where doc.fullName=obj.name and obj.className = '" + XConfig.AGENDA_CLASS_NAME + "'"; 
		calendarManager.getXWikiServiceInstance().getDocuments(hsql, 0, 0, true, true, false, cb);
	}
	
	protected XWikiGWTException getAccessDeniedException(String message, String fullMessage) {
        //todora: define an exception constants class in xwiki-web-gwt api
        return new XWikiGWTException(message, fullMessage, 9001, 48);
    }


}
