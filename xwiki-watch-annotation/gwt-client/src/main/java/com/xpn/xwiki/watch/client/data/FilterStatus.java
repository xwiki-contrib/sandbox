package com.xpn.xwiki.watch.client.data;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.gwtwidgets.client.util.SimpleDateFormat;

import com.google.gwt.http.client.URL;
import com.xpn.xwiki.watch.client.Constants;
import com.xpn.xwiki.watch.client.Watch;

public class FilterStatus {
    protected Watch watch;
    private int flagged;
    private int trashed;
    private int read;
    private List tags = new ArrayList();
    private String keyword;
    private Feed feed;
    private String group;
    private Date dateStart;
    private Date dateEnd;
    private int start;
    private int total;
    private String query;

    public FilterStatus() {
        reset();        
    };

    public FilterStatus(Watch watch) {
        this();
        this.watch = watch;
    }

    public void reset() {
        flagged = 0;
        trashed = -1;
        read = 0;
        tags = new ArrayList();
        keyword = null;
        feed = null;
        group = null;
        dateStart = null;
        dateEnd = null;
        start = 0;
    }

    public String toString() {
        String status = "";
        if (feed!=null) {
            status = watch.getTranslation("feed");
            status += " " + feed;
        }
        if (group!=null) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("group");
            status += " " + group;
        }
        if (keyword!=null) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("keyword");
            status += " " + keyword;
        }
        if (tags.size()>0) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("tags");
            for (int i=0;i<tags.size();i++)
                status += " " + tags.get(i);
        }
        if (flagged ==1) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("flagon");
        }
        if (flagged ==-1) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("flagoff");
        }
        if (trashed == 1) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("trashedon");
        }
        if (trashed ==-1) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("trashedoff");
        }

        if (read==1) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("readon");
        }
        if (read==-11) {
            if (!status.equals(""))
                status += " - ";
            status += watch.getTranslation("readoff");
        }
        if (dateStart != null) {
            if (!status.equals("")) {
                status += " - ";
            }
            status += watch.getTranslation("filter.dates.startDate");
            status += dateStart; 
        }
        if (dateEnd != null) {
            if (!status.equals("")) {
                status += " - ";
            }
            status += watch.getTranslation("filter.dates.endDate");
            status += dateEnd; 
        }
        return status;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getFlagged() {
        return flagged;
    }

    public void setFlagged(int flagged) {
        this.flagged = flagged;
    }

    public int getTrashed() {
        return trashed;
    }

    public void setTrashed(int trashed) {
        this.trashed = trashed;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public List getTags() {
        return tags;
    }

    public void setTags(List tags) {
        this.tags = tags;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public Map getMap() {
        Map map = new HashMap();
        map.put("flagged", "" + getFlagged());
        map.put("trashed", "" + getTrashed());
        map.put("read", "" + getRead());
        if (getFeed() !=null)
            map.put("feed",getFeed().getUrl());
        if (getGroup() !=null)
            map.put("group",getGroup());
        if (getTags().size()>0)
            map.put("tags", getTags());
        if (getKeyword() !=null)
            map.put("keyword",getKeyword());
        if (getDateStart() != null) {
            //format date first
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
            String fDate = format.format(getDateStart());
            map.put("dateStart", fDate);
        }
        if (getDateEnd() != null) {
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
            String fDate = format.format(getDateEnd());
            map.put("dateEnd", fDate);
        }
        return map;
    }

    public String getQueryString() {
        StringBuffer qs = new StringBuffer();
        qs.append("&amp;flagged=" + getFlagged());
        qs.append("&amp;trashed=" + getTrashed());
        qs.append("&amp;read=" + getRead());
        if (getFeed() !=null)
            qs.append("&amp;feed=" + URL.encodeComponent(getFeed().getUrl()));
        if (getGroup() !=null)
            qs.append("&amp;group=" + getGroup());
        if (getTags().size()>0) {
            for (int i=0;i<getTags().size();i++) {
                String tag = (String) getTags().get(i);
                qs.append("&amp;tags=" +  tag);
            }
        }
        if (getKeyword() !=null)
            qs.append("&amp;keyword=" + getKeyword());
        if (getDateStart() != null) {
            //format the date first
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
            String fDate = format.format(getDateStart());
            qs.append("&amp;dateStart=" + URL.encodeComponent(fDate));
        }
        if (getDateEnd() != null) { 
            SimpleDateFormat format = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
            String fDate = format.format(getDateEnd());
            qs.append("&amp;dateEnd=" + URL.encodeComponent(fDate));
        }
        return qs.toString();
    }

    public Date getDateEnd()
    {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd)
    {
        //make sure this date is at 23:59
        //TODO: never use deprecated API!!!
        if (dateEnd != null) {
            dateEnd.setHours(23);
            dateEnd.setMinutes(59);
        }
        this.dateEnd = dateEnd;
    }

    public Date getDateStart()
    {
        return dateStart;
    }

    public void setDateStart(Date dateStart)
    {
        this.dateStart = dateStart;
    }

}
