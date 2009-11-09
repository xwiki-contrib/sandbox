package com.xpn.xwiki.calendar.client.data;

import com.google.gwt.user.client.Window;
import com.xpn.xwiki.calendar.client.config.XConfig;
import com.xpn.xwiki.gwt.api.client.XObject;

public class XView {
	
	protected String  name;
	
	protected String  active;
	
	protected String  timeScale;
	
	protected int     startAt;
	
	protected int     endAt;
	
	public XView(){
		super();
	}
	
	public XView(XObject view){
		if( view == null || view.getClassName() != XConfig.VIEW_CLASS_NAME)
			return;
		
		name = view.get(XConfig.VIEW_FIELD_NAME).toString();
		Window.alert(name);
		timeScale = view.get(XConfig.VIEW_FIELD_TIMESCALE).toString();
		startAt = Integer.parseInt(view.get(XConfig.VIEW_FIELD_STARTAT).toString());
		endAt = Integer.parseInt(view.get(XConfig.VIEW_FIELD_ENDAT).toString());
		active = view.get(XConfig.VIEW_FIELD_ACTIVE).toString(); 
	}

	public String getName() {
		return name;
	}

	public String getTimeScale() {
		return timeScale;
	}

	public int getStartAt() {
		return startAt;
	}

	public int getEndAt() {
		return endAt;
	}

	public String isActive() {
		return active;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTimeScale(String timeScale) {
		this.timeScale = timeScale;
	}

	public void setStartAt(int startAt) {
		this.startAt = startAt;
	}

	public void setEndAt(int endAt) {
		this.endAt = endAt;
	}

	public void setActive(String active) {
		this.active = active;
	}

	
}
