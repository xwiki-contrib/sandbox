package com.xpn.xwiki.plugin.ratings.internal;

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

import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.comments.internal.DefaultContainer;

import com.xpn.xwiki.plugin.ratings.*;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.BaseObject;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Vector;
import java.util.List;

public abstract class AbstractRatingsManager implements RatingsManager
{
    private static Log LOG = LogFactory.getLog(AbstractRatingsManager.class);

    private static String ratingsClassName = "XWiki.RatingsClass";
    private static String averageRatingsClassName = "XWiki.AverageRatingsClass";

    private ReputationAlgorythm reputationAlgorythm;
    private String reputationAlgorythmVersion;

    public String getRatingsClassName(XWikiContext context) {
        return ratingsClassName;
    }

    public String getAverageRatingsClassName(XWikiContext context) {
        return averageRatingsClassName;
    }

     public void init(XWikiContext context)
    {
        try {
            initRatingsClass(context);
            initAverageRatingsClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void virtualInit(XWikiContext context)
    {
        try {
            initRatingsClass(context);
            initAverageRatingsClass(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public BaseClass initAverageRatingsClass(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;
        String averageRatingsClassName = getAverageRatingsClassName(context);

        doc = xwiki.getDocument(averageRatingsClassName, context);
        BaseClass bclass = doc.getxWikiClass();
        bclass.setName(averageRatingsClassName);
        if (context.get("initdone") != null) {
            return bclass;
        }

        needsUpdate |= bclass.addNumberField(AVERAGERATING_CLASS_FIELDNAME_NBVOTES, "Number of Votes", 5, "integer");
        needsUpdate |= bclass.addNumberField(AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE, "Average Vote", 5, "float");
        needsUpdate |= bclass.addTextField(AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD, "Average Vote method", 10);

        if (StringUtils.isBlank(doc.getAuthor())) {
            needsUpdate = true;
            doc.setAuthor("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getCreator())) {
            needsUpdate = true;
            doc.setCreator("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getParent())) {
            needsUpdate = true;
            doc.setParent("XWiki.XWikiClasses");
        }

        String content = doc.getContent();
        if ((content == null) || (content.equals(""))) {
            needsUpdate = true;
            doc.setContent("1 XWiki Average Ratings Class");
        }

        if (needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
        return bclass;
    }
    public BaseClass initRatingsClass(XWikiContext context) throws XWikiException
    {
        XWikiDocument doc;
        XWiki xwiki = context.getWiki();
        boolean needsUpdate = false;
        String ratingsClassName = getRatingsClassName(context);

        doc = xwiki.getDocument(ratingsClassName, context);
        BaseClass bclass = doc.getxWikiClass();
        bclass.setName(ratingsClassName);
        if (context.get("initdone") != null) {
            return bclass;
        }

        needsUpdate |= bclass.addTextField(RATING_CLASS_FIELDNAME_AUTHOR, "Author", 30);
        needsUpdate |= bclass.addNumberField(RATING_CLASS_FIELDNAME_VOTE, "Vote", 5, "integer");
        needsUpdate |= bclass.addDateField(RATING_CLASS_FIELDNAME_DATE, "Date");
        needsUpdate |= bclass.addTextField(RATING_CLASS_FIELDNAME_PARENT, "Parent", 30);
    
        if (StringUtils.isBlank(doc.getAuthor())) {
            needsUpdate = true;
            doc.setAuthor("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getCreator())) {
            needsUpdate = true;
            doc.setCreator("XWiki.Admin");
        }
        if (StringUtils.isBlank(doc.getParent())) {
            needsUpdate = true;
            doc.setParent("XWiki.XWikiClasses");
        }

        String content = doc.getContent();
        if ((content == null) || (content.equals(""))) {
            needsUpdate = true;
            doc.setContent("1 XWiki Ratings Class");
        }

        if (needsUpdate) {
            xwiki.saveDocument(doc, context);
        }
        return bclass;
    }


    public Container newContainer(XWikiContext context) {
        return new DefaultContainer(context);
    }

    public boolean hasRatings(XWikiContext context) {
        int result = (int) context.getWiki().ParamAsLong("xwiki.ratings", 0);
        return (context.getWiki().getXWikiPreferenceAsInt("ratings", result, context)==1);
    }

    public boolean isAverageRatingStored(XWikiContext context) {
        int result = (int) context.getWiki().ParamAsLong("xwiki.ratings.averagerating.stored", 0);
        return (context.getWiki().getXWikiPreferenceAsInt("ratings_averagerating_stored", result, context)==1);
    }

    public boolean isReputationStored(XWikiContext context) {
        int result = (int) context.getWiki().ParamAsLong("xwiki.ratings.reputation.stored", 0);
        return (context.getWiki().getXWikiPreferenceAsInt("ratings_reputation_stored", result, context)==1);
    }

    public boolean hasReputation(XWikiContext context) {
        int result = (int) context.getWiki().ParamAsLong("xwiki.ratings.reputation", 0);
        return (context.getWiki().getXWikiPreferenceAsInt("ratings_reputation", result, context)==1);
    }

    public String[] getDefaultReputationMethods(XWikiContext context) {
        String method = context.getWiki().Param("xwiki.ratings.reputation.defaultmethod", RATING_REPUTATION_METHOD_DEFAULT);
        method = context.getWiki().getXWikiPreference("ratings_reputation_defaultmethod", method, context);
        return method.split(",");
    }

    public void updateAverageRatings(Container container, Rating rating, int oldVote, XWikiContext context) throws RatingsException {
        String[] methods = getDefaultReputationMethods(context);
            for (int i=0;i<methods.length;i++)
             updateAverageRating(container, rating, oldVote, methods[i], context);
    }

    public AverageRating getAverageRating(String fromsql, String wheresql, XWikiContext context) throws RatingsException {
        return getAverageRating(fromsql, wheresql, RATING_REPUTATION_METHOD_AVERAGE, context);
    }


    public AverageRating getAverageRating(Container container, XWikiContext context) throws RatingsException {
        return getAverageRating(container, RATING_REPUTATION_METHOD_AVERAGE, context);
    }

    public AverageRating getAverageRating(String fromsql, String wheresql, String method, XWikiContext context) throws RatingsException {
        try {
            String fromsql2 = fromsql + ", BaseObject as avgobj, FloatProperty as avgvote, StringProperty as avgmethod ";
            String wheresql2 = (wheresql.equals("") ? "where " : wheresql + " and ") + "doc.fullName=avgobj.name and avgobj.className='" + getAverageRatingsClassName(context)
                    + "' and avgobj.id=avgvote.id.id and avgvote.id.name='" + AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE
                    + "' and avgobj.id=avgmethod.id.id and avgmethod.id.name='" + AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD + "' and avgmethod.value='" + method + "'";
            String sql = "select sum(avgvote.value) as vote, count(avgvote.value) as nbvotes from XWikiDocument as doc " + fromsql2 + wheresql2;

            if (LOG.isDebugEnabled())
              LOG.debug("Running average rating with sql " + sql);
            context.put("lastsql", sql);

            List result = context.getWiki().getStore().search(sql, 0 , 0, context);
            float vote =  ((Number)((Object[])result.get(0))[0]).floatValue();
            int nbvotes =  ((Number)((Object[])result.get(0))[1]).intValue();

            AverageRating avgr = new MemoryAverageRating(null, nbvotes, vote / (float) nbvotes, method);
            return avgr;
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }

    public boolean removeRating(Rating rating, XWikiContext context) throws RatingsException {
        return rating.remove();
    }

    /**
     * Get the reputation algorythm class. Make sure the version is checked when it is a groovy script
     * @param context
     * @return
     * @throws RatingsException
     */
    public ReputationAlgorythm getReputationAlgorythm(XWikiContext context) throws RatingsException {
        String groovyPage = getReputationAlgorythmGroovyPage(context);
        if (reputationAlgorythm!=null) {
            if ((reputationAlgorythmVersion==null)||(groovyPage==null))
                return reputationAlgorythm;
            else {
                XWikiDocument groovyDoc = null;
                try {
                    groovyDoc = context.getWiki().getDocument(groovyPage, context);
                    String groovyVersion = groovyDoc.getVersion();
                    // version is the same let's use the already loaded one
                    if (reputationAlgorythmVersion.equals(groovyVersion))
                        return reputationAlgorythm;


                } catch (XWikiException e) {
                    if (LOG.isErrorEnabled())
                        LOG.error("Could not load reputation algorythm for groovy " + groovyPage, e);
                    return reputationAlgorythm;
                }
            }
        }

        if (groovyPage!=null) {
            try {
                reputationAlgorythm = (ReputationAlgorythm) context.getWiki().parseGroovyFromPage(groovyPage, context);
                try {
                    // we should update the version that was used
                    XWikiDocument groovyDoc = context.getWiki().getDocument(groovyPage, context);
                    reputationAlgorythmVersion = groovyDoc.getVersion();
                } catch (Exception e) {}
            } catch (XWikiException e) {
                if (LOG.isErrorEnabled())
                    LOG.error("Could not init reputation algorythm for groovy page " + groovyPage, e);
                reputationAlgorythm = new DefaultReputationAlgorythm();
            }
        } else {
            String className = context.getWiki().Param("xwiki.ratings.reputation.classname", "com.xpn.xwiki.plugin.ratings.internal.DefaultReputationAlgorythm");

            try {
                reputationAlgorythm = (ReputationAlgorythm) Class.forName(className).newInstance();
            } catch (Exception e) {
                if (LOG.isErrorEnabled())
                    LOG.error("Could not init reputation algorythm for class " + className, e);
                reputationAlgorythm = new DefaultReputationAlgorythm();
            }
        }
        reputationAlgorythm.setRatingsManager(this);
        return reputationAlgorythm;
    }

    private String getReputationAlgorythmGroovyPage(XWikiContext context) {
        String groovyPage = context.getWiki().Param("xwiki.ratings.reputation.groovypage", "");
        groovyPage = context.getWiki().getXWikiPreference("ratings_reputation_groovypage", groovyPage, context);
        return groovyPage;
    }

    public AverageRating calcAverageRating(Container container, String method, XWikiContext context) throws RatingsException {
        int nbVotes = 0;
            int balancedNbVotes = 0;
        float totalVote = 0;
        float averageVote = 0;
        List<Rating> ratings = getRatings(container, 0, 0, true, context);
        if (ratings==null)
            return null;
        for (Rating rating : ratings) {
            if (method.equals(RATING_REPUTATION_METHOD_BALANCED)) {
                String author = rating.getAuthor();
                // in case we are evaluating the average rating of a user
                // we should not include votes of himself to a user
                if (!author.equals(container.getDocumentName())) {
                    AverageRating reputation = getUserReputation(author, context);
                    if ((reputation==null)||(reputation.getAverageVote()==0)) {
                        totalVote += rating.getVote();
                        balancedNbVotes++;
                    } else {
                        totalVote += rating.getVote() * reputation.getAverageVote();
                        balancedNbVotes += reputation.getAverageVote();
                    }
                }
            } else {
                totalVote += rating.getVote();
                balancedNbVotes++;
            }
            nbVotes++;
        }

        if (balancedNbVotes != 0)
            averageVote = totalVote / balancedNbVotes;
            return new MemoryAverageRating(container, nbVotes, averageVote, method);
    }

    public void updateAverageRating(Container container, Rating rating, int oldVote, String method, XWikiContext context) throws RatingsException {
        // we only update if we are in stored mode and if the vote changed
        if (isAverageRatingStored(context)&&oldVote!=rating.getVote()) {
            AverageRating aRating = calcAverageRating(container, method, context);
            AverageRating averageRating = getAverageRating(container, method, true, context);
            averageRating.setAverageVote(aRating.getAverageVote());
            averageRating.setNbVotes(aRating.getNbVotes());
            averageRating.save(context);
            /*
                StoredAverageRating averageRating = (StoredAverageRating) getAverageRating(container, method, true, context);
                int diffTotal = rating.getVote() - oldVote;
                int diffNbVotes = (oldVote==0) ? 1 : 0;
                int oldNbVotes = averageRating.getNbVotes();
                averageRating.setNbVotes(oldNbVotes + diffNbVotes);
                averageRating.setAverageVote((averageRating.getAverageVote()*oldNbVotes + diffTotal) / (oldNbVotes + diffNbVotes));
                */
        }
    }

    public void updateReputation(Container container, Rating rating, int oldVote, XWikiContext context) throws RatingsException {
        // we only update if we are in stored mode and if the vote changed
        if (hasReputation(context) && isReputationStored(context) && oldVote!=rating.getVote()) {
            ReputationAlgorythm ralgo = getReputationAlgorythm(context);
            // voter reputation. This will give points to the voter
            try {
                AverageRating voterRating = ralgo.calcNewVoterReputation(rating.getAuthor(), container, rating, oldVote, context);
                // we need to save this reputation if it has changed
                updateUserReputation(rating.getAuthor(), voterRating, context);
            } catch (ReputationException e) {
                if (e.getCode()!= ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED) {
                    // we should log this error
                    if (LOG.isErrorEnabled())
                        LOG.error("Error while calculating voter reputation " + rating.getAuthor() + " for document " + container.getDocumentName(), e);
                }
            }

            // author reputation. This will be giving points to the creator of a document or comment
            try {
                XWikiDocument doc = context.getWiki().getDocument(container.getDocumentName(), context);
                AverageRating authorRating = ralgo.calcNewContributorReputation(doc.getCreator(), container, rating, oldVote, context);
                // we need to save the author reputation
                updateUserReputation(doc.getCreator(), authorRating, context);
            } catch (ReputationException e) {
                if (e.getCode()!= ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED) {
                    // we should log this error
                    if (LOG.isErrorEnabled())
                        LOG.error("Error while calculating author reputation for document " + container.getDocumentName(), e);
                }
            } catch (XWikiException e) {
                if (LOG.isErrorEnabled())
                     LOG.error("Error while calculating author reputation for document " + container.getDocumentName(), e);
            }

            // all authors reputation. This will be used to give points to all participants to a document
            try {
                Map<String, AverageRating> authorsRatings = ralgo.calcNewAuthorsReputation(container, rating, oldVote, context);
                // TODO this is not implemented yet        
            } catch (ReputationException e) {
                if (e.getCode()!= ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED) {
                    // we should log this error
                    if (LOG.isErrorEnabled())
                        LOG.error("Error while calculating authors reputation for document " + container.getDocumentName(), e);
                }
            } catch (XWikiException e) {
                if (LOG.isErrorEnabled())
                    LOG.error("Error while calculating authors for document " + container.getDocumentName(), e);
            }
        }
    }

    private void updateUserReputation(String author, AverageRating voterRating, XWikiContext context) throws RatingsException {
        try {
            // We should update the user rating
            Container container = newContainer(context);
            container.setDocumentName(author);
            AverageRating rating = getAverageRating(container, voterRating.getMethod(), true, context);
            rating.setAverageVote(voterRating.getAverageVote());
            rating.setMethod(voterRating.getMethod());
            rating.setNbVotes(voterRating.getNbVotes());
            rating.save(context);
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }

    public AverageRating getUserReputation(String username, XWikiContext context) throws RatingsException {
        try {
            return getReputationAlgorythm(context).getUserReputation(username, context);
        } catch (ReputationException e) {
            if (e.getCode()== ReputationException.ERROR_REPUTATION_NOT_IMPLEMENTED) {
                Container container = newContainer(context);
                container.setDocumentName(username);
                return getAverageRating(container, context);
            } else
                throw e;
        }
    }

    public AverageRating getAverageRating(Container container, String method, XWikiContext context) throws RatingsException {
        return getAverageRating(container, method, false, context);
    }

    public AverageRating getAverageRating(Container container, String method, boolean create, XWikiContext context) throws RatingsException {
        try {
            if (isAverageRatingStored(context)) {
                String className = getAverageRatingsClassName(context);
                XWikiDocument doc = context.getWiki().getDocument(container.getDocumentName(), context);
                BaseObject averageRatingObject = doc.getObject(className, RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD, method, false);
                if (averageRatingObject==null) {
                    if (!create) {
                        return calcAverageRating(container, method, context);
                    }

                    // initiate a new average rating object
                    averageRatingObject = doc.newObject(className, context);
                    averageRatingObject.setStringValue(RatingsManager.AVERAGERATING_CLASS_FIELDNAME_AVERAGEVOTE_METHOD, method);
                }

                return new StoredAverageRating(container, doc, averageRatingObject);
            } else {
                return calcAverageRating(container, method, context);
            }
        } catch (XWikiException e) {
            throw new RatingsException(e);
        }
    }

}
