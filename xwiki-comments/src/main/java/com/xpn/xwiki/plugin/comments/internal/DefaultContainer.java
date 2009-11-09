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

package com.xpn.xwiki.plugin.comments.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.comments.Container;
import com.xpn.xwiki.plugin.comments.Comment;

import java.util.HashMap;
import java.util.Map;

public class DefaultContainer implements Container
{
    private static Log LOG = LogFactory.getLog(DefaultContainer.class);

    private String docName;

    private int section;

    private String phrase;

    private Comment comment;

    private HashMap<String, Object> data = new HashMap<String, Object>();

    public XWikiContext context;

    public DefaultContainer(XWikiContext context)
    {
        this(null, -1, null, null, context);
    }

    public DefaultContainer(String docName, int sectionNumber, String phrase, Comment comment, XWikiContext context)
    {
        this.docName = docName;
        this.section = sectionNumber;
        this.phrase = phrase;
        this.comment = comment;
        this.context = context;
    }

    public String getDocumentName()
    {
        return docName;
    }

    public void setDocumentName(String docName)
    {
        this.docName = docName;
    }

    public int getSection()
    {
        return section;
    }

    public void setSection(int sectionNumber)
    {
        this.section = sectionNumber;
    }

    public String getPhrase()
    {
        return phrase;
    }

    public void setPhrase(String phrase)
    {
        this.phrase = phrase;
    }

    public Comment getComment()
    {
        return comment;
    }

    public void setComment(Comment comment)
    {
        this.comment = comment;
    }

    public Container getParentContainer()
    {
        if (comment!=null)
         return new DefaultContainer(docName, section, phrase, null, context);

        if (phrase!=null)
                 return new DefaultContainer(docName, section, null, null, context);

        if (section!=-1)
                 return new DefaultContainer(docName, -1, null, null, context);

        return null;
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void put(String key, Object value) {
        this.data.put(key,value);
    }

}
