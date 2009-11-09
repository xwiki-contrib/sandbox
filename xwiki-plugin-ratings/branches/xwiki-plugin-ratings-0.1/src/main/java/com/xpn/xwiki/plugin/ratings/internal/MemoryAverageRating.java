package com.xpn.xwiki.plugin.ratings.internal;

import com.xpn.xwiki.plugin.ratings.AverageRating;
import com.xpn.xwiki.plugin.ratings.RatingsException;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.XWikiContext;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 21 sept. 2008
 * Time: 23:46:21
 * To change this template use File | Settings | File Templates.
 */
public class MemoryAverageRating implements AverageRating {

    private Container container;
    private int nbVotes;
    private float averageVote;
    private String method;

    public MemoryAverageRating(Container container, int nbVotes, float averageVote, String method) {
        this.container = container;
        this.nbVotes = nbVotes;
        this.averageVote = averageVote;
        this.method = method;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public int getNbVotes() {
        return nbVotes;
    }

    public void setNbVotes(int nbVotes) {
        this.nbVotes = nbVotes;
    }

    public float getAverageVote() {
        return averageVote;
    }

    public void setAverageVote(float averageVote) {
        this.averageVote = averageVote;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void save(XWikiContext context) throws RatingsException {       
    }
}
