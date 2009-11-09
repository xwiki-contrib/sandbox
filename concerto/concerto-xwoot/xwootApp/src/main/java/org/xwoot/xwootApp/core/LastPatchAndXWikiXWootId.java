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
package org.xwoot.xwootApp.core;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.xwoot.contentprovider.XWootId;
import org.xwoot.xwootApp.XWootException;
import org.xwoot.xwootUtil.PersistencyUtil;

/**
 * Manages a content id map which associates a {@link XWootId} to a set of content id, gets the map, adds content id or
 * removes a given number of occurrences of a content id in an entry.
 * <p>
 * It is used by {@link XWoot} to store last modified contents id. The {@link XWoot} user can consume contents id of the
 * Map.
 * 
 * @version $Id$
 */
public class LastPatchAndXWikiXWootId
{
    /** The name of the file where to serialize. */
    public static final String SERIALIZATION_FILE_NAME = "ids";

    /** map : ({@link XWootId}pageName,contentId set). */
    private Map<XWootId, Set<String>> patchIdMap;
    
    /** map : (String pageName,{@link XWootId}). */
    private Map<String,XWootId> xwikiIdMap;

    /** The file path where to serialize. */
    private String serializationFilePath;

    /**
     * Creates a new LastModifiedContentIdMap instance to be used by the {@link ContentManager}. The PageManager will
     * add the id of each modified content in a page. A consumer can gets the Map and removes some occurrences of a
     * given ContentId in an entry.
     * 
     * @param xwootWorkingDirPath the workingDir for the XWoot the page manager belongs to.
     */
    public LastPatchAndXWikiXWootId(String xwootWorkingDirPath)
    {
        this.serializationFilePath = xwootWorkingDirPath + File.separator + SERIALIZATION_FILE_NAME;
    }

    /**
     * Serializes the map.
     * 
     * @throws XWootException if serialization or file access problems occur.
     */
    private void storePatchIdMap() throws XWootException
    {
        if (!this.patchIdMap.isEmpty()) {
            try {
                PersistencyUtil.saveObjectToXml(this.patchIdMap, this.getPatchIdMapFilePath());
            } catch (Exception e) {
                throw new XWootException("Problems storing the patch id Map.", e);
            }
        } else {
            new File(this.getPatchIdMapFilePath()).delete();
        }
    }
    
    /**
     * Serializes the map.
     * 
     * @throws XWootException if serialization or file access problems occur.
     */
    private void storeXWikiIdMap() throws XWootException
    {
        if (!this.xwikiIdMap.isEmpty()) {
            try {
                PersistencyUtil.saveObjectToXml(this.xwikiIdMap, this.getXWikiIdMapFilePath());
            } catch (Exception e) {
                throw new XWootException("Problems storing the xwiki id map.", e);
            }
        } else {
            new File(this.getXWikiIdMapFilePath()).delete();
        }
    }

    /**
     * Loads the map previously stored. Creates a new map if it has never been stored.
     * 
     * @throws XWootException if the loading causes deserializing problems.
     */
    @SuppressWarnings("unchecked")
    private void loadPatchIdMap() throws XWootException
    {
        String filePath = this.getPatchIdMapFilePath();

        if (!new File(filePath).exists()) {
            this.setPatchIdMap(new Hashtable<XWootId, Set<String>>());
        } else {
            try {
                Hashtable<XWootId, Set<String>> map =
                    (Hashtable<XWootId, Set<String>>) PersistencyUtil.loadObjectFromXml(filePath);
                if (map == null) {
                    this.setPatchIdMap(new Hashtable<XWootId, Set<String>>());
                } else {
                    this.setPatchIdMap(map);
                }
            } catch (Exception e) {
                throw new XWootException("Problems loading the patch id Map.", e);
            }
        }
    }
    
    /**
     * Loads the map previously stored. Creates a new list if it has never been stored.
     * 
     * @throws XWootException if the loading causes deserializing problems.
     */
    @SuppressWarnings("unchecked")
    private void loadXWikiIdMap() throws XWootException
    {
        String filePath = this.getXWikiIdMapFilePath();

        if (!new File(filePath).exists()) {
            this.setXWikiIdMap(new Hashtable<String,XWootId>());
        } else {
            try {
                Hashtable<String,XWootId> map =
                    ( Hashtable<String,XWootId>) PersistencyUtil.loadObjectFromXml(filePath);
                if (map == null) {
                    this.setXWikiIdMap(new Hashtable<String,XWootId>());
                } else {
                    this.setXWikiIdMap(map);
                }
            } catch (Exception e) {
                throw new XWootException("Problems loading the xwiki id map.", e);
            }
        }
    }

    /**
     * @return the map.
     */
    private Map<XWootId, Set<String>> getPatchIdMap()
    {
        return this.patchIdMap;
    }
    
    /**
     * @return the map.
     */
    private Map<String,XWootId> getXWikiIdMap()
    {
        return this.xwikiIdMap;
    }

    /**
     * @param map the map to set.
     */
    private void setXWikiIdMap(Map<String,XWootId> map)
    {
        this.xwikiIdMap = map;
    }

    /**
     * @param map the map to set.
     */
    private void setPatchIdMap(Map<XWootId, Set<String>> map)
    {
        this.patchIdMap = map;
    }
    
    /**
     * @return the path of the file where the map is serialized.
     */
    private String getPatchIdMapFilePath()
    {
        return this.serializationFilePath+"1";
    }
    
    /**
     * @return the path of the file where the map is serialized.
     */
    private String getXWikiIdMapFilePath()
    {
        return this.serializationFilePath+"2";
    }

    /**
     * @param pageName the {@link XWootId} corresponding to the name of container page of the content id to add.
     * @param contentId the content id to add.
     * @throws XWootException if problems occur while loading/storing the Map.
     */
    public void add2PatchIdMap(XWootId pageName, String contentId) throws XWootException
    {
        this.loadPatchIdMap();
        Set<String> contents = this.getPatchIdMap().get(pageName);
        if (contents == null) {   
            contents = new TreeSet<String>();
            this.getPatchIdMap().put(pageName, contents);
        }
        contents.add(contentId);

        this.storePatchIdMap();
    }
    
    /**
     * @param xwootId the {@link XWootId} corresponding to the id to add.
     * @param pageName the pagename of the corresponding xwootId to add.
     * @throws XWootException if problems occur while loading/storing the Map.
     */
    public void add2XWikiIdMap(String pageName,XWootId id) throws XWootException
    {
        this.loadXWikiIdMap();  
        XWootId old_id=this.xwikiIdMap.get(pageName);
        if (old_id==null 
            || (old_id.getVersion()<id.getVersion() 
                || (old_id.getVersion()==id.getVersion() 
                    && old_id.getMinorVersion()<id.getMinorVersion()))){            
            this.getXWikiIdMap().put(pageName, id);
            this.storeXWikiIdMap();
        }
       
    }

    /**
     * @return the map, not as it is in memory, but as it is stored on drive.
     * @throws XWootException if problems occur while loading the Map.
     */
    public Map<XWootId, Set<String>> getCurrentPatchIdMap() throws XWootException
    {
        this.loadPatchIdMap();
        return this.getPatchIdMap();
    }
    
    public XWootId getXwikiId(String pageName) throws XWootException
    {
        this.loadXWikiIdMap();
        return this.getXWikiIdMap().get(pageName);
    }

//    public void remove(XWootId id) throws XWootException
//    {
//        this.loadContentIdMap();
//        this.contentIdMap.remove(id);
//        this.storeContentIdMap();
//    }

    public void removePatchId(XWootId xwid, String objectId) throws XWootException
    {
        this.loadPatchIdMap();
        Set<String> contents = this.getPatchIdMap().get(xwid);
        if (contents == null) {   
           return ;
        }
        contents.remove(objectId);
        if (contents.isEmpty()){
            this.getPatchIdMap().remove(xwid);
        }

        this.storePatchIdMap();
        
    }
    
    public void removeAllPatchId() throws XWootException
    {
        this.loadPatchIdMap();
        this.getPatchIdMap().clear();
        this.storePatchIdMap();
        
    }
}
