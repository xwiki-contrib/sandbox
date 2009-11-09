package com.xpn.xwiki.plugin.ratings;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 24 sept. 2008
 * Time: 16:55:21
 * To change this template use File | Settings | File Templates.
 */
public class AverageRatingApi extends Api {

    protected AverageRating averageRating;

    public AverageRatingApi(AverageRating arating, XWikiContext context) {
        super(context);
        this.averageRating = arating;
    }


    public int getNbVotes() {
        if (averageRating==null)
            return 0;
        else
            return averageRating.getNbVotes();
    }

    public float getAverageVote() {
        if (averageRating==null)
         return 0;
        else
         return averageRating.getAverageVote();
    }

    public String getMethod() {
        if (averageRating==null)
         return "";
        else
         return averageRating.getMethod();
    }

}
