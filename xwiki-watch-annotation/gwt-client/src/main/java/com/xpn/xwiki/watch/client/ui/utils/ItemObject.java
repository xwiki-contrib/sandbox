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
 */
package com.xpn.xwiki.watch.client.ui.utils;

import com.google.gwt.user.client.ui.Widget;

/**
 * Class to enclose data to be stored by a interface item that should be able to:
 * <ul>
 * <li> get uniquely identifying key for this item </li>
 * <li> generate a widget to be displayed by the item </li>
 * </ul>
 */
public abstract class ItemObject {
    //the unique ID 
    protected String key;
    //the data of the ItemObject
    protected Object data; 
    
    public ItemObject(String key, Object data)
    {
        this.key = key;
        this.data = data;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public abstract Widget getWidget(boolean selected);
}