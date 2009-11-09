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
package org.xwoot.contentprovider;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

public interface XWootContentProviderInterface
{

    /**
     * Login to the remote XWiki.
     * 
     * @param username
     * @param password
     * @throws XWootContentProviderException
     * @throws XmlRpcException
     * @throws MalformedURLException
     */
    void login(String username, String password) throws XWootContentProviderException;

    /**
     * Logout from the remote XWiki.
     * 
     * @throws XmlRpcException
     */
    void logout();

    /**
     * Returns a list of references where each reference points to a different page at its oldest modification available
     * in the modification list that has not been cleared.
     * 
     * @return A set of XWootIds.
     * @throws XWootContentProviderException
     */
    Set<XWootId> getModifiedPagesIds() throws XWootContentProviderException;

    /**
     * Set the "cleared" flag of the modification related to the id passed as parameter. This means that the
     * modification has been processed and should not be returned in subsequent calls.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    void clearModification(XWootId xwootId) throws XWootContentProviderException;

    /**
     * Set the "cleared" flag of all the modifications up to the one (included) related to the id passed as parameter.
     * 
     * @param xwootId
     * @throws XWootContentProviderException
     */
    void clearAllModifications(XWootId xwootId) throws XWootContentProviderException;

    /**
     * Clear all the modifications. Useful for testing purpose.
     * 
     * @throws XWootContentProviderException
     */
    void clearAllModifications() throws XWootContentProviderException;

    /**
     * Returns a list of XWootObjects that contains all the entities that have been modified in the page identified by
     * the the XWootId (i.e., at a given timestamp). The contract here is that each XWootObject in the list will contain
     * only the fields that have been modified (i.e., a subset of the fields that actually make up the underlying
     * object). If the entity didn't exist in the previous version, then all the fields are present in the corresponding
     * XWootObject. Here we return only XWootObjects since we decided to process in a uniform way XWikiPages and
     * XWikiObjects, since they both can be seen as a collection of pairs name=value.
     * 
     * @param xwootId
     * @return
     * @throws XWootContentProviderException
     */
    List<XWootObject> getModifiedEntities(XWootId xwootId) throws XWootContentProviderException;

    /**
     * Equivalent to store(object, null, false)
     */
    XWootId store(XWootObject object) throws XWootContentProviderException;
    
    /**
     * Equivalent to store(object, versionAdjustement, true)
     */
    XWootId store(XWootObject object, XWootId versionAdjustement) throws XWootContentProviderException;

    /**
     * Updates XWiki's data.
     * 
     * @param object : the object to update
     * @param versionAdjustement : An XWootId that contains version number information for adjusting the
     *            page-to-be-sent's version. This is useful because clients (i.e., the synchronizer) can set the
     *            "last known version number" before trying to store the page.
     * @param useAtomicStore : true if the version-checking store should be used. This store operation checks that the
     *            entity that is going to be stored has the same version of the page on the wiki, preventing the
     *            overwriting of remotely modified pages.
     * @return An XWootId containing the pageId and the new updated version of the stored page so that clients are able
     *         to know what is the version that they have stored on the server, or null if concurrent modification
     *         detected in the meanwhile.
     * @throws XWootContentProviderException
     */
    XWootId store(XWootObject object, XWootId versionAdjustement, boolean useAtomicStore)
        throws XWootContentProviderException;

    XWootContentProviderConfiguration getConfiguration();

    boolean isConnected();

    List<Entry> getEntries(String pageId, int start, int number);

    List<Entry> getLastClearedEntries(String pageId, int start, int number);

    String getEndpoint();
}
