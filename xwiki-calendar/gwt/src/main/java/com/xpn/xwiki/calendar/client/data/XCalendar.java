package com.xpn.xwiki.calendar.client.data;

import java.util.Date;
import org.gwtwidgets.client.util.SimpleDateFormat;

import com.xpn.xwiki.calendar.client.config.*;


/**
 * @author samir CHAKOUR (smartech@hotmail.fr)
 *
 */
public class XCalendar implements Comparable 
{	
	private final String shortDays[] 	= {"dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."}; 
	private final String longDays[] 	= {"dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi"};
	private final String shortMonths[] 	= {"jan.", "fev.", "mars", "avr.", "mai", "juin", "jui.", "août", "sept.", "oct.", "nov.", "dec." };
	private final String longMonths[] 	= {"janvier", "fevrier", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "decembre" };
	
	public final SimpleDateFormat fDayMonth = new SimpleDateFormat("dd/MM");
	public final SimpleDateFormat fHoursMinutes = new SimpleDateFormat("HH:mm");
	public final SimpleDateFormat fDayMonYear = new SimpleDateFormat("dd MM yyyy");
	
	
	private int 	 	 defaultWeekendDays[] = {5, 6};
	private int    		 defaultWeekStartDay = 5;	
	private int 		 weekStartDay = defaultWeekStartDay;
	private int          weekend[];
	private Date         iDate;
	private int 		 iDay;
	private int 		 iMonth;
	private int 		 iYear;
	private int 		 iHours;
	private int 		 iMinutes;
	
	public  static final int DAY_OF_MONTH = 0x1;
	public  static final int DAY_OF_WEEK = 0x2;
	public  static final int MONTH = 0x4;
	public  static final int YEAR = 0x8;
	public 	static final int HOURS = 0x10;
	public  static final int MINUTES = 0x20;
 	
	
	public XCalendar(){
		initData();
	}
	
	public XCalendar(int year, int month, int day){
		iDay = day;
		iMonth = month;
		iYear = year;
		iHours = 0;
		iMinutes = 0;
		calculateDate();
	}
	
	public XCalendar(Date date){
		setDate(date);	
	}
	
	public XCalendar(XCalendar c){
		//TODO clone all internal data of the calendar
		setDate(c.getDate());
	}
	
	public XCalendar(String date){
		Date dt = new Date( Date.parse( date ));
		setDate(dt);
	}
	
	private void initData(){
		setDate(new Date());
	}
	
	private void calculateDate(){
		setDate(new Date(iYear, iMonth, iDay, iHours, iMinutes, 0));
	}
	
	public void add(int field, int amount){
		switch(field){
		case DAY_OF_MONTH : iDay += amount; break;
		case MONTH : iMonth += amount; break;
		case YEAR  : iYear += amount; break;
		case HOURS : iHours += amount; break;
		case MINUTES : iMinutes += amount; break;
		}
		calculateDate();
	}
	
	public int  get(int field){
		switch(field){
		case DAY_OF_MONTH : return iDate.getDate(); 
		case MONTH : return iDate.getMonth(); 
		case YEAR  : return iDate.getYear(); 
		case DAY_OF_WEEK : return iDate.getDay();
		case HOURS : return iDate.getHours();
		case MINUTES : return iDate.getMinutes();
		default : return 0;
		}
	}
	
	public boolean before(XCalendar calendar){
		long t1 = iDate.getTime();
		long t2 = calendar.getDate().getTime();
		return t1 < t2 ? true : false;
	}
	
	public boolean after(XCalendar calendar){
		long t1 = iDate.getTime();
		long t2 = calendar.getDate().getTime();
		return t1 > t2 ? true : false;
	}	
	
	public boolean isToday(){
		Date d = new Date();
		return d.getYear() == iDate.getYear() && 
			   d.getMonth() == iDate.getMonth() && 
			   d.getDate() == iDate.getDate()
			   ? true : false; 
	}
	
	public void resetFromDayStart(){
		iHours = XConfig.CalendarStartHours;
		iMinutes = 0;
		calculateDate();
	}
	public void resetFromWeekStart(){
		int dayOfWeek = getDayOfWeek();
		iHours = 0;
		iMinutes = 0;
		add(DAY_OF_MONTH, -dayOfWeek);
	}
	
	public void resetFromMonthStart(){
		iDay = 1;
		iHours = 0;
		iMinutes = 0;
		calculateDate();
		resetFromWeekStart();
	}
	
	public void resetFromYearStart(){
		iMonth = 1;
		resetFromMonthStart();
	}
	
	public long getYearRange(){
		XCalendar c = new XCalendar();
		c.resetFromYearStart();
		c.iDay = 31;
		c.iMonth = 11;
		c.iHours = 23;
		c.iMinutes = 59;
		c.calculateDate();
		return c.getDate().getTime();
	}
	
	public void nextHours(){
		add(HOURS, 1);
	}
	
	public void prevHours(){
		add(HOURS, 1);
	}
	public void nextDay(){
		add(DAY_OF_MONTH, 1);
	}
	
	public void prevDay(){
		add(DAY_OF_MONTH, -1);
	}
	
	public void nextWeek(){
		add(DAY_OF_MONTH, 7);
	}
	
	public void endofWeek(){
		add(DAY_OF_MONTH, 6);
	}
	public void prevWeek(){
		add(DAY_OF_MONTH, -7);
	}
	
	public void nextMonth(){
		add(MONTH, 1);
	}
	
	public void prevMonth(){
		add(MONTH, -1);
	}
	
	public void nextYear(){
		add(YEAR, 1);
	}
	
	public void prevYear(){
		add(YEAR, -1);
	}
	
	public int getDayOfMonth(){
		return get(DAY_OF_MONTH);
	}
	
	public int getDayOfWeek(){
		return get(DAY_OF_WEEK);
	}
	
	public int getMonth(){
		return get(MONTH) + 1;
	}
	
	public int getYear(){
		return get(YEAR) + 1900;
	}
	
	public String getShortDate(){
		return shortDays[getDayOfWeek()] + " " + 
			   fDayMonYear.format(iDate);
	}
	
	public String getLongDate(){
		return longDays[getDayOfWeek()] + " " + 
				fDayMonYear.format(iDate);
	}
	
	
	public String getDayAndMonth(){
		return 	getShortDayName() + " " + 
				fDayMonth.format(iDate);
	}
	
	public String getNumericDayMonth(){
		return fDayMonth.format(iDate);
	}
	
	public boolean isWeek(){
		int today = getDayOfWeek();
		int i = 0;
		while( i < weekend.length){
			if( weekend[i++] == today ) 
				return true;
		}
		return false;
	}
	
	public boolean isWeek(int day){
		int i = 0;
		while( i < weekend.length){
			if( weekend[i++] == day ) 
				return true;
		}
		return false;
	}
	
	public Date getDayRange(){
		long time = iDate.getTime();
		time += 0x5265C00; 
		return new Date(time);
	}
	
	public Date getHourRange(){
		long time = iDate.getTime();
		time += 0x36EE80;
		return new Date(time);
	}
	
	public Date getDayRange(Date date){
		long time = date.getTime();
		time += 0x5265C00; 
		return new Date(time);
	}
	
	public Date getWeekRange(){
		long time = iDate.getTime();
		time += 0x240C8400;
		return new Date(time);
	}
	
	public Date getWeekRange(Date date){
		long time = date.getTime();
		time += 0x240C8400;
		return new Date(time);		
	}
	
	public Date getHoureRangeDate(Date date){
		long time = date.getTime();
		time += 0x36EE80;
		return new Date(time);
	}
	
	public String getLongDayName(int day){
		return longDays[day];
	}
	
	public String getLongDayName(){
		return longDays[getDayOfWeek()];
	}
	
	public String getShortDayName(int day){
		return shortDays[day];
	}
	
	public String getShortDayName(){
		return shortDays[getDayOfWeek()];
	}
	

	public String getLongMonthName(){
		return longMonths[getMonth()-1];
	}
	
	public String getLongMonthName(int month){
		return longMonths[month];
	}
	
	public String getShortMonthName(int month){
		return shortMonths[month];
	}
	
	public String getShortMonthName(){
		return shortMonths[getMonth()-1];
	}
	
	public void setWeekend(int[] week){
		weekend = week;
	}
	

	public void setDefaultWeekend(){
		weekend = defaultWeekendDays;
	}
	public int[] getWeekend(){
		return weekend;
	}
	
	public int getWeekStartDay() {
		return weekStartDay;
	}

	public void setWeekStartDay(int weekStartDay) {
		this.weekStartDay = weekStartDay;
	}
	
	public void setDefaultWeekStartDay(){
		weekStartDay = defaultWeekStartDay;
	}
	
	public Date getDate() {
		return iDate;
	}

	public void setDate(Date date) {
		if(date != null){
			iDate = date;
			iDay = iDate.getDate();
			iMonth = iDate.getMonth();
			iYear = iDate.getYear();
			iHours = iDate.getHours();
			iMinutes = iDate.getMinutes();
		}
	}
	
	public int getHours() {
		return iHours;
	}

	public void setHours(int hours) {
		iHours = hours;
		calculateDate();
	}

	public int getMinutes() {
		return iMinutes;
	}

	public void setMinutes(int minutes) {
		iMinutes = minutes;
		calculateDate();
	}
	
	public int compareTo(Object arg0) {
		// TODO write the appropriate code for comparing XCalendar with other objects
		return 0;
	}	
	
	public String getStrHours(){
		return iHours > 9 ? String.valueOf(iHours) : "0" + iHours;
	}
	
	public String getStrMinutes(){
		return iMinutes > 9 ? String.valueOf(iMinutes) : "0" + iMinutes;
	}	
	
	public String getLongNumDate(){
		return iDay + "/" + iMonth + "/" + iYear + " " + iHours + ":" + iMinutes + ":00" ;
	}
	
	

}
