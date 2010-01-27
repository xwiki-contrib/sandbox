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
import java.util.Map;

import com.xpn.xwiki.gwt.api.client.XObject;
import com.xpn.xwiki.gwt.api.client.XWikiGWTException;
import com.xpn.xwiki.gwt.api.client.XWikiService;

/**
 * Service to expose Watch specific functions, extending the default XWikiService, to preserve the default functions.
 */
public interface XWatchService extends XWikiService
{
    public List getArticles(String sql, int nb, int start) throws XWikiGWTException;
    
    /**
     * Returns the configuration documents for the instance of Watch in the specified space: the feeds, groups and 
     * the keywords.
     * 
     * @param watchSpace the Watch space
     * @return the list of configuration documents in the specified space
     * @throws XWikiGWTException
     */
    public List getConfigDocuments(String watchSpace) throws XWikiGWTException;

    /**
     * Returns the total number of articles fetched for the specified Watch space.
     */
    public int getArticlesCount(String watchSpace) throws XWikiGWTException;
    
    /**
     * Returns the number of new articles for each feed, for the specified Watch space.
     * This function returns a list of lists in which the first position is the name of the feed and the second position
     * is the number of new articles in that feed.
     */
    public List getNewArticlesCountPerFeeds(String watchSpace) throws XWikiGWTException;
    
    /**
     * Returns the list of tags, each one joined by the tag count, for the specified Watch space. If the <tt>like</tt>
     * parameter is not null, the tags returned will be the ones matching the like expression.
     * 
     * @param watchSpace the Watch space to return the tags for
     * @param like the pattern that returned tags must match. If null, all tags will be returned.
     * @return a list of tags, each one with its count. The returned list will contain lists which will have, on first 
     * position, the tag and on the second position the tag count.
     * @throws XWikiGWTException
     */
    public List getTagsList(String watchSpace, String like) throws XWikiGWTException;
    
    /**
     * Returns the access rights specified in the passed list for the specified resource, for the current user.
     * 
     * @param rights the list of rights to be queried for the specified resource
     * @param docname the resource to query the rights for 
     * @return the access rights specified in the passed list for the specified resource, for the current user.
     */
    public Map getAccessLevels(List rights, String docname) throws XWikiGWTException;

    /**
     * Adds a feed aggregator to the wiki, as specified by the parameters.
     * 
     * @param spaceName the space in which to add the feed aggregator document
     * @param feedName the name of the feed, used as a hint to generate an unique name
     * @param feedObject the feed object, completed with all the data
     * @return <code>true</code> if the add was successful, <code>false</code> otherwise (lack of rights or any other
     *         issue)
     * @throws XWikiGWTException in case something goes wrong during addition
     */
    public boolean addFeed(String spaceName, String feedName, XObject feedObject) throws XWikiGWTException;
}
