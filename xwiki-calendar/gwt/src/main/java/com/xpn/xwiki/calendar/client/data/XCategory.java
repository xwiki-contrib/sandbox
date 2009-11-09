package com.xpn.xwiki.calendar.client.data;

import com.xpn.xwiki.calendar.client.config.XConfig;
import com.xpn.xwiki.gwt.api.client.XObject;

public class XCategory {
	
	private String name;

	private String desc;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public XCategory(){
	}
	
	public XCategory(XObject obj){
		if( obj.getClassName().equals(XConfig.CATEGORY_CLASS_NAME)){
			name = obj.get(XConfig.CATEGORY_FIELD_NAME).toString();
			desc = obj.get(XConfig.CATEGORY_FIELD_DESC).toString();
		}
	}

}
