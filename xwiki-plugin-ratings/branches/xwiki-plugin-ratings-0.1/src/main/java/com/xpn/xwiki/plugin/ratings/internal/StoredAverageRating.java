package com.xpn.xwiki.plugin.ratings.internal;

import com.xpn.xwiki.plugin.ratings.AverageRating;
import com.xpn.xwiki.plugin.ratings.RatingsException;
import com.xpn.xwiki.plugin.ratings.RatingsManager;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Created by IntelliJ IDEA.
 * User: ludovic
 * Date: 21 sept. 2008
 * Time: 20:59:11
 * To change this template use File | Settings | File Templates.
 */
public class StoredAverageRating implements AverageRating {

    private Container container;

    private XWikiDocument document;

    private BaseObject object;

    public StoredAverageRating(Container container, XWikiDocument document, BaseObject ratingObject) {
        this.container = container;
        this.document = document;
        this.object = ratingObject;
    }

    public int getNbVotes() {
        return object.getIntValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_NBVOTES);
    }

    public void setNbVotes(int nbVotes) {
        object.setIntValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_NBVOTES, nbVotes);
    }

    public float getAverageVote() {
        return object.getFloatValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE);
    }

    public void setAverageVote(float averageVote) {
        object.setFloatValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE, averageVote);
    }

    public String getMethod() {
        return object.getStringValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD);
    }

    public void setMethod(String method) {
        object.setStringValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD, method);
    }

    public void save(XWikiContext context) throws RatingsException {
        try {
            // Force content dirty to false, so that the content update date is not changed when saving the document.
            // This should not be handled there, since it is not the responsibility of this plugin to decide if
            // the content has actually been changed or not since current revision, but the implementation of
            // this in XWiki core is wrong. See http://jira.xwiki.org/jira/XWIKI-2800 for more details.
            // There is a draw-back to doing this, being that if the document content is being changed before 
            // the document is rated, the contentUpdateDate will not be modified. Although possible, this is very
            // unlikely to happen, or to be a use case. The default rating application will use an asynchronous service to
            // note a document, which service will only set the rating, so the behavior will be correct.
            document.setContentDirty(false);
            context.getWiki().saveDocument(document, context);
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }
}
