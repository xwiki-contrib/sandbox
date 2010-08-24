package com.xpn.xwiki.calendar.client.ui;

import java.util.List;
import java.util.Date;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.calendar.client.config.XConfig;
import com.xpn.xwiki.calendar.client.data.XAgenda;
import com.xpn.xwiki.calendar.client.data.XCalendar;

/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 *
 */
public class CreateEventDialog extends DialogBox implements ClickListener {
	private Label 	lDialogHead;
	private Label 	lObject;
	private Label 	lAgenda;
	private Label   lType;
	private TextBox tbObject;
	private ListBox lbAgenda;
	private ListBox lbType;
	private Button  btnOk;
	private Button  btnDetail;
	private Button  btnCancel;
	private VerticalPanel mainPanel;
	private Date    sDate;
	private Date    eDate;
	private int		view;
	private FlexTable bckgrid;
	
	
	public String getObject() {
		return tbObject.getText();
	}

	public void setObject(String object) {
		this.tbObject.setText(object);
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

	public String getType() {
		return lbType.getItemText(lbType.getSelectedIndex());
	}

	public void setType(int index) {
		this.lbType.setSelectedIndex(index);
	}

	public Date getStartDate() {
		return sDate;
	}

	public void setStartDate(Date date) {
		sDate = date;
	}

	public Date getEndDate() {
		return eDate;
	}

	public void setEndDate(Date date) {
		eDate = date;
	}

	public int getView() {
		return view;
	}

	public void setView(int view) {
		this.view = view;
	}

	public CreateEventDialog(){
		//mainPanel = new VerticalPanel();
		initialize("");
		setWidget(bckgrid);
		this.center();
	}
	
	public CreateEventDialog(Date startDate, Date endDate){
		sDate = startDate;
		eDate = endDate;
		XCalendar sc = new XCalendar(sDate);
		XCalendar ec = new XCalendar(eDate);
		//mainPanel = new VerticalPanel();
		initialize(sc.getDayAndMonth() + " " + ec.getDayAndMonth());
		setWidget(bckgrid);
		this.center();
	}
	
	public CreateEventDialog(Date startDate, Date endDate, int mode){
		sDate = startDate;
		eDate = endDate;
		view = mode;
		XCalendar sc = new XCalendar(sDate);
		XCalendar ec = new XCalendar(eDate);
		//mainPanel = new VerticalPanel();
		initialize(getHeadText());
		setWidget(bckgrid);
		this.center();
	}
	
	private String getHeadText(){
		String 		result = "";
		XCalendar 	sc = new XCalendar(sDate);
		XCalendar 	ec = new XCalendar(eDate);
		
		switch(view){
		case 0 : 
			result = sc.getDayAndMonth() + " de " + 
						sc.getStrHours() + ":" + sc.getStrMinutes() + " à " + 
						ec.getStrHours() + ":" + ec.getStrMinutes();			
			break;
		case 1: 
			result = sc.getDayAndMonth() + " - " + 
			sc.getStrHours() + ":" + sc.getStrMinutes();
			break;
		case 2 :
			result = sc.getDayAndMonth();			
			break;

		}
		return result;
	}
	
	public void initialize(String dialogHead){
		setTitle("Création d'un événement");
		
		bckgrid = new FlexTable();
		bckgrid.setCellSpacing(4);
		
		lDialogHead = new Label(dialogHead);
		
		lObject = new Label("Objet :");
		lObject.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lAgenda = new Label("Agenda :");
		lAgenda.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		lType = new Label("Type :");
		lType.setStyleName(XConfig.CalendarEventLabelFieldStyleName);
		
		tbObject = new TextBox();
		lbAgenda = new ListBox();
		lbType = new ListBox();
		lbType.addItem("Evénement");
		
		btnOk = new Button("Enregistrer");	
		btnCancel = new Button("Annuler");
		btnDetail = new Button("Détails");
		
		btnCancel.addClickListener(this);
		bckgrid.setWidget(0, 1, lDialogHead);
		
		//type
		bckgrid.setWidget(1, 0, lType);
		bckgrid.setWidget(1, 1, lbType);
		
		//object
		bckgrid.setWidget(2, 0, lObject);
		bckgrid.setWidget(2, 1,tbObject);
		
		//agenda
		bckgrid.setWidget(3, 0, lAgenda);
		bckgrid.setWidget(3, 1, lbAgenda);
		
		HorizontalPanel p = new HorizontalPanel();
		p.add(btnOk);
		p.add(btnDetail);
		p.add(btnCancel);
		bckgrid.setWidget(4, 1, p);
		
		tbObject.setFocus(true);
		
		setStyleName(XConfig.CalendarCreateEventDialogStyleName);
		bckgrid.getColumnFormatter().setStyleName(0, XConfig.CalendarFormTitleStyleName);
		bckgrid.getColumnFormatter().setStyleName(1, XConfig.CalendarFormFieldStyleName);
	}
	
	public void addClickListener(ClickListener listener){
		btnOk.addClickListener(listener);
	}

	public void addDetailClickListener(ClickListener listener){
		btnDetail.addClickListener(listener);
	}
	
	public void onClick(Widget arg0) {
		// TODO Auto-generated method stub
		CreateEventDialog.this.hide();
	}
	
}
