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
package com.xpn.xwiki.plugin.ratings.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.ratings.Rating;
import com.xpn.xwiki.plugin.ratings.RatingsException;
import com.xpn.xwiki.plugin.ratings.RatingsManager;

/**
 * @version $Id$
 * @see RatingsManager
 * @see AbstractRatingsManager
 */
public class SeparatePageRatingsManager extends AbstractRatingsManager
{
    private static Log LOG = LogFactory.getLog(SeparatePageRatingsManager.class);

    public SeparatePageRatingsManager()
    {
        super();
    }

    public String getRatingsSpaceName(XWikiContext context)
    {
        String ratingsSpaceName = context.getWiki().Param("xwiki.ratings.separatepagemanager.spacename", "");
        ratingsSpaceName =
            context.getWiki().getXWikiPreference("ratings_separatepagemanager_spacename", ratingsSpaceName, context);
        return ratingsSpaceName;
    }

    public boolean hasRatingsSpaceForeachSpace(XWikiContext context)
    {
        int result = (int) context.getWiki().ParamAsLong("xwiki.ratings.separatepagemanager.hasratingsforeachspace", 0);
        return (context.getWiki().getXWikiPreferenceAsInt("ratings_separatepagemanager_hasratingsforeachspace", result,
            context) == 1);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#setRating(com.xpn.xwiki.plugin.comments.Container, String, int,
     *      com.xpn.xwiki.XWikiContext)
     */
    public Rating setRating(String documentName, String author, int vote, XWikiContext context) throws RatingsException
    {
        Rating rating = getRating(documentName, author, context);
        int oldVote;
        if (rating == null) {
            oldVote = 0;
            rating = new SeparatePageRating(documentName, author, vote, context);
        } else {
            oldVote = rating.getVote();
            rating.setVote(vote);
            rating.setDate(new Date());
        }
        rating.save();
        // update the average rating
        updateAverageRatings(documentName, rating, oldVote, context);

        // update reputation
        updateReputation(documentName, rating, oldVote, context);
        return rating;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#getRatings(com.xpn.xwiki.plugin.comments.Container, int, int,
     *      boolean, com.xpn.xwiki.XWikiContext)
     */
    public List<Rating> getRatings(String documentName, int start, int count, boolean asc, XWikiContext context)
        throws RatingsException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Calling separate page manager code for ratings");
        }

        String sql =
            ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='"
                + getRatingsClassName(context)
                + "' and obj.id=parentprop.id.id and parentprop.id.name='"
                + RATING_CLASS_FIELDNAME_PARENT
                + "' and parentprop.value='"
                + documentName
                +
                "' and obj.name not in (select obj2.name from BaseObject as obj2, StringProperty as statusprop where obj2.className='"
                + getRatingsClassName(context)
                +
                "' and obj2.id=statusprop.id.id and statusprop.id.name='status' and (statusprop.value='moderated' or statusprop.value='refused') and obj.id=obj2.id) order by doc.date "
                + (asc ? "asc" : "desc");
        List<Rating> ratings = new ArrayList<Rating>();
        try {
            List<String> ratingPageNameList =
                context.getWiki().getStore().searchDocumentsNames(sql, count, start, context);

            for (String ratingPageName : ratingPageNameList) {
                ratings.add(new SeparatePageRatingsManager().getRatingFromDocument(documentName, context.getWiki()
                    .getDocument(ratingPageName, context), context));
            }
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }

        return ratings;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#getRatings(com.xpn.xwiki.plugin.comments.Container, int, int,
     *      boolean, com.xpn.xwiki.XWikiContext)
     */
    public Rating getRating(String documentName, int id, XWikiContext context) throws RatingsException
    {
        String sql =
            ", BaseObject as obj, StringProperty as parentprop where doc.fullName=obj.name and obj.className='"
                + getRatingsClassName(context)
                + "' and obj.id=parentprop.id.id and parentprop.id.name='"
                + RATING_CLASS_FIELDNAME_PARENT
                + "' and parentprop.value='"
                + documentName
                +
                "' and obj.name not in (select obj2.name from BaseObject as obj2, StringProperty as statusprop where obj2.className='"
                + getRatingsClassName(context)
                +
                "' and obj2.id=statusprop.id.id and statusprop.id.name='status' and (statusprop.value='moderated' or statusprop.value='refused') and obj.id=obj2.id) order by doc.date desc";
        try {
            List<String> ratingPageNameList = context.getWiki().getStore().searchDocumentsNames(sql, 1, id, context);
            if ((ratingPageNameList == null) || (ratingPageNameList.size() == 0)) {
                return null;
            } else {
                return new SeparatePageRatingsManager().getRatingFromDocument(documentName, context.getWiki()
                    .getDocument(ratingPageNameList.get(0), context), context);
            }
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#getRating(com.xpn.xwiki.plugin.comments.Container, String,
     *      com.xpn.xwiki.XWikiContext)
     */
    public Rating getRating(String documentName, String author, XWikiContext context) throws RatingsException
    {
        try {
            for (Rating rating : getRatings(documentName, 0, 0, false, context)) {
                if (author.equals(rating.getAuthor())) {
                    return rating;
                }
            }
        } catch (XWikiException e) {
            return null;
        }
        return null;
    }

    public Rating getRating(String ratingId, XWikiContext context) throws RatingsException
    {
        try {
            int i1 = ratingId.indexOf(".");
            if (i1 == -1) {
                throw new RatingsException(RatingsException.MODULE_PLUGIN_RATINGS,
                    RatingsException.ERROR_RATINGS_INVALID_RATING_ID, "Invalid rating ID, cannot parse rating id");
            }

            XWikiDocument doc = context.getWiki().getDocument(ratingId, context);
            if (doc.isNew()) {
                throw new RatingsException(RatingsException.MODULE_PLUGIN_RATINGS,
                    RatingsException.ERROR_RATINGS_INVALID_RATING_ID, "Invalid rating ID, rating does not exist");
            }

            BaseObject object = doc.getObject(getRatingsClassName(context));
            if (object == null) {
                throw new RatingsException(RatingsException.MODULE_PLUGIN_RATINGS,
                    RatingsException.ERROR_RATINGS_INVALID_RATING_ID, "Invalid rating ID, rating does not exist");
            }

            String parentDocName = object.getStringValue(RATING_CLASS_FIELDNAME_PARENT);

            return new SeparatePageRating(parentDocName, doc, context);
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }

    public Rating getRatingFromDocument(String documentName, XWikiDocument doc, XWikiContext context)
        throws RatingsException
    {
        return new SeparatePageRating(documentName, doc, context);
    }
}
