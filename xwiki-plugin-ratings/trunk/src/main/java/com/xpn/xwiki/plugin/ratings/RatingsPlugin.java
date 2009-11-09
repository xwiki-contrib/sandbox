/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xpn.xwiki.plugin.ratings;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.plugin.ratings.internal.DefaultRatingsManager;

/**
 * @version $Id: $
 */
public class RatingsPlugin extends XWikiDefaultPlugin
{
    private static Log LOG = LogFactory.getLog(RatingsPlugin.class);

    private final static Object RATINGS_MANAGER_LOCK = new Object();

    public static final String RATINGS_PLUGIN_NAME = "ratings";

    private RatingsManager ratingsManager;

    public RatingsPlugin(String name, String className, XWikiContext context)
    {
        super(name, className, context);
        init(context);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#getName()
     */
    public String getName()
    {
        return RATINGS_PLUGIN_NAME;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.xpn.xwiki.plugin.XWikiDefaultPlugin#getPluginApi
     */
    public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context)
    {
        return new RatingsPluginApi((RatingsPlugin) plugin, context);
    }

    public RatingsManager getRatingsManager(XWikiContext context)
    {
        synchronized (this.RATINGS_MANAGER_LOCK) {
            if (this.ratingsManager == null) {
                String ratingsManagerClass;
                ratingsManagerClass =
                    context.getWiki().Param("xwiki.ratings.ratingsmanager",
                        "com.xpn.xwiki.plugin.ratings.internal.SeparatePageRatingsManager");

                if (LOG.isDebugEnabled())
                    LOG.debug("Init comments manager with class " + ratingsManagerClass);

                try {
                    this.ratingsManager = (RatingsManager) Class.forName(ratingsManagerClass).newInstance();
                } catch (Exception e) {
                    if (LOG.isErrorEnabled())
                        LOG.error("Could not init ragints manager for class " + ratingsManagerClass, e);
                    this.ratingsManager = new DefaultRatingsManager();
                }
            }
        }

        return this.ratingsManager;
    }

    public void init(XWikiContext context)
    {
        try {
            getRatingsManager(context).init(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
            getRatingsManager(context).virtualInit(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRatingsManager(RatingsManager ratingsManager)
    {
        this.ratingsManager = ratingsManager;
    }

    public List<Rating> getRatings(String documentName, int start, int count, boolean asc, XWikiContext context)
        throws RatingsException
    {
        return getRatingsManager(context).getRatings(documentName, start, count, asc, context);
    }

    public Rating setRating(String documentName, String author, int vote, XWikiContext context) throws RatingsException
    {
        return getRatingsManager(context).setRating(documentName, author, vote, context);
    }

    public Rating getRating(String documentName, String author, XWikiContext context) throws RatingsException
    {
        return getRatingsManager(context).getRating(documentName, author, context);
    }

    public AverageRating getAverageRating(String documentName, String method, XWikiContext context)
        throws RatingsException
    {
        return getRatingsManager(context).getAverageRating(documentName, method, context);
    }

    public AverageRating getAverageRating(String documentName, XWikiContext context) throws RatingsException
    {
        return getRatingsManager(context).getAverageRating(documentName, context);
    }

    public AverageRating getAverageRating(String fromsql, String wheresql, String method, XWikiContext context)
        throws RatingsException
    {
        return getRatingsManager(context).getAverageRatingFromQuery(fromsql, wheresql, method, context);
    }

    public AverageRating getAverageRatingFromQuery(String fromsql, String wheresql, XWikiContext context)
        throws RatingsException
    {
        return getRatingsManager(context).getAverageRatingFromQuery(fromsql, wheresql, context);
    }

    public AverageRating getUserReputation(String username, XWikiContext context) throws RatingsException
    {
        return getRatingsManager(context).getUserReputation(username, context);
    }

}
