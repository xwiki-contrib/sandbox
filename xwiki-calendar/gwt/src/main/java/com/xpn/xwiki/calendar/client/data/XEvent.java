package com.xpn.xwiki.calendar.client.data;



import org.gwtwidgets.client.util.SimpleDateFormat;

import com.google.gwt.user.client.Window;
import com.xpn.xwiki.calendar.client.config.*;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XObject;

/**
 * @author samir Chakour
 *
 */
public class  XEvent 
{ 
    private String name = "";
    
    private int 	 number = 0;
    
    private String object = "";

    private String startDate = "";

    private String endDate = "";

    private String location = "";

    private String URL = "";

    private String category = "";

    private String description = "";

    private String user = "";
    
    private String author = "";
    
    private String agenda = "";
    
    private Object ref = "";   
    
    public Object getRef() {
		return ref;
	}

	public void setRef(Object ref) {
		this.ref = ref;
	}

	public XEvent(){
    	super();
    }
 
    /**
     * @param eventDoc a valid document containing the event to load
     */
    public XEvent(Document eventDoc)
    {
        XObject event = eventDoc.getObject(XConfig.EVENT_CLASS_NAME, 0);
        name = eventDoc.getFullName();
        object = String.valueOf(event.getProperty(XConfig.EVENT_FILED_TITLE));
        startDate = String.valueOf(event.getProperty(XConfig.EVENT_FILED_STARTDATE));
        endDate = String.valueOf(event.getProperty(XConfig.EVENT_FILED_ENDDATE));
        location = String.valueOf(event.getProperty(XConfig.EVENT_FILED_LOCATION));
        category = String.valueOf(event.getProperty(XConfig.EVENT_FILED_CATEGORY));
        description = String.valueOf(event.getProperty(XConfig.EVENT_FILED_DESCRIPTION));
        author = String.valueOf(event.getProperty(XConfig.EVENT_FILED_AUTHOR));
        agenda = String.valueOf(event.getProperty(XConfig.EVENT_FILED_AGENDA));
        number = event.getNumber();
    }
    
    public XEvent(XObject event){
    	if( event.getClassName().equals(XConfig.EVENT_CLASS_NAME)){
    		name = event.getName();
            object = String.valueOf(event.get(XConfig.EVENT_FILED_TITLE));
            startDate = String.valueOf(event.getProperty(XConfig.EVENT_FILED_STARTDATE));
            endDate = String.valueOf(event.getProperty(XConfig.EVENT_FILED_ENDDATE));
            location = String.valueOf(event.get(XConfig.EVENT_FILED_LOCATION));
            category = String.valueOf(event.get(XConfig.EVENT_FILED_CATEGORY));
            description = String.valueOf(event.get(XConfig.EVENT_FILED_DESCRIPTION));
            author = String.valueOf(event.get(XConfig.EVENT_FILED_AUTHOR));
            agenda = String.valueOf(event.get(XConfig.EVENT_FILED_AGENDA));
            number = event.getNumber();            
    	}
    }
    
    /**
     * @return a valid XWiki Object representing the event object
     */
    public XObject toXObject(){
    	return toxo();
    }
    
    /**
     * @return a valid XWiki Object representing the event object
     * @throws ParseException 
     */
    public XObject toxo(){
    	XObject eventObj = new XObject();
    	eventObj.setProperty(XConfig.EVENT_FILED_TITLE, object);
    	eventObj.setProperty(XConfig.EVENT_FILED_DESCRIPTION, description);
    	SimpleDateFormat sdf = new SimpleDateFormat(XConfig.CalendarTimeFormat);
    	
	    eventObj.set(XConfig.EVENT_FILED_STARTDATE, sdf.parse(startDate));
	    eventObj.set(XConfig.EVENT_FILED_ENDDATE, sdf.parse(endDate));
    	
    	eventObj.setProperty(XConfig.EVENT_FILED_LOCATION, location);
    	eventObj.setProperty(XConfig.EVENT_FILED_URL, URL);
    	eventObj.setProperty(XConfig.EVENT_FILED_CATEGORY,  category);
    	eventObj.setProperty(XConfig.EVENT_FILED_USER, user);
    	eventObj.setProperty(XConfig.EVENT_FILED_AUTHOR, author);
    	eventObj.setProperty(XConfig.EVENT_FILED_AGENDA, agenda);
    	eventObj.setClassName(XConfig.EVENT_CLASS_NAME);
    	eventObj.setNumber(0);
    	eventObj.setName(name);
    	return eventObj;    	
    }
    
    public String getName(){
    	return name;
    }

	public String getObject() {
		return object;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public String getLocation() {
		return location != null ? location : " ";
	}

	public String getURL() {
		return URL;
	}

	public String getCategory() {
		return category != null ? category : " ";
	}

	public String getDescription() {
		return description != null ? description : " ";
	}

	public String getUser() {
		return user;
	}

	public String getAuthor() {
		return author;
	}


	public String getAgenda() {
		return agenda;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setObject(String title) {
		this.object = title;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setURL(String url) {
		URL = url;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setAgenda(String agenda) {
		this.agenda = agenda;
	}
}
