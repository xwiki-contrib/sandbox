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

import java.util.List;
import java.util.Map;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.plugin.ratings.AverageRating;
import com.xpn.xwiki.plugin.ratings.Rating;
import com.xpn.xwiki.plugin.ratings.RatingsException;
import com.xpn.xwiki.plugin.ratings.RatingsManager;
import com.xpn.xwiki.plugin.ratings.ReputationAlgorythm;
import com.xpn.xwiki.plugin.ratings.ReputationException;

/**
 * Default very simple reputation algorithm. It won't include recalculation put only flow level reputation
 * 
 * @version $Id: $
 * @see ReputationAlgorythm
 */
public class SimpleReputationAlgorythm implements ReputationAlgorythm
{

    protected RatingsManager ratingsManager;

    protected float totalReputation;

    protected float constantX = -2;

    protected float constantY = 50;

    public SimpleReputationAlgorythm()
    {
    }

    public void setRatingsManager(RatingsManager ratingsManager)
    {
        this.ratingsManager = ratingsManager;
    }

    /**
     * Gets or calculates the user reputation.
     * 
     * @param username Person to calculate the reputation for
     * @param context context of the request
     * @return AverageRating of the voter
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public AverageRating getUserReputation(String username, XWikiContext context) throws ReputationException
    {
        try {
            AverageRating aveRating =
                ratingsManager.getAverageRating(username, RatingsManager.RATING_REPUTATION_METHOD_AVERAGE, context);
            float oldRep = aveRating.getAverageVote();
            aveRating.setAverageVote(aveRating.getAverageVote() * 100 / getTotalReputation(context));
            totalReputation += aveRating.getAverageVote() - oldRep;
            return aveRating;
        } catch (RatingsException e) {
            throw new ReputationException(e);
        }
    }

    /**
     * Not implemented. Voters don't receive reputation
     * 
     * @param voter
     * @param container
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public AverageRating calcNewVoterReputation(String voter, String documentName, Rating rating, int oldVote,
        XWikiContext context) throws ReputationException
    {
        notimplemented();
        return null;
    }

    /**
     * Implemented. Authors will receive a simple reputation.
     * 
     * @param contributor
     * @param container
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public AverageRating calcNewContributorReputation(String contributor, String documentName, Rating rating,
        int oldVote, XWikiContext context) throws ReputationException
    {
        String voter = rating.getAuthor();
        float voterRep = getUserReputation(voter, context).getAverageVote();
        float constantX = getConstantX(context);
        float constantY = getConstantY(context);
        AverageRating currentRep = getUserReputation(contributor, context);
        currentRep.setAverageVote(currentRep.getAverageVote() + (rating.getVote() + constantX) * voterRep / constantY);
        notimplemented();
        return null;
    }

    private float getTotalReputation(XWikiContext context)
    {
        if (totalReputation == 0) {
            // recalc it
            try {
                List result =
                    context
                        .getWiki()
                        .search(
                            "select sum(prop.value) from XWikiDocument as doc, BaseObject as obj, FloatProperty as prop where doc.fullName=obj.name and obj.className='XWiki.XWikiUsers' and obj.id=prop.id.id and prop.id.name='averagevote'",
                            context);
                if ((result == null) || (result.size() == 0))
                    totalReputation = 0;
                else
                    totalReputation = ((Float) result.get(0)).floatValue();
            } catch (XWikiException e) {
                totalReputation = 0;
            }
        }
        return (totalReputation <= 1) ? 1 : totalReputation;
    }

    private float getConstantX(XWikiContext context)
    {
        return constantX;
    }

    private float getConstantY(XWikiContext context)
    {
        return constantY;
    }

    /**
     * Not implemented
     * 
     * @param documentName
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public Map<String, AverageRating> calcNewAuthorsReputation(String documentName, Rating rating, int oldVote,
        XWikiContext context) throws ReputationException
    {
        notimplemented();
        return null;
    }

    /**
     * Not implemented
     * 
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public Map<String, AverageRating> recalcAllReputation(XWikiContext context) throws ReputationException
    {
        notimplemented();
        return null;
    }

    protected void notimplemented() throws ReputationException
    {
        throw new ReputationException(ReputationException.MODULE_PLUGIN_RATINGS_REPUTATION,
            ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED, "Not implemented");
    }
}
