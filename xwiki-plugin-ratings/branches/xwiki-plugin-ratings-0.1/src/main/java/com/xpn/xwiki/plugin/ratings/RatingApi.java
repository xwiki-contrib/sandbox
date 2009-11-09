package com.xpn.xwiki.plugin.ratings;

import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.plugin.comments.Comment;
import com.xpn.xwiki.XWikiContext;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 21 sept. 2008
 * Time: 14:45:17
 * To change this template use File | Settings | File Templates.
 */
public class RatingApi extends Api {

    protected Rating rating;

    public RatingApi(Rating rating, XWikiContext context) {
        super(context);
        this.rating = rating;
    }

    protected Rating getRating() {
        return rating;
    }

    public String getGlobalRatingId() {
        return rating.getGlobalRatingId();
    }

    public int getVote() {
        if (rating==null)
         return 0;
        else
         return rating.getVote();
    }

    public String getAuthor() {
        return rating.getAuthor();
    }

    public Date getDate() {
        return rating.getDate();
    }
}
