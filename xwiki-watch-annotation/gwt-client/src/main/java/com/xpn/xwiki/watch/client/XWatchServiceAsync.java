/* 
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
 */
package com.xpn.xwiki.watch.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.xpn.xwiki.gwt.api.client.XObject;
import com.xpn.xwiki.gwt.api.client.XWikiGWTException;
import com.xpn.xwiki.gwt.api.client.XWikiServiceAsync;
import com.xpn.xwiki.watch.client.annotation.Annotation;
import com.xpn.xwiki.watch.client.data.FeedArticle;

/**
 * The asynchronous interface corresponding to the {@link XWatchService} interface. This interface will be exposed by
 * the Watch application instead of the default {@link XWikiServiceAsync} interface to the Watch code.
 */
public interface XWatchServiceAsync extends XWikiServiceAsync
{   
    public void getArticles(String sql, int nb, int start, AsyncCallback<List<FeedArticle>> cb);
    
    /**
     * Builds and returns a single feed article, built from the object in the passed document.
     * 
     * @param documentName the name of the document for which to fetch and build a feed article
     * @return the FeedArticle object for the feed entry in the passed document 
     * @throws XWikiGWTException if anything goes wrong
     */
    public void getArticle(String documentName, AsyncCallback<FeedArticle> cb);    
    
    public void getConfigDocuments(String watchSpace, AsyncCallback cb);
    
    public void getArticlesCount(String watchSpace, AsyncCallback cb);
    
    public void getNewArticlesCountPerFeeds(String watchSpace, AsyncCallback cb);
    
    public void getTagsList(String watchSpace, String like, AsyncCallback cb);    

    public void getAccessLevels(List rights, String docname, AsyncCallback cb);

    /**
     * Adds a feed aggregator to the wiki, as specified by the parameters.
     * 
     * @param spaceName the space in which to add the feed aggregator document
     * @param feedName the name of the feed, used as a hint to generate an unique name
     * @param feedObject the feed object, completed with all the data
     * @param cb asynchronous callback to handle the response from the server: <code>true</code> if the add was
     *            successful, <code>false</code> otherwise (lack of rights or any other issue)
     */
    public void addFeed(String spaceName, String feedName, XObject feedObject, AsyncCallback<Boolean> cb);
    
    public void getAnnotatedEntryFeed(String documentName, AsyncCallback<String> cb);
    
    public void addAnnotation(String selection, String metadata, String documentName, AsyncCallback<String> cb);
    
    public void removeAnnotation(String documentName, String id, AsyncCallback<String> cb);

    public void getAnnotations(String documentName, AsyncCallback<List<Annotation>> cb);   
}
