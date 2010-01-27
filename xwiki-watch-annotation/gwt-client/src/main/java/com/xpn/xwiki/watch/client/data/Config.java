package com.xpn.xwiki.watch.client.data;

import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.gwt.api.client.Document;
import com.xpn.xwiki.gwt.api.client.XObject;
import com.xpn.xwiki.watch.client.Constants;
import com.xpn.xwiki.watch.client.Watch;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.*;

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
public class Config {
    private Watch watch;
    private Map feedsList;
    private Map feedsByGroupList;
    private List keywords;
    private Map groups;
    private List articles;
    private boolean lastPage;
    
    /*
     * variables to store the access rights for the watch space. By default, they are all true to enable all actions
     * in the UI (even if the actions are present on the UI, if an action is not allowed, it will be signaled on the
     * server side when the action will be attempted).
     */
    private boolean hasEditRight = true;
    private boolean hasDeleteRight = true;
    private boolean hasCommentRight = true;

    public Config() {
    }

    public Config(Watch watch) {
        this.watch = watch;
        clearConfig();
    }

    public Map getFeedsList() {
        return feedsList;
    }

    public Map getFeedsByGroupList() {
        return feedsByGroupList;
    }

    public Map getGroups() {
        return groups;
    }

    public List getKeywords() {
        return keywords;
    }

    public boolean getHasEditRight()
    {
        return hasEditRight;
    }

    public boolean getHasDeleteRight()
    {
        return hasDeleteRight;
    }

    public boolean getHasCommentRight()
    {
        return hasCommentRight;
    }

    public void clearConfig() {
        feedsList = new HashMap();
        feedsByGroupList = new HashMap();
        keywords = new ArrayList();
        groups = new HashMap();
        articles = new ArrayList();
        this.hasCommentRight = this.hasEditRight = this.hasDeleteRight = true;
        this.lastPage = false;
    }

    /**
     * Read the feed list, groups and keywords on the server
     */
    public void refreshConfig(final XWikiAsyncCallback cb) {
        watch.getDataManager().getConfigurationDocuments(new XWikiAsyncCallback(watch) {
            public void onSuccess(Object result) {
                super.onSuccess(result);
                List feedDocuments = (List) result;
                // Reset the data
                feedsList = new HashMap();
                feedsByGroupList = new HashMap();
                keywords = new ArrayList();
                groups = new HashMap();
                for (int index=0;index<feedDocuments.size();index++) {
                        addToConfig((Document) feedDocuments.get(index));
                }
                if (cb!=null)
                 cb.onSuccess(result);
            }
        });
    }
    
    public void refreshArticleList(final AsyncCallback cb) {
        FilterStatus fstatus = watch.getFilterStatus();
        //hack to test the existance of next articles: ask for one more: 
        //if we get it, then we have more articles
        watch.getDataManager().getArticles(fstatus, watch.getArticleNbParam() + 1, fstatus.getStart(), new AsyncCallback() {
            public void onFailure(Throwable caught) {
                if (cb != null) {
                    cb.onFailure(caught);
                }
            }

            public void onSuccess(Object result) {
                //test the size of the list
                List resultList = (List)result;
                if (resultList.size() == (watch.getArticleNbParam() + 1)) {
                    //we have next
                    lastPage = false;
                } else {
                    lastPage = true;
                }
                //remove the last element if fetched for next
                if (!lastPage) {
                    resultList.remove(resultList.size() - 1);
                }
                //update the article list
                updateArticleList(resultList);
                if (cb != null) {
                    cb.onSuccess(result);
                }
            }
        });
    }
    
    private void updateArticleList(List result) {
        this.articles.clear();
        //update the articles list
        for (Iterator rIt = result.iterator(); rIt.hasNext();) {
            this.articles.add(new FeedArticle((Document)rIt.next()));
        }
    }    
    
    public void refreshRights(final AsyncCallback cb)
    {
        List rightsList = new ArrayList();
        rightsList.add("edit"); rightsList.add("comment"); rightsList.add("delete");
        //test rights on the Reader document in Watch space, assuming that the rights on this document are 
        // the ones on the space.
        String documentName = watch.getWatchSpace() + "." + Constants.PAGE_READER;
        watch.getXWatchServiceInstance().getAccessLevels(rightsList, documentName, new AsyncCallback() {
            public void onSuccess(Object result) {
                updateRights((Map)result);
                if (cb != null) {
                    cb.onSuccess(result);
                }
            }
            public void onFailure(Throwable t) {
                if (cb != null) {
                    cb.onFailure(t);
                }
            }
        });
    }
    
    private void updateRights(Map result) {
        if (result.get("edit") != null) {
            this.hasEditRight = ((Boolean)result.get("edit")).booleanValue();
        }
        if (result.get("delete") != null) {
            this.hasDeleteRight = ((Boolean)result.get("delete")).booleanValue();
        }
        if (result.get("comment") != null) {
            this.hasCommentRight = ((Boolean)result.get("comment")).booleanValue();
        }
    }

    private void addToGroup(String group, Feed feed) {
        Map feeds = (Map) feedsByGroupList.get(group);
        if (feeds == null) {
            feeds = new HashMap();
            feedsByGroupList.put(group, feeds);
        }
        if (feed!=null)
         feeds.put(feed.getName(), feed);
        if (!groups.containsKey(group)) {
            Group newGroup = new Group();
            newGroup.setName(group);
            groups.put(group, newGroup);
        }
    }

    private void addToConfig(Document feedpage) {
        List fobjects = feedpage.getObjects("XWiki.AggregatorURLClass");
        if (fobjects!=null) {
            for (int i=0;i<fobjects.size();i++) {
                XObject xobj = (XObject) fobjects.get(i);
                Feed feed = new Feed(xobj);
                List feedgroups = feed.getGroups();
                if (feedgroups!=null) {
                    for (int j=0;j<feedgroups.size();j++) {
                        String groupFullName = (String) feedgroups.get(j);
                        addToGroup(groupFullName, feed);
                    }
                }
                String all = watch.getTranslation("all");
                addToGroup(all, feed);
                feedsList.put(feed.getName(), feed);
            }
        }
        List kobjects = feedpage.getObjects("XWiki.KeywordClass");
        if (kobjects!=null) {
            for (int j=0;j<kobjects.size();j++) {
                XObject xobj = (XObject) kobjects.get(j);
                Keyword keyword = new Keyword(xobj);
                keywords.add(keyword);
            }
         }

        List gobjects = feedpage.getObjects("XWiki.AggregatorGroupClass");
        if (gobjects!=null) {
            for (int j=0;j<gobjects.size();j++) {
                XObject xobj = (XObject) gobjects.get(j);
                Group group = new Group(xobj);
                if ((group.getName()!=null)&&(!group.getName().equals("")))
                    groups.put(feedpage.getFullName(), group);
            }
         }

    }

     public void refreshArticleNumber(final AsyncCallback cb) {
         // Load the article counts
        watch.getDataManager().getNewArticlesCount(new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                if (cb != null) {
                    cb.onFailure(throwable);
                }
            }
            public void onSuccess(Object result) {
                // Update the article list with the current results
                updateArticleNumbers((List) result);
                //call the cb.onSuccess if cb exists
                if (cb != null) {
                    cb.onSuccess(result);
                }
            }
        });
     }

    public void updateArticleNumbers(List list) {
        if (list!=null) {
            for (int i=0;i<list.size();i++) {
                List result = (List) list.get(i); 
                String feedname = (String) result.get(0).toString();
                Integer count = new Integer(((Number)result.get(1)).intValue());
                Feed feed = (Feed) feedsList.get(feedname);
                if (feed!=null) {
                   feed.setNb(count);
                }
            }
        }
        // watch.refreshFeedTreeUI();
    }

    public List getArticles()
    {
        return articles;
    }

    public boolean isLastPage()
    {
        return lastPage;
    }

}
