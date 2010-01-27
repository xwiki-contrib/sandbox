package com.xpn.xwiki.watch.client.data;

import com.xpn.xwiki.gwt.api.client.XObject;

/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software;you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation;either version2.1of
 * the License,or(at your option)any later version.
 * <p/>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software;if not,write to the Free
 * Software Foundation,Inc.,51 Franklin St,Fifth Floor,Boston,MA
 * 02110-1301 USA,or see the FSF site:http://www.fsf.org.
 *
 * @author ldubost
 */

public class Keyword {
    private String name;
    private String group;
    private String pageName;

    public Keyword(XObject xobj) {
        setName((String) xobj.getProperty("name"));
        setGroup((String) xobj.getProperty("group"));
        setPageName(xobj.getName());
    }
    
    public Keyword(String keyword, String group) {
        this.name = keyword;
        this.group = group;
    }

    public String getDisplayName() {
        if ((group!=null)&&(!group.equals("")))
         return name + " - " + group;
        else
         return name;        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group == null ? "" : this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPageName() {
        return pageName == null ? "" : this.pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
