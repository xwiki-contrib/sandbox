package com.xpn.xwiki.watch.client.data;

import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XObject;
import com.xpn.xwiki.watch.client.Constants;

import java.util.List;
import java.util.ArrayList;

public class Feed {
    private String pageName;
    private String name;
    private String title;
    private String url;
    private String imgurl;
    private List groups;
    private String date;
    private Integer nb;

    public Feed() {
    }

    public Feed(Document doc )  {
        this(doc.getObject("XWiki.AggregatorURLClass"));
    }

    public Feed(XObject xobj)  {
        setPageName(xobj.getName());
        String feedName = (String) xobj.getProperty(Constants.PROPERTY_AGGREGATOR_URL_NAME); 
        setName(feedName);
        setUrl((String) xobj.getProperty(Constants.PROPERTY_AGGREGATOR_URL_URL));
        setGroups((List) xobj.getProperty(Constants.PROPERTY_AGGREGATOR_URL_GROUPS));
        setDate((String) xobj.getViewProperty(Constants.PROPERTY_AGGREGATOR_URL_DATE));
        String setTitleProperty = (String)xobj.getViewProperty(Constants.PROPERTY_AGGREGATOR_URL_TITLE);
        //if title property is not set, set it from the feed name
        if (setTitleProperty.trim().length() == 0) {
            setTitle(feedName);
        } else {
            setTitle(setTitleProperty);
        }
        setNb(new Integer(0));
        setImgurl((String)xobj.getProperty(Constants.PROPERTY_AGGREGATOR_URL_IMAGEURL));
    }

    public String getName() {
        return (name==null) ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return (url==null) ? "" : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List getGroups() {
        return (groups==null) ? new ArrayList() : groups;
    }

    public void setGroups(List groups) {
        this.groups = groups;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getNb() {
        return nb;
    }
    

    public void setNb(Integer nb) {
        this.nb = nb;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getPageName() {
        return (pageName==null) ? "" : pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getTitle()
    {
        return title == null ? "" : title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}
