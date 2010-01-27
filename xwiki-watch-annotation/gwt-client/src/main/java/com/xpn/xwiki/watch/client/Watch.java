package com.xpn.xwiki.watch.client;

import com.xpn.xwiki.gwt.api.client.XWikiServiceAsync;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTDefaultApp;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTAppConstants;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.watch.client.ui.UserInterface;
import com.xpn.xwiki.watch.client.ui.dialog.AnalysisDialog;
import com.xpn.xwiki.watch.client.ui.wizard.ConfigWizard;
import com.xpn.xwiki.watch.client.ui.wizard.PressReviewWizard;
import com.xpn.xwiki.watch.client.data.Config;
import com.xpn.xwiki.watch.client.data.DataManager;
import com.xpn.xwiki.watch.client.data.Feed;
import com.xpn.xwiki.watch.client.data.FilterStatus;
import com.xpn.xwiki.watch.client.data.Group;
import com.xpn.xwiki.watch.client.data.Keyword;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

public class Watch extends XWikiGWTDefaultApp implements EntryPoint {
    protected Config config;
    protected UserInterface userInterface;
    protected DataManager dataManager;
    private FilterStatus filterStatus = new FilterStatus();
    protected  NewArticlesMonitoring newArticlesMonitoring;
    protected String watchSpace = null;
    
    public Watch() {
    }

    public String getName() {
        return Constants.APPNAME;
    }

    /**
     * Allows to access the name of the translations page provided in gwt parameters
     * @return
     */
    public String getTranslationPage() {
        return getParam("translations", Constants.DEFAULT_TRANSLATIONS_PAGE);
    }

    public String getLocale() {
        return getParam("locale", Constants.DEFAULT_LOCALE);
    }
    
    public String getCSSPrefix() {
        return Constants.CSS_PREFIX;
    }

    public String getStyleName(String cssname) {
        return getCSSPrefix() + "-" + cssname;
    }

    public String getStyleName(String module, String cssname) {
        return getCSSPrefix() + "-" + module + "-" + cssname;
    }

    public Config getConfig() {
        return config;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }

    public void startServerLoading() {
        Map map = filterStatus.getMap();
        map.put("space", getWatchSpace());
        getXWikiServiceInstance().getDocumentContent(Constants.DEFAULT_CODE_SPACE + "." + Constants.PAGE_LOADING_STATUS, true, map, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
            }
            public void onSuccess(Object object) {
            }
        });
    }

    public void forceServerLoading() {
        Map map = filterStatus.getMap();
        map.put("space", getWatchSpace());
        map.put("force", "1");
        getXWikiServiceInstance().getDocumentContent(Constants.DEFAULT_CODE_SPACE + "." + Constants.PAGE_LOADING_STATUS, true, map, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
            }
            public void onSuccess(Object object) {
            }
        });
    }

    /**
     * Current watch space
     * @return
     */

    public String getWatchSpace() {
        return (watchSpace==null) ? getParam("watchspace", Constants.DEFAULT_WATCH_SPACE) : watchSpace;
    }

    public void setWatchSpace(String watchSpace) {
        this.watchSpace = watchSpace;
    }

    public String getSkinFile(String file) {
        String name = "watch-" + file;
        if ("1".equals(getParam("useskin")))
         return super.getSkinFile(name);
        else {
            String imagePath = getParam("resourcepath", "");
            if (imagePath.equals(""))
             return name;
            else
             return imagePath + "/" + name;
        }
    }

    public FilterStatus getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(FilterStatus filterStatus) {
        this.filterStatus = filterStatus;
    }

    public void onModuleLoad() {
        if (!GWT.isScript()) {
            getXWikiServiceInstance().login("Admin", "admin", true, new XWikiAsyncCallback(this) {
                public void onFailure(Throwable caught) {
                    super.onFailure(caught);
                }
                public void onSuccess(Object result) {
                    super.onSuccess(result);    
                    onModuleLoad(false);
                }
            });
        } else {
            onModuleLoad(false);
        }
    }

    
    public void onModuleLoad(boolean translationDone) {
        if (!translationDone) {
            checkTranslator(new XWikiAsyncCallback(this) {
                public void onFailure(Throwable caught) {
                    super.onFailure(caught);
                    onModuleLoad(true);
                }

                public void onSuccess(Object result) {
                    super.onSuccess(result);
                    onModuleLoad(true);
                }
            });
            return;
        }
        // Launch monitoring of new incoming article which is updating the title bar
        newArticlesMonitoring = new NewArticlesMonitoring(this);

        config = new Config(this);
        userInterface = new UserInterface(this);
        dataManager = new DataManager(this);
        
        // Launch the UI
        RootPanel.get("Watch").add(userInterface);

        // Load the feed list and other info
        config.refreshConfig(new XWikiAsyncCallback(this) {
            public void onSuccess(Object object) {
                super.onSuccess(object);
                //load the rights too
                Watch.this.config.refreshRights(new XWikiAsyncCallback(Watch.this) {
                    public void onSuccess(Object result) {
                        super.onSuccess(result);
                        // Refresh the Feed Tree UI
                        Watch.this.userInterface.init();
                        Watch.this.userInterface.refreshData("feedtree");
                        // Load the number of articles for each feed
                        Watch.this.refreshArticleNumber();
                        // Refresh tag cloud asynchronously
                        Watch.this.refreshTagCloud();
                        Watch.this.refreshKeywords();
                        Watch.this.refreshArticleList();
                        // Make sure server has started loading feed
                        Watch.this.startServerLoading();
                        Watch.this.userInterface.resizeWindow();
                    }
                });
            }
        });
    }

    private void refreshKeywords() {
        userInterface.refreshData("keywords");
    }

    public void refreshTagCloud() {
        userInterface.refreshData("tagcloud");
    }
    
    /**
     * Refreshes the article number and handles the interface refresh triggered by this 
     * data update, if necessary.
     */
    public void refreshArticleNumber() {
        getConfig().refreshArticleNumber(new AsyncCallback() {
            public void onSuccess(Object arg0)
            {
                //update the interface objects affected by this change
                userInterface.refreshData("feedtree");
            }
            public void onFailure(Throwable arg0)
            {
                //silent failure
            }
        });
    }

    public void refreshOnTagActivated(String tagName) {
        FilterStatus fstatus = getFilterStatus();
        List tags = filterStatus.getTags();
        if (tags.contains(tagName))
         tags.remove(tagName);
        else
         tags.add(tagName);
        getFilterStatus().setStart(0);
        refreshArticleList();
        userInterface.resetSelections("tagcloud");
    }

    
    public String getTitleBarText() {
        int nbArticles = newArticlesMonitoring.getArticlesNumber();
        Date lastChange =  newArticlesMonitoring.lastChangeDate();

        String[] args = new String[2];
        args[0] = "" + nbArticles;
        args[1] = (lastChange==null) ? "" : lastChange.toString();
        return getTranslation("title", args);
    }

    /**
     * Refresh the Article List
     */
    public void refreshArticleList() {
        config.refreshArticleList(new XWikiAsyncCallback(this) {
            public void onFailure(Throwable caught)
            {
                super.onFailure(caught);
            }
            public void onSuccess(Object result)
            {
                super.onSuccess(result);
                userInterface.refreshData("articlelist");
                newArticlesMonitoring.stopBlinking();
            }
        });
    }

    /**
     * Refresh the feed tree UI.
     * First refresh the configuration that holds the state of feeds lists and group
     * Then refresh the user interface that displays the feed tree
     */
    public void refreshFeedTree() {
        config.refreshConfig(new XWikiAsyncCallback(this) {
            public void onSuccess(Object object) {
                super.onSuccess(object);
                // Refresh the feed tree with refreshed config data
                userInterface.refreshData("feedtree");
                // Load the number of articles for each feed
                refreshArticleNumber();
            }
        });        
    }

    public void refreshOnSearch(String text) {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setKeyword(text);
        refreshArticleList();
        //set the search box selection
        userInterface.resetSelections("search");
    }

    /**
     * A feed has been clicked. We need to:
     *  - invalidate the group setting
     *  - invalidate the start number
     *  - set the Feed that has been clicked
     *  - refreshData the article list
     *  - refreshData UI elements visuals
     * @param feed to activate
     */
    public void refreshOnFeedChange(Feed feed) {
        getFilterStatus().setFeed(feed);
        getFilterStatus().setGroup(null);
        getFilterStatus().setStart(0);
        //also refresh the tree, to set the current selected feed
        refreshFeedTree();
        refreshArticleList();
    }

    /**
     * A feed has been deleted: we must update the FilterStatus (remove the feed as the currently
     * selected feed) and refresh interface objects impacted by this: feed tree,
     * article list and the tag cloud if the articles of the feed have also been deleted.
     *  
     * @param feed deleted feed
     * @param withArticles true if articles fetched for this feed have also been deleted,
     *        false otherwise
     */
    public void refreshOnFeedDelete(Feed feed, boolean withArticles) {
        getFilterStatus().setFeed(null);
        getFilterStatus().setGroup(null);
        getFilterStatus().setStart(0);
        refreshFeedTree();
        refreshArticleList();
        if (withArticles) {
            //do some other interface updates
            refreshTagCloud();
        }
    }

    /**
     * A group has been clicked. We need to:
     *  - invalidate the feed setting
     *  - invalidate the start number
     *  - set the Group that has been clicked
     *  - refreshData the article list
     *  - refreshData UI elements visuals
     * @param groupName group to activate
     */
    public void refreshOnGroupChange(String groupName) {
        getFilterStatus().setFeed(null);
        if (groupName.equals(getTranslation("all")))
            getFilterStatus().setGroup(null);
        else
            getFilterStatus().setGroup(groupName);
        getFilterStatus().setStart(0);
        refreshArticleList();
        //also refresh the tree, to set the current selected group
        refreshFeedTree();
    }

    /**
     * Previous has been clicked. We need to:
     *  - add the number of articles to the "start" filter setting
     *  - refreshData the article list
     *  - refreshData UI elements visuals
     */
    public void refreshOnPrevious() {
        int currentStart = getFilterStatus().getStart();
        int start =  currentStart - getArticleNbParam();
        start = (start<0) ? 0 : start;
        if (currentStart!=start) {
            getFilterStatus().setStart(start);
            refreshArticleList();
        }
    }

    /**
     * Next has been clicked. We need to:
     *  - substract the number of articles to the "start" filter setting
     *  - refreshData the article list
     *  - refreshData UI elements visuals
     */
    public void refreshOnNext() {
        getFilterStatus().setStart(getFilterStatus().getStart() + getArticleNbParam());
        refreshArticleList();
    }

    public void refreshOnHideReadArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setRead(-1);
        refreshArticleList();
    }

    public void refreshOnShowReadArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setRead(0);
        refreshArticleList();
    }

    public void refreshOnNewFeed() {
        refreshFeedTree();
    }
    
    public void refreshOnUpdateFeed() {
        // Refresh the feedtree interface and the article list interface, after refreshing the data from the server.
        // TODO: the server data refresh is not needed for this case, we could use locally fetched data but we 
        // take advantage of this situation and make a full refresh, until automatic refresh will be implemented.
        config.refreshConfig(new XWikiAsyncCallback(this) {
            public void onSuccess(Object object) {
                super.onSuccess(object);
                // Refresh the feed tree with refreshed config data
                userInterface.refreshData("feedtree");
                userInterface.refreshData("articlelist");
                // Load the number of articles for each feed
                refreshArticleNumber();
            }
        });        
    }

    public void refreshOnNewKeyword() {
        config.refreshConfig(new XWikiAsyncCallback(this) {
            public void onSuccess(Object object) {
                super.onSuccess(object);
                userInterface.refreshData("keywords");
            }
        });
    }

    public void refreshOnNewGroup() {
        refreshFeedTree();
    }

    public void refreshOnResetFilter() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setFlagged(0);
        fstatus.setKeyword(null);
        fstatus.setRead(0);
        fstatus.setStart(0);
        fstatus.setTags(new ArrayList());
        fstatus.setTrashed(-1);
        fstatus.setDateStart(null);
        fstatus.setDateEnd(null);
        // Also refresh the tree selections
        fstatus.setGroup(null);
        fstatus.setFeed(null);
        refreshArticleList();
        userInterface.resetSelections();        
    }

    public void refreshOnActivateKeyword(Keyword keyword) {
        FilterStatus fstatus = getFilterStatus();
        if (keyword == null) {
            // Cancel current filter
            fstatus.setKeyword(null);
        } else {
            // Set keyword and group
            fstatus.setKeyword(keyword.getName());
            // Remove feed selection, since group and feed are exclusive
            fstatus.setFeed(null);
            fstatus.setGroup(keyword.getGroup());
        }
        fstatus.setStart(0);
        refreshArticleList();
        userInterface.resetSelections("keywords");
        userInterface.resetSelections("feedtree");
    }

    public String getFavIcon(Feed feed) {
        if (getParam("feeds_favicon", Constants.DEFAULT_FEEDS_FAVICON).equals("remote")) {
            String url = feed.getUrl();
            int i=url.indexOf("/", 10);
            if (i==-1)
                return null;
            else
                return url.substring(0,i) + "/favicon.ico";
        } else {
            return getDownloadUrl(feed.getPageName(), feed.getName() + ".ico");
        }
    }

    public String getDownloadUrl(String pageName, String filename) {
        return XWikiGWTAppConstants.XWIKI_DEFAULT_BASE_URL + "/" + XWikiGWTAppConstants.XWIKI_DEFAULT_ACTION_PATH
               + "/download/" + pageName.replaceAll("\\.", "/") + "/" + filename;
    }

    public String getViewUrl(String pageName, String querystring) {
        return XWikiGWTAppConstants.XWIKI_DEFAULT_BASE_URL + "/" + XWikiGWTAppConstants.XWIKI_DEFAULT_ACTION_PATH
               + "/view/" + pageName.replaceAll("\\.", "/") +  "?" + querystring;
    }

    public String getPDFUrl(String pageName, String querystring) {
        return XWikiGWTAppConstants.XWIKI_DEFAULT_BASE_URL + "/" + XWikiGWTAppConstants.XWIKI_DEFAULT_ACTION_PATH
               + "/pdf/" + pageName.replaceAll("\\.", "/") +  "?" + querystring;
    }

    public void openPressReviewWizard() {
        // Placeholder for PR
        PressReviewWizard wizard = new PressReviewWizard(this, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
            }

            public void onSuccess(Object object) {
            }
        });
        wizard.launchWizard();
    }

    public void openAnalysisWizard() {
        // Placeholder for Analysis
        String[] languages = {"en", "fr"};
        AnalysisDialog analysisDialog = new AnalysisDialog(this, "analysis", Dialog.BUTTON_CANCEL, 
                languages);
        analysisDialog.show();
    }

    public void launchConfig(String tabName) {
        if (tabName.equals("feeds")) {
            // Launch the add feed wizard
            ConfigWizard addfeedwizard = new ConfigWizard(this, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    if (throwable != null) {
                        Window.alert("failed: " + throwable.getMessage());
                    }
                }

                public void onSuccess(Object object) {
                }
            });
            addfeedwizard.launchWizard();
        }
    }

    public void addFeed(final Feed feed, final AsyncCallback cb) {
        getDataManager().addFeed(feed, new XWikiAsyncCallback(this) {
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                // getConfig().addFeed((String) result, feed);
                refreshOnNewFeed();
                cb.onSuccess(result);
            }
        });
    }

    public void addKeyword(final Keyword keyword, final AsyncCallback cb) {
        getDataManager().addKeyword(keyword, new XWikiAsyncCallback(this) {
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                // getConfig().addKeyword((String) result, keyword, group);
                refreshOnNewKeyword();
                cb.onSuccess(result);
            }
        });
    }

    public void addGroup(final Group group, final AsyncCallback cb) {
        getDataManager().addGroup(group, new XWikiAsyncCallback(this) {
            public void onFailure(Throwable caught) {
                super.onFailure(caught);
                cb.onFailure(caught);
            }

            public void onSuccess(Object result) {
                super.onSuccess(result);
                // getConfig().addGroup((String) result, group);
                refreshOnNewGroup();
                cb.onSuccess(result);
            }
        });
    }

    public int getArticleNbParam() {
        return getParamAsInt("nb_articles_per_page", Constants.DEFAULT_PARAM_NB_ARTICLES_PER_PAGE);
    }

    public void refreshOnShowOnlyFlaggedArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setFlagged(1);
        // If trashed is set in the filter, unset it
        if (fstatus.getTrashed() == 1) {
            fstatus.setTrashed(0);
        }
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnNotShowOnlyFlaggedArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setFlagged(0);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnShowOnlyReadArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setRead(1);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnNotShowOnlyReadArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setRead(0);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnShowOnlyUnReadArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setRead(-1);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnNotShowOnlyUnReadArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setRead(0);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnShowOnlyTrashedArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setTrashed(1);
        // If flagged is set in the filter, unset it 
        if (fstatus.getFlagged() == 1) {
            fstatus.setFlagged(0);
        }
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnNotShowOnlyTrashedArticles() {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setTrashed(-1);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnDateStartChange(Date newDate) {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setDateStart(newDate);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public void refreshOnDateEndChange(Date newDate) {
        FilterStatus fstatus = getFilterStatus();
        fstatus.setDateEnd(newDate);
        fstatus.setStart(0);
        refreshArticleList();
    }

    public String[] getPressReviewPages() {
        return getParam("pressreviewpages",Constants.DEFAULT_CODE_SPACE + "." + Constants.PAGE_PRESSREVIEW).split(",");
    }

    public XWikiServiceAsync getXWikiServiceInstance()
    {
        // Return the XWatchService instance
        return getXWatchServiceInstance();
    }

    public XWatchServiceAsync getXWatchServiceInstance()
    {
        if (serviceInstance == null) {
            serviceInstance = (XWikiServiceAsync) GWT.create(XWatchService.class);
            String serviceUrl = getProperty("serviceurl");
            if ((serviceUrl == null) || (serviceUrl.equals(""))) {
                if (GWT.isScript()) {
                    serviceUrl = XWikiGWTAppConstants.XWIKI_DEFAULT_BASE_URL + Constants.XWATCH_SERVICE;
                } else {
                    // Since GWT does not document the format of the URL returned by this function
                    // and it seems to have changed from the last version, we do a test
                    String moduleBaseURL = GWT.getModuleBaseURL();
                    if (moduleBaseURL.endsWith("/")) {
                        moduleBaseURL = moduleBaseURL.substring(0, moduleBaseURL.length() - 1);
                    }
                    serviceUrl = moduleBaseURL + Constants.XWATCH_SERVICE;
                }            	
            }
            ((ServiceDefTarget) serviceInstance).setServiceEntryPoint(serviceUrl);
        }
        return (XWatchServiceAsync) serviceInstance;
    }
}
