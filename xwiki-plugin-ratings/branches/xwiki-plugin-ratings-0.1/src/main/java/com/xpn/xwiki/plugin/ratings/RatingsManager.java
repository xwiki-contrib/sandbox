package com.xpn.xwiki.plugin.ratings;

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

import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.ratings.internal.StoredAverageRating;
import com.xpn.xwiki.XWikiContext;

import java.util.List;

public interface RatingsManager
{
    String RATING_CLASS_FIELDNAME_DATE = "date";
    String RATING_CLASS_FIELDNAME_AUTHOR = "author";
    String RATING_CLASS_FIELDNAME_VOTE = "vote";
    String RATING_CLASS_FIELDNAME_PARENT = "parent";
    String AVERAGERATING_CLASS_FIELDNAME_NBVOTES = "nbvotes";
    String AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE = "averagevote";
    String AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD = "method";
    String RATING_REPUTATION_METHOD_BALANCED = "balanced";
    String RATING_REPUTATION_METHOD_AVERAGE = "average";
    String RATING_REPUTATION_METHOD_DEFAULT = "average";


    void init(XWikiContext context);

    void virtualInit(XWikiContext context);

    String getRatingsClassName(XWikiContext context);

    Container newContainer(XWikiContext context);

    List<Rating> getRatings(Container container, int start, int count, boolean asc, XWikiContext context) throws RatingsException;

    Rating getRating(String ratingId, XWikiContext context) throws RatingsException;

    Rating getRating(Container container, int id, XWikiContext context) throws RatingsException;

    Rating getRating(Container container, String user, XWikiContext context) throws RatingsException;

    Rating setRating(Container container, String author, int vote, XWikiContext context) throws RatingsException;

    boolean removeRating(Rating rating, XWikiContext context) throws RatingsException;

    // average rating and reputation

    boolean isAverageRatingStored(XWikiContext context);

    boolean isReputationStored(XWikiContext context);

    boolean hasRatings(XWikiContext context);

    boolean hasReputation(XWikiContext context);

    String[] getDefaultReputationMethods(XWikiContext context);
    
    AverageRating getAverageRating(Container container, XWikiContext context) throws RatingsException;

    AverageRating getAverageRating(Container container, String method, XWikiContext context) throws RatingsException;

    AverageRating getAverageRating(String fromsql, String wheresql, XWikiContext context) throws RatingsException;

    AverageRating getAverageRating(String fromsql, String wheresql, String method, XWikiContext context) throws RatingsException;

    AverageRating getAverageRating(Container container, String method, boolean create, XWikiContext context) throws RatingsException;
    
    AverageRating calcAverageRating(Container container, String method, XWikiContext context) throws RatingsException;

    void updateAverageRating(Container container, Rating rating, int oldVote, String method, XWikiContext context) throws RatingsException;

    void updateReputation(Container container, Rating rating, int oldVote, XWikiContext context) throws RatingsException;

    void updateAverageRatings(Container container, Rating rating, int oldVote, XWikiContext context) throws RatingsException;

    AverageRating getUserReputation(String username, XWikiContext context) throws RatingsException;

    ReputationAlgorythm getReputationAlgorythm(XWikiContext context) throws RatingsException;
}
