/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software;you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation;either version2.1of
 * the License,or(at your option)any later version.
 * <p/>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software;if not,write to the Free
 * Software Foundation,Inc.,51 Franklin St,Fifth Floor,Boston,MA
 * 02110-1301 USA,or see the FSF site:http://www.fsf.org.
 *
 */

package com.xpn.xwiki.watch.client.ui.filterbar;

import org.gwtwidgets.client.ui.cal.CalendarDate;
import org.gwtwidgets.client.ui.cal.CalendarListener;
import org.gwtwidgets.client.ui.cal.CalendarMonth;
import org.gwtwidgets.client.ui.cal.CalendarPanel;
import org.gwtwidgets.client.util.SimpleDateFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.ui.WatchWidget;

/**
 * Date picker widget, used for selecting a date from a calendar.
 * <br />
 * To execute a specific action when a date is selected using this picker, add your own 
 * {@link CalendarListener} using {@link DatePickerWidget#addCalendarListener(CalendarListener)} and 
 * implement the {@link CalendarListener#onDateClick(CalendarDate)} handler. 
 *  
 */
public class DatePickerWidget extends WatchWidget implements CalendarListener
{
    protected CalendarPanel calendarPanel = new CalendarPanel();
    protected Label dateLabel = new Label();
    protected String dateFormat = "dd/MM/yyyy";
    protected String panelTitle = "";
    protected Label monthLabel = new Label();
    protected Label yearLabel = new Label();
    
    public DatePickerWidget()
    {
        super();
    }
    
    public DatePickerWidget(Watch app, String title) {
        super(app);
        this.panelTitle = title;
        setPanel(new FlowPanel());
        this.init();
        initWidget(panel);
    }
    
    public DatePickerWidget(Watch app) {
        this(app, "");
    }
    
    /**
     * {@inheritDoc}
     * @see WatchWidget#init()
     */
    public void init()
    {
        super.init();
        //some nice localisation for the calendar panel
        String monthsLocalisations = watch.getTranslation("filter.dates.months");
        //split by commas
        String[] monthsNames = monthsLocalisations.split(",");
        if (monthsNames.length == 12) {
            //use these new ones
            String[] monthsNamesTrimmed = new String[12];
            for (int i = 0; i < monthsNames.length; i++) {
                monthsNamesTrimmed[i] = monthsNames[i].trim();
            }
            this.calendarPanel.setMonthNames(monthsNamesTrimmed);
        }
        String daysLocalisations = watch.getTranslation("filter.dates.days");
        String[] daysNames = daysLocalisations.split(",");
        if (daysNames.length == 7) {
            //use these new ones
            String[] daysNamesTrimmed = new String[7];
            for (int i = 0; i < daysNames.length; i++) {
                daysNamesTrimmed[i] = daysNames[i].trim();
            }
            this.calendarPanel.setWeekDayNames(daysNamesTrimmed);
        }
        //set first day of the week
        String firstDayOfWeek = watch.getTranslation("filter.dates.firstDayOfWeek");
        //cast the value to the int
        int offset = 0;
        try {
            offset = Integer.parseInt(firstDayOfWeek.trim());
        } catch (NumberFormatException e) {
            //no problem, will use the default 0
        }
        if (offset >= 0 && offset < 7) {
            this.calendarPanel.setFirstDayOffset(offset);
        }        
        FlowPanel p = new FlowPanel();
        Label startLabel = new Label(this.panelTitle);
        startLabel.addStyleName(watch.getStyleName("filter", "date-title-label"));
        p.add(startLabel);
        this.dateLabel.addStyleName(watch.getStyleName("filter", "date-label"));
        p.add(this.dateLabel);
        panel.add(p);
        Button nextMButton = new Button("&gt;");
        nextMButton.setTitle(watch.getTranslation("filter.dates.months.next"));
        nextMButton.addStyleName(watch.getStyleName("filter", "date-month-button"));
        Button prevMButton = new Button("&lt;");
        prevMButton.setTitle(watch.getTranslation("filter.dates.months.previous"));
        prevMButton.addStyleName(watch.getStyleName("filter", "date-month-button"));
        panel.add(prevMButton);
        monthLabel.setText(this.calendarPanel.getCurrentMonthName());
        monthLabel.addStyleName(watch.getStyleName("filter", "date-month-label"));
        panel.add(nextMButton); 
        panel.add(monthLabel);
        Button nextYButton = new Button("&gt;");
        nextYButton.setTitle(watch.getTranslation("filter.dates.years.next"));
        nextYButton.addStyleName(watch.getStyleName("filter", "date-year-button"));
        Button prevYButton = new Button("&lt;");
        prevYButton.setTitle(watch.getTranslation("filter.dates.years.previous"));        
        prevYButton.addStyleName(watch.getStyleName("filter", "date-year-button"));
        panel.add(prevYButton); 
        yearLabel.setText(this.calendarPanel.getCurrentYear());
        yearLabel.addStyleName(watch.getStyleName("filter", "date-year-label"));
        panel.add(nextYButton);
        panel.add(yearLabel);

        this.calendarPanel.addPrevMonthActivator(prevMButton);
        this.calendarPanel.addNextMonthActivator(nextMButton);
        this.calendarPanel.addPrevYearActivator(prevYButton);
        this.calendarPanel.addNextYearActivator(nextYButton);
        this.calendarPanel.addCalendarListener(this);
        //some calendar styling
        this.calendarPanel.setBorderWidth(0);
        this.calendarPanel.setCellPadding(2);
        panel.add(this.calendarPanel);
        this.calendarPanel.redraw();
    }
    
    /**
     * Adds a calendar listener to the embedded calendar widget. Use this to attach whatever
     * functionality is needed when the calendar data changes. 
     * 
     * @param listener
     */
    public void addCalendarListener(CalendarListener listener) {
        this.calendarPanel.addCalendarListener(listener);
    }
    
    /**
     * {@inheritDoc}
     * @see WatchWidget#getName()
     */
    public String getName()
    {
        return "datepicker";
    }
   
    /**
     * {@inheritDoc}
     * @see WatchWidget#resetSelections(String)
     */
    public void resetSelections()
    {
        //set the label to none
        this.dateLabel.setText("");
        //should also set calendar selection to none
    }
    
    /**
     * Gets the date format used for date label formatting.
     * 
     * @return                          date format
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    /**
     * Sets the date format for the date label, as specified by {@link SimpleDateFormat}
     * @see SimpleDateFormat
     * 
     * @param dateFormat                    the new date format
     */
    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }
    
    /**
     * Listener for the embedded {@link CalendarPanel} for changing the date label.
     */
    public void onDateClick(CalendarDate date)
    {
        //set the date label to the selected date
        SimpleDateFormat format = new SimpleDateFormat(this.dateFormat);
        String fDate = format.format(date.getDate());
        this.dateLabel.setText(fDate);
    }

    /**
     * {@inheritDoc}
     * @see CalendarListener#onEventDateClick(CalendarDate)
     */
    public boolean onEventDateClick(CalendarDate date)
    {
        return false;
    }

    /**
     * Listener for the embedded {@link CalendarPanel} for changing the month label and year label.
     */
    public void onMonthChange(CalendarMonth month)
    {
        //update the month label and the year label, if needed
        this.monthLabel.setText(this.calendarPanel.getCurrentMonthName());
        if (!this.calendarPanel.getCurrentYear().trim().equals(this.yearLabel.getText().trim())) {
            //update
            this.yearLabel.setText(this.calendarPanel.getCurrentYear());
        }
    }

    public String getPanelTitle()
    {
        return panelTitle;
    }

    public void setPanelTitle(String panelTitle)
    {
        this.panelTitle = panelTitle;
    }    
}
