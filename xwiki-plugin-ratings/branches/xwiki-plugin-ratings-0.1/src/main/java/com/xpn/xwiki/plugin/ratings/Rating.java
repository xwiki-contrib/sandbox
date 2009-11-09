package com.xpn.xwiki.plugin.ratings;

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
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.XWikiContext;

import java.util.Date;

public interface Rating
{
    /**
     * Retrieves the container to which this rating applies
     * This can be a page, a rating, or something else
     * @return Container parent container
     */
    Container getContainer();

    /**
     * Retrives the current rating as a BaseObject
     * This method is used for compatiblity
     * @return BaseObject rating object
     * @throws RatingsException
     */
    BaseObject getAsObject() throws RatingsException;


    /**
     * Allows to access the ratings manager used to manage this rating
     * @return RatingsManager ratings manager
     */
    RatingsManager getRatingsManager();

    /**
     * Retrieves the rating unique ID allowing to distinguish it from other ratings of the same container
     * @return  String rating ID
     */
    String getRatingId();

    /**
     * Retrieves the rating unique ID allowing to find the rating
     * @return  String rating ID
     */
    String getGlobalRatingId();

    /**
     * Retrives the current rating author
     * @return String author of the rating
     */
    String getAuthor();

    /**
     * Retrieves the date of the rating
     * @return Date date of the rating
     */
    Date getDate();

    /**
     * Retrieves the rating value
     * @return int value of rating
     */
    int getVote();

    /**
     * Retrieves additional properties
     * @param propertyName
     * @return Object property value
     */
    Object get(String propertyName);

    /**
     * Retrieves additional properties
     * @param propertyName
     * @return Object property value
     */
    String display(String propertyName, String mode, XWikiContext context);

    void setAuthor(String author);

    void setDate(Date date);

    void setVote(int vote);

    void save() throws RatingsException;

    boolean remove() throws RatingsException;

    String toString();
}
