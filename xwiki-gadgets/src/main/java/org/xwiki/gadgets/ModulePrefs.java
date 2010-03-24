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
package org.xwiki.gadgets;

import java.util.HashMap;
import java.util.Map;

/**
 * A Google Gadget Module Preferences.
 * 
 * @see <a href="http://code.google.com/apis/gadgets/docs/reference.html#Moduleprefs_Ref"> Gadgets XML Reference -
 *      ModulePrefs</a>
 * @version $Id$
 */
public class ModulePrefs
{
    /**
     * Module preferences like: title, title_url, description, author, author_email, screenshot, thumbnail (attributes
     * that are supported in all containers) and other, mapped to their values. All attributes are optional. See your
     * container documentation for any container-specific attributes.
     */
    private Map<String, String> attrs;

    /**
     * Initializes the preferences map.
     */
    public ModulePrefs()
    {
        this.attrs = new HashMap<String, String>();
    }

    /**
     * @param key the Module Preference key attribute, like: title, title_url, description, author, author_email,
     *            screenshot, thumbnail (attributes that are supported in all containers) and other, mapped to their
     *            values
     * @return the value associated to the key, null if key not found (or value is actually null)
     */
    public String get(String key)
    {
        return attrs.get(key);
    }

    /**
     * Associates the specified value with the specified key. If the Module Preference previously contained a mapping
     * for the key, the old value is replaced by the specified value.
     * 
     * @param key the Module Preference key attribute, like: title, title_url, description, author, author_email,
     *            screenshot, thumbnail (attributes that are supported in all containers) and other, mapped to their
     *            values
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also
     *         indicate that the map previously associated null with key)
     */
    public String set(String key, String value)
    {
        return attrs.put(key, value);
    }
}
