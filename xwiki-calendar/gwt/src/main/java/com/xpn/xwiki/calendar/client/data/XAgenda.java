package com.xpn.xwiki.calendar.client.data;

import com.xpn.xwiki.calendar.client.config.XConfig;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XObject;

public class 
XAgenda {
	
	protected String name;
	
	protected int    number;

	protected String title;
	
	protected String description;
	
	protected String author;
	
	protected String visibility;
	
	
	public XAgenda(){
		super();
		
		
	}
	
	public XAgenda(Document agendaDoc){
        XObject event = agendaDoc.getObject(XConfig.AGENDA_CLASS_NAME, 0);
        name = agendaDoc.getFullName();
        title = event.getViewProperty(XConfig.AGENDA_FIELD_TITLE);
        description = event.getViewProperty(XConfig.AGENDA_FIELD_DESCRIPTION);
        author = event.getViewProperty(XConfig.AGENDA_FIELD_AUTHOR);
        visibility = event.getViewProperty(XConfig.AGENDA_FIELD_VISIBILITY);
        number = event.getNumber();
	}
	
	public XAgenda(XObject agenda){
		if( agenda.getClassName().equals(XConfig.AGENDA_CLASS_NAME)){
	        name = agenda.getName();
	        title = agenda.getProperty(XConfig.AGENDA_FIELD_TITLE).toString();
	        description = agenda.getProperty(XConfig.AGENDA_FIELD_DESCRIPTION).toString();
	        author = agenda.getProperty(XConfig.AGENDA_FIELD_AUTHOR).toString();
	        //visibility = agenda.getProperty(XConfig.AGENDA_FIELD_VISIBILITY).toString();
	        number = agenda.getNumber();			
		}
	}
	
	public XObject toxo(){
		XObject obj = new XObject();
		obj.set(XConfig.AGENDA_FIELD_TITLE, title);
		obj.set(XConfig.AGENDA_FIELD_DESCRIPTION, description);
		obj.set(XConfig.AGENDA_FIELD_VISIBILITY, visibility);
		obj.setClassName(XConfig.AGENDA_CLASS_NAME);
		obj.setNumber(number);
		return obj;
	}

	public XObject toXObject(){
		return toxo();
	}

	public String getName(){
		return name;
	}
	
	public String getTitle() {
		return title;
	}


	public String getDescription() {
		return description;
	}


	public String getAuthor() {
		return author;
	}


	public String getVisibility() {
		return visibility;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	public void setName(String name){
		this.name = name;
	}

}
