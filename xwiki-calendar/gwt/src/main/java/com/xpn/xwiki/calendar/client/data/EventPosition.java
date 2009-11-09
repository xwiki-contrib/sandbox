package com.xpn.xwiki.calendar.client.data;

public class EventPosition {
	
	private int row = -1;
	private int col = -1;
	
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public EventPosition(){
	}
	
	public EventPosition(int row, int col){
		this.row = row;
		this.col = col;
	}
	
	

}
