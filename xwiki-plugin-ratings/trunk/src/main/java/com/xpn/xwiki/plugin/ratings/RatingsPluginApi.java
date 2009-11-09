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

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.plugin.PluginApi;
import com.xpn.xwiki.plugin.ratings.Rating;

import java.util.List;
import java.util.ArrayList;

/**
 * @version $Id: $
 */
public class RatingsPluginApi extends PluginApi<RatingsPlugin>
{
    public RatingsPluginApi(RatingsPlugin plugin, XWikiContext context)
    {
        super(plugin, context);
    }

    protected RatingsManager getRatingsManager()
    {
        return getRatingsPlugin().getRatingsManager(context);
    }

    protected RatingsPlugin getRatingsPlugin()
    {
        return ((RatingsPlugin) getProtectedPlugin());
    }

    protected static List<RatingApi> wrapRatings(List<Rating> ratings, XWikiContext context)
    {
        if (ratings == null)
            return null;

        List<RatingApi> ratingsResult = new ArrayList<RatingApi>();
        for (Rating rating : ratings) {
            ratingsResult.add(new RatingApi(rating, context));
        }
        return ratingsResult;
    }

    
    public RatingApi setRating(Document doc, String author, int vote)
    {
        // TODO protect this with programming rights
        // and add a setRating(docName), not protected but for which the author is retrieved from context.
        try {
            return new RatingApi(getRatingsPlugin().setRating(doc.getFullName(), author, vote, context), context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

    public RatingApi getRating(Document doc, String author)
    {
        try {
            Rating rating =
                getRatingsPlugin()
                    .getRating(doc.getFullName(), author, context);
            if (rating == null)
                return null;
            return new RatingApi(rating, context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

    public List<RatingApi> getRatings(Document doc, int start, int count)
    {
        return getRatings(doc, start, count, true);
    }

    public List<RatingApi> getRatings(Document doc, int start, int count, boolean asc)
    {
        try {
            return wrapRatings(getRatingsPlugin().getRatings(doc.getFullName(), start, count, asc, context), context);
        } catch (Exception e) {
            context.put("exception", e);
            return null;
        }

    }

    public AverageRatingApi getAverageRating(Document doc, String method)
    {
        try {
            return new AverageRatingApi(getRatingsPlugin().getAverageRating(doc.getFullName(), method, context),
                context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

    public AverageRatingApi getAverageRating(Document doc)
    {
        try {
            return new AverageRatingApi(getRatingsPlugin().getAverageRating(doc.getFullName(), context), context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

    public AverageRatingApi getAverageRating(String fromsql, String wheresql, String method)
    {
        try {
            return new AverageRatingApi(getRatingsPlugin().getAverageRating(fromsql, wheresql, method, context),
                context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

    public AverageRatingApi getAverageRating(String fromsql, String wheresql)
    {
        try {
            return new AverageRatingApi(getRatingsPlugin().getAverageRatingFromQuery(fromsql, wheresql, context), context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

    public AverageRatingApi getUserReputation(String username)
    {
        try {
            return new AverageRatingApi(getRatingsPlugin().getUserReputation(username, context), context);
        } catch (Throwable e) {
            context.put("exception", e);
            return null;
        }
    }

}
