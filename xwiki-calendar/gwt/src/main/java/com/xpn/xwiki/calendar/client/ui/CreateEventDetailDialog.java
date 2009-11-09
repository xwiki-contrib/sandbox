package com.xpn.xwiki.calendar.client.ui;

import java.util.Date;
import java.util.List;

import org.gwtwidgets.client.util.SimpleDateFormat;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.xpn.xwiki.calendar.client.Calendar;
import com.xpn.xwiki.calendar.client.config.XConfig;
import com.xpn.xwiki.calendar.client.data.XAgenda;
import com.xpn.xwiki.calendar.client.data.XCategory;
import com.xpn.xwiki.calendar.client.data.XEvent;

public class CreateEventDetailDialog extends DialogBox {
	
	private VerticalPanel 	mainPanel;
	private FlexTable		table;
	private Label 			lTitle;
	private Label 			lCategory;
	private Label 			lLocation;
	private Label			lDescription;
	private Label 			lDate;
	private Label 			lEnd;
	private Label 			lAgenda;
	
	private ListBox			lbCategory;
	private ListBox			lbAgenda;
	private TextArea		taDescription;
	private TextBox			tbObject;
	private TextBox			tbLocation;
	private TextBox			tbStartDate;
	private TextBox			tbStartTime;
	private TextBox			tbEndDate;
	private TextBox			tbEndTime;
	
	private Button 			btnOk;
	private Button 			btnCancel;
	
	private XEvent			event;


	public CreateEventDetailDialog(){
		mainPanel = new VerticalPanel();
		initialize();
		setWidget(mainPanel);
		setStyleName(XConfig.CalendarCreateEventDialogStyleName);
		this.center();
	}
	
	public void initialize(){
		table = new FlexTable();
		table.setCellSpacing(4);
		lCategory = new Label("Catégorie :");
		lCategory.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lAgenda = new Label("Agenda :");
		lAgenda.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lLocation = new Label("Location :");
		lLocation.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lTitle = new Label("Objet :");
		lTitle.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lDescription = new Label("Description :");
		lDescription.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lDate = new Label("du :");
		lDate.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lEnd  = new Label("au :");
		lEnd.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		
		lbCategory = new ListBox();
		lbAgenda = new ListBox();
		tbObject	= new TextBox();
		tbLocation = new TextBox();
		tbStartDate = new TextBox();
		tbStartTime = new TextBox();
		tbEndDate = new TextBox();
		tbEndTime = new TextBox();
		taDescription = new TextArea();
		taDescription.setHeight("100px");
		
		btnOk = new Button("Enregistrer");
		btnCancel = new Button("Fermer");
		
		table.setWidget(0, 0, lTitle);
		table.setWidget(0, 1, tbObject);

		table.setWidget(1, 0, lDate);
		table.setWidget(1, 1, tbStartDate);
		table.setWidget(2, 1, new Label("Date au format " +  XConfig.CalendarTimeSimpleFormat));

		table.setWidget(3, 0, lEnd);
		table.setWidget(3, 1, tbEndDate);
		table.setWidget(4, 1, new Label("Date au format " + XConfig.CalendarTimeSimpleFormat));
		
		table.setWidget(5, 0, lCategory);
		table.setWidget(5, 1, lbCategory);
		
		table.setWidget(6, 0, lAgenda);
		table.setWidget(6, 1, lbAgenda);
		
		table.setWidget(7, 0, lDescription);
		table.setWidget(7, 1, taDescription);
		
		table.setWidget(8, 0, lLocation);
		table.setWidget(8, 1, tbLocation);
		
		HorizontalPanel p = new HorizontalPanel();
		HTML spacer = new HTML();
		spacer.setWidth("100px");
		p.add(spacer);
		p.add(btnOk);
		p.add(btnCancel);
		p.setStyleName(XConfig.CalendarEventDisplayButtonPanelStyleName);
		table.setWidget(9, 1, p);
		
		initCategorys();
		setAgendaList(Calendar.getAgendaList());
		mainPanel.add(table);
		
		tbObject.setFocus(true);
		taDescription.setCharacterWidth(40);
		taDescription.setVisibleLines(40);

	}
	
	public void initCategorys(){
		List events = Calendar.getInstance().getCategorys();
		
		for(int i = 0; i < events.size(); i++){
			lbCategory.addItem(((XCategory)events.get(i)).getName());
		}
	}
	
	public String getAgenda() {
		return lbAgenda.getItemText(lbAgenda.getSelectedIndex());
	}
	
	public int getAgendaIndex(){
		return lbAgenda.getSelectedIndex();
	}

	public void setAgenda(int index) {
		this.lbAgenda.setSelectedIndex(index);
	}
	
	public void setAgendaList(List lag){
		for(int i = 0; i < lag.size(); i++){
			lbAgenda.addItem(((XAgenda)lag.get(i)).getTitle());
		}
	}
	
	public void addOkClickListener(ClickListener listener){
		btnOk.addClickListener(listener);
	}
	
	public void addCancelClickListener(ClickListener listener){
		btnCancel.addClickListener(listener);
	}
	
	//---------------------------------- Dialog-box properties getters/setters
	
	public int getCategoryIndex() {
		return lbCategory.getSelectedIndex();
	}

	public void setCategoryIndex(int category) {
		lbCategory.setSelectedIndex(category);
	}
	
	public String getCategory(){
		return lbCategory.getItemText(lbCategory.getSelectedIndex());
	}

	public String getDescription() {
		return taDescription.getText();
	}

	public void setDescription(String description) {
		this.taDescription.setText(description);
	}

	public String getObject() {
		return tbObject.getText();
	}

	public void setObject(String object) {
		this.tbObject.setText(object);
	}

	public String getLocation() {
		return tbLocation.getText();
	}

	public void setLocation(String location) {
		this.tbLocation.setText(location);
	}

	public String getStartDate() {
		return tbStartDate.getText();
	}

	public void setStartDate(String startDate) {
		this.tbStartDate.setText(startDate);
	}

	public String getStartTime() {
		return tbStartTime.getText();
	}

	public void setStartTime(String startTime) {
		this.tbStartTime.setText(startTime);
	}

	public String getEndDate() {
		return tbEndDate.getText();
	}

	public void setEndDate(String endDate) {
		this.tbEndDate.setText(endDate);
	}

	public String getEndTime() {
		return tbEndTime.getText();
	}

	public void setEndTime(String endTime) {
		this.tbEndTime.setText(endTime);
	}
	
	public XEvent getEvent() {
		return event;
	}

	public void setEvent(XEvent event) {
		this.event = event;
		this.tbObject.setText(event.getObject());
		this.taDescription.setText(event.getDescription());
		this.tbLocation.setText(event.getLocation());
		
		SimpleDateFormat fmt = new SimpleDateFormat(XConfig.CalendarTimeSimpleFormat);
		long t1 = Date.parse(event.getStartDate());
		long t2 = Date.parse(event.getEndDate());
		this.tbStartDate.setText(fmt.format(new Date(t1)));
		this.tbEndDate.setText(fmt.format(new Date(t2)));

	}

}
