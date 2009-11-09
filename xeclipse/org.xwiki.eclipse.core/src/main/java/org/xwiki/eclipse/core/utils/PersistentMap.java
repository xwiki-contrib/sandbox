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
 *
 */
package org.xwiki.eclipse.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.xwiki.eclipse.core.CoreLog;

/**
 * A class that implements a Map from strings to strings, that saves its content to the disk.
 */
public class PersistentMap
{
    private IFile file;

    private Map<String, String> map;

    /**
     * Constructor.
     * 
     * @param file The file where to synchronize the map content.
     * @throws CoreException
     */
    public PersistentMap(IFile file) throws CoreException
    {
        this.file = file;

        if (file.exists()) {
            map = (Map<String, String>) CoreUtils.readDataFromXML(file);
        } else {
            map = new HashMap<String, String>();
            synchronize();
        }
    }

    public void put(String key, String value)
    {
        map.put(key, value);
        synchronize();
    }

    public void remove(String key)
    {
        if (!map.containsKey(key)) {
            return;
        }

        map.remove(key);
        synchronize();
    }

    public String get(String key)
    {
        return map.get(key);
    }

    public Set<String> keySet()
    {
        return map.keySet();
    }

    private void synchronize()
    {
        try {
            CoreUtils.writeDataToXML(file, map);
        } catch (CoreException e) {
            CoreLog.logError("Unable to synchronize persistent map", e);
        }
    }
}
