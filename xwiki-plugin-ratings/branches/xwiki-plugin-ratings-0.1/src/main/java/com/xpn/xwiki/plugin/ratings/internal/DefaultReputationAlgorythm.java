package com.xpn.xwiki.plugin.ratings.internal;

import com.xpn.xwiki.plugin.ratings.*;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.XWikiContext;

import java.util.Map;

/**
 * Default very simple reputation algorithm. It won't include recalculation put only flow level reputation
 * 
 */
public class DefaultReputationAlgorythm implements ReputationAlgorythm {

    protected RatingsManager ratingsManager;

    public DefaultReputationAlgorythm() {
    }


    public void setRatingsManager(RatingsManager ratingsManager) {
         this.ratingsManager = ratingsManager;
    }


    /**
     * Gets or calculates the user reputation.
     * @param username Person to calculate the reputation for
     * @param context context of the request
     * @return AverageRating of the voter
     * @throws ReputationException
     */
    public AverageRating getUserReputation(String username, XWikiContext context) throws ReputationException {
        Container container = ratingsManager.newContainer(context);
        container.setDocumentName(username);
        try {
            return ratingsManager.getAverageRating(container, RatingsManager.RATING_REPUTATION_METHOD_AVERAGE, context);
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
     * @throws ReputationException
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
     * @throws ReputationException
     */
    public AverageRating calcNewContributorReputation(String contributor, Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException {
        notimplemented();
        return null;
    }

    /**
     * Not implemented
     * @param container
     * @param rating
     * @param oldVote
     * @param context
     * @return
     * @throws ReputationException
     */
    public Map<String, AverageRating> calcNewAuthorsReputation(Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException {
        notimplemented();
        return null;
    }

    /**
     * Not implemented
     * @param context
     * @return
     * @throws ReputationException
     */
    public Map<String, AverageRating> recalcAllReputation(XWikiContext context) throws ReputationException {
        notimplemented();
        return null;
    }

    protected void notimplemented() throws ReputationException {
        throw new ReputationException(ReputationException.MODULE_PLUGIN_RATINGS_REPUTATION, ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED, "Not implemented");
    }
}
