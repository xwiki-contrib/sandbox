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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.ratings.Rating;
import com.xpn.xwiki.plugin.ratings.RatingsException;
import com.xpn.xwiki.plugin.ratings.AverageRating;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.comments.internal.DefaultContainer;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class DefaultRatingsManager extends AbstractRatingsManager
{

    private static Log LOG = LogFactory.getLog(DefaultRatingsManager.class);

    public DefaultRatingsManager() {
        super();
    }


    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#setRating(com.xpn.xwiki.plugin.comments.Container, String, int, com.xpn.xwiki.XWikiContext)
     */
    public Rating setRating(Container container, String author, int vote, XWikiContext context) throws RatingsException
    {
        Rating rating = getRating(container, author, context);
        int oldVote;
        if (rating==null) {
         oldVote = 0;
         rating = new DefaultRating(container, author, vote, context);
        } else {
         oldVote = rating.getVote();
         rating.setVote(vote);
         rating.setDate(new Date());
        }
        rating.save();
        updateAverageRatings(container, rating, oldVote, context);
        return rating;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#getRatings(com.xpn.xwiki.plugin.comments.Container, int, int, boolean, com.xpn.xwiki.XWikiContext)
     */
    public List<Rating> getRatings(Container container, int start, int count, boolean asc, XWikiContext context) throws RatingsException
    {
        if (LOG.isDebugEnabled())
          LOG.debug("Calling default manager code for ratings");
        try {
            int skipped = 0;
            int nb = 0;
            XWikiDocument doc =
                context.getWiki().getDocument(container.getDocumentName(), context);
            List<BaseObject> bobjects = doc.getObjects(getRatingsClassName(context));
            if (bobjects != null) {
                List<Rating> ratings = new ArrayList<Rating>();
                for (BaseObject bobj : bobjects) {
                    if (bobj != null) {
                        if (skipped<start) {
                            skipped++;
                        } else {
                            ratings.add(getDefaultRating(container, doc, bobj, context));
                            nb++;
                        }
                        if ((count!=0)&&(nb==count))
                            break;
                    }
                }
                return ratings;
            }
        } catch (XWikiException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Rating getRating(String ratingId, XWikiContext context) throws RatingsException {
        try {
            int i1 = ratingId.indexOf(":");
            if (i1==-1)
                throw new RatingsException(RatingsException.MODULE_PLUGIN_RATINGS, RatingsException.ERROR_RATINGS_INVALID_RATING_ID,  "Invalid rating ID, cannot parse rating id");

            String docName = ratingId.substring(0, i1);
            String sObjectNb = ratingId.substring(i1+1);
            int objectNb = Integer.parseInt(sObjectNb);
            Container container = new DefaultContainer(docName, -1, "", null, context);
            XWikiDocument doc = context.getWiki().getDocument(docName, context);

            if (doc.isNew())
                throw new RatingsException(RatingsException.MODULE_PLUGIN_RATINGS, RatingsException.ERROR_RATINGS_INVALID_RATING_ID,  "Invalid rating ID, document does not exist");

            BaseObject object = doc.getObject(getRatingsClassName(context), objectNb);

            if (object==null)
                throw new RatingsException(RatingsException.MODULE_PLUGIN_RATINGS, RatingsException.ERROR_RATINGS_INVALID_RATING_ID,  "Invalid rating ID, could not find rating");

            return new DefaultRating(container, doc, object, context);
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#getRating(com.xpn.xwiki.plugin.comments.Container, int, com.xpn.xwiki.XWikiContext)
     */
    public Rating getRating(Container container, int id, XWikiContext context)
        throws RatingsException
    {
        try {
            int skipped = 0;
            XWikiDocument doc =
                context.getWiki().getDocument(container.getDocumentName(), context);
            List<BaseObject> bobjects = doc.getObjects(getRatingsClassName(context));
            if (bobjects != null) {
                for (BaseObject bobj : bobjects) {
                    if (bobj != null) {
                        if (skipped<id)
                         skipped++;
                        else
                         return getDefaultRating(container, doc, bobj, context);
                    }
                }
            }
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.plugin.ratings.RatingsManager#getRating(com.xpn.xwiki.plugin.comments.Container, String, com.xpn.xwiki.XWikiContext)
     */
    public Rating getRating(Container container, String author, XWikiContext context)
            throws RatingsException
    {
        try {
            if (author==null)
             return null;
            List<Rating> ratings = getRatings(container, 0, 0, false, context);
            if (ratings==null)
             return null;
            for (Rating rating : ratings)  {
                if (rating!=null && author.equals(rating.getAuthor()))
                    return rating;
            }
        } catch (XWikiException e) {
            return null;
        }
        return null;
    }

    private DefaultRating getDefaultRating(Container container, XWikiDocument doc, BaseObject bobj, XWikiContext context)
    {
        return new DefaultRating(container, doc, bobj, context);
    }

}