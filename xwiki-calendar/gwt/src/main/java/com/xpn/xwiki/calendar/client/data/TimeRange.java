package com.xpn.xwiki.calendar.client.data;

public class TimeRange {

	private long from;
	private long to;
	
	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public TimeRange(){
	}
	
	public TimeRange(long from, long to){
		this.from = from;
		this.to = to;
	}
}
