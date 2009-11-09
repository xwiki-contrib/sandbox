package com.xpn.xwiki.plugin.ratings.internal;

import com.xpn.xwiki.plugin.ratings.*;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import java.util.Map;
import java.util.List;

/**
 * Default very simple reputation algorithm. It won't include recalculation put only flow level reputation
 *
 */
public class SimpleReputationAlgorythm implements ReputationAlgorythm {

    protected RatingsManager ratingsManager;

    protected float totalReputation;

    protected float constantX = -2;

    protected float constantY = 50;

    public SimpleReputationAlgorythm() {
    }


    public void setRatingsManager(RatingsManager ratingsManager) {
         this.ratingsManager = ratingsManager;
    }


    /**
     * Gets or calculates the user reputation.
     * @param username Person to calculate the reputation for
     * @param context context of the request
     * @return AverageRating of the voter
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public AverageRating getUserReputation(String username, XWikiContext context) throws ReputationException {
        Container container = ratingsManager.newContainer(context);
        container.setDocumentName(username);
        try {
            AverageRating aveRating =  ratingsManager.getAverageRating(container, RatingsManager.RATING_REPUTATION_METHOD_AVERAGE, context);
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
     * @param voter
     * @param container
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public AverageRating calcNewVoterReputation(String voter, Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException {
        notimplemented();
        return null;
    }

    /**
     * Implemented. Authors will receive a simple reputation.
     * @param contributor
     * @param container
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public AverageRating calcNewContributorReputation(String contributor, Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException {
        String voter = rating.getAuthor();
        float voterRep = getUserReputation(voter, context).getAverageVote();
        float constantX = getConstantX(context);
        float constantY = getConstantY(context);
           AverageRating currentRep = getUserReputation(contributor, context);
        currentRep.setAverageVote(currentRep.getAverageVote() + (rating.getVote() + constantX) * voterRep / constantY);
        notimplemented();
        return null;
    }

    private float getTotalReputation(XWikiContext context) {
        if (totalReputation == 0) {
            // recalc it
            try {
               List result = context.getWiki().search("select sum(prop.value) from XWikiDocument as doc, BaseObject as obj, FloatProperty as prop where doc.fullName=obj.name and obj.className='XWiki.XWikiUsers' and obj.id=prop.id.id and prop.id.name='averagevote'", context);
               if ((result==null)||(result.size()==0))
                totalReputation = 0;
               else
                totalReputation = ((Float) result.get(0)).floatValue();
            } catch (XWikiException e) {
                totalReputation = 0;
            }
        }
        return (totalReputation<=1) ? 1 : totalReputation;
    }

    private float getConstantX(XWikiContext context) {
        return constantX;
    }

    private float getConstantY(XWikiContext context) {
         return constantY;
     }

    /**
     * Not implemented
     * @param container
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public Map<String, AverageRating> calcNewAuthorsReputation(Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException {
        notimplemented();
        return null;
    }

    /**
     * Not implemented
     * @param context
     * @return
     * @throws com.xpn.xwiki.plugin.ratings.ReputationException
     */
    public Map<String, AverageRating> recalcAllReputation(XWikiContext context) throws ReputationException {
        notimplemented();
        return null;
    }

    protected void notimplemented() throws ReputationException {
        throw new ReputationException(ReputationException.MODULE_PLUGIN_RATINGS_REPUTATION, ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED, "Not implemented");
    }
}