package com.xpn.xwiki.calendar.client.ui;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author samir CHAKOUR
 *
 */
public interface  CalendarImageBundle extends ImageBundle 
{	
	/**
	 * @gwt.resource com/xpn/xwiki/calendar/public/prev.png 
	 */
	public AbstractImagePrototype timescrollPrevImage();
	
	/**
	 * @gwt.resource com/xpn/xwiki/calendar/public/next.png 
	 */
	public AbstractImagePrototype timescrollNextImage();
	
	/**
	 * @gwt.resource com/xpn/xwiki/calendar/public/up_arrow.png
	 */
	public AbstractImagePrototype upArrow();
	
	/**
	 * @gwt.resource com/xpn/xwiki/calendar/public/down_arrow.png
	 */
	public AbstractImagePrototype downArrow();
	
}
