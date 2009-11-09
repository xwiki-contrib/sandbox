package com.xpn.xwiki.plugin.ratings;

import com.xpn.xwiki.XWikiContext;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 21 sept. 2008
 * Time: 23:36:03
 * To change this template use File | Settings | File Templates.
 */
public interface AverageRating {
    public int getNbVotes();
    public void setNbVotes(int nbVotes);
    public float getAverageVote();
    public void setAverageVote(float averageVote);
    public String getMethod();
    public void setMethod(String method);
    public void save(XWikiContext context) throws RatingsException;
}
