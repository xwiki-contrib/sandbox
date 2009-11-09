package com.xpn.xwiki.plugin.ratings;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.comments.Container;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 22 sept. 2008
 * Time: 16:49:42
 * To change this template use File | Settings | File Templates.
 */
public interface ReputationAlgorythm {

    /**
     * Set the ratings manager. This should be implemented
     * @param ratingsManager
     */
    public void setRatingsManager(RatingsManager ratingsManager);

    /**
      * Gets or calculates the user reputation.
      * @param username Person to calculate the reputation for
      * @param context context of the request
      * @return AverageRating of the voter
      * @throws ReputationException
      */
     AverageRating getUserReputation(String username, XWikiContext context) throws ReputationException;

    /**
     * Recalculates the contributor reputation. Only the creator of the document will have it's reputation updated
     * @param voter Voter that will have it's reputation updated
     * @param container Elements that was rated
     * @param rating New rating of the element, including who did the rating
     * @param oldVote previous vote of the user
     * @param context context of the request
     * @return AverageRating of the voter
     * @throws ReputationException
     */
    AverageRating calcNewVoterReputation(String voter, Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException;

    /**
     * Recalculates the contributors reputation
     * @param container Elements that was rated
     * @param rating New rating of the element, including who did the rating
     * @param oldVote previous vote of the user
     * @param context context of the request
     * @return Map of AverageRating of each contributor of the page that has an updated reputation
     * @throws ReputationException
     */
    Map<String, AverageRating> calcNewAuthorsReputation(Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException;

    /**
     * Recalculates the contributor reputation. Only the creator of the document will have it's reputation updated
     * @param contributor Contributor that will have it's reputation updated
     * @param container Elements that was rated
     * @param rating New rating of the element, including who did the rating
     * @param oldVote previous vote of the user
     * @param context context of the request
     * @return AverageRating of the contributor
     * @throws ReputationException
     */
    AverageRating calcNewContributorReputation(String contributor, Container container, Rating rating, int oldVote, XWikiContext context) throws ReputationException;

    /**
     * Recalculated all reputation of the wiki
     * The result is given as a set of AverageRating objects
     * That can be saved to the user page
     * @param context context of the request
     * @return Map of AverageRating of each user of the wiki
     * @throws ReputationException
     */
    Map<String, AverageRating> recalcAllReputation(XWikiContext context) throws ReputationException; 

}
