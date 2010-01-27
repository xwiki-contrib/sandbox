package com.xpn.xwiki.watch.client;

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
 * @author ldubost
 */

public class Constants {

    public Constants() {};

    public static final String XWATCH_SERVICE = "/XWatchService";

    public static final int DEFAULT_PARAM_NB_ARTICLES_PER_PAGE = 10;
    public static final int DEFAULT_PARAM_NEWARTICLES_MONITORING_TIMER = 30000;
    public static final String DEFAULT_LOCALE = "en";
    // "remote" means favicon are directly taken from feed site
    // "local" means favicon are taken from XWiki 
    public static final String DEFAULT_FEEDS_FAVICON = "remote";

    public static final String WEBAPPNAME = "xwiki";

    public static final String APPNAME = "watch";
    public static final String CSS_PREFIX = "watch";

    public static final String DEFAULT_WATCH_SPACE = "Watch";
    public static final String DEFAULT_CODE_SPACE = "WatchCode";
    public static final String DEFAULT_SHEETS_SPACE = "WatchSheets";
    public static final String DEFAULT_TEMPLATE_SPACE = "WatchTemplate";
    public static final String DEFAULT_TRANSLATIONS_PAGE = "WatchCode.Translations";
    
    //Sheets
    public static final String SHEET_FEED = "FeedSheet";
    public static final String SHEET_GROUP = "GroupSheet";
    public static final String SHEET_KEYWORD = "KeywordSheet";

    // Analysis actions
    public static final String PAGE_PRESSREVIEW = "PressReview";
    public static final String PAGE_PRESSREVIEW_RSS  = "PressReviewRss";
    public static final String PAGE_TAGCLOUD = "TagCloud";
    public static final String PAGE_LOADING_STATUS = "LoadingStatus";
    public static final String PAGE_PREVIEW_FEED = "PreviewFeed";
    public static final String PAGE_EMAIL_PRESSREVIEW = "SendPressReviewEmail";
    
    public static final String PAGE_READER = "Reader";

    public static final String IMAGE_COMMENT = "comment.png";
    public static final String IMAGE_TAG = "tag.png";
    public static final String IMAGE_FEED = "feed.png";
    public static final String IMAGE_USER = "user.png";
    public static final String IMAGE_DELETE = "delete.png";
    public static final String IMAGE_DELETE_ACTIVE = "delete-active.png";
    
    public static final String IMAGE_PRESS_REVIEW = "pressreview.png";
    public static final String IMAGE_ANALYSIS = "analysis.png";
    public static final String IMAGE_HIDE_READ = "show-read.png";
    public static final String IMAGE_SHOW_READ = "hide-read.png";
    public static final String IMAGE_CONFIG = "config.png";
    public static final String IMAGE_REFRESH = "refresh.png";
    public static final String IMAGE_MORE = "more.png";
    public static final String IMAGE_FLAG_ON = "news-flag2.png";
    public static final String IMAGE_FLAG_OFF = "news-noflag2.png";
    public static final String IMAGE_TRASH_ON = "news-trash2.png";
    public static final String IMAGE_TRASH_OFF = "news-untrash2.png";
    public static final String IMAGE_EXT_LINK = "news-ext2.png";
    public static final String IMAGE_LOADING_SPINNER = "spinner.gif";

    public static final String CLASS_AGGREGATOR_URL = "XWiki.AggregatorURLClass";
    public static final String PROPERTY_AGGREGATOR_URL_NAME = "name";
    public static final String PROPERTY_AGGREGATOR_URL_TITLE = "title";
    public static final String PROPERTY_AGGREGATOR_URL_URL = "url";
    public static final String PROPERTY_AGGREGATOR_URL_GROUPS = "group";
    public static final String PROPERTY_AGGREGATOR_URL_DATE = "date";
    public static final String PROPERTY_AGGREGATOR_URL_IMAGEURL = "imgurl";
    public static final String CLASS_AGGREGATOR_KEYWORD = "XWiki.KeywordClass";
    public static final String PROPERTY_KEYWORD_NAME = "name";
    public static final String PROPERTY_KEYWORD_GROUP = "group";
    public static final String CLASS_AGGREGATOR_GROUP = "XWiki.AggregatorGroupClass";
    public static final String PROPERTY_GROUP_NAME = "name";
    public static final String PROPERTY_TAGS_SEPARATORS_EDIT = " ";
    public static final String PROPERTY_TAGS_SEPARATOR_DISPLAY = " ";    
    
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm";
}
