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

package org.xwiki.escaping;

import org.junit.experimental.theories.suppliers.TestedOn;
import org.junit.runner.RunWith;
import org.xwiki.escaping.framework.ArchiveSuite;
import org.xwiki.escaping.framework.ArchiveSuite.ArchivePathGetter;


/**
 * Runs the automatically generated escaping tests for all velocity templates found in XWiki enterprise war.
 * 
 * @version $Id$
 * @since 2.5
 */
@RunWith(ArchiveSuite.class)
public class TemplateTest
{
    /**
     * The templates are read from the XWiki XAR, defined in a maven property.
     * 
     * @return local path to the XAR archive to use
     */
    @ArchivePathGetter
    public static String getArchivePath()
    {
        return System.getProperty("localRepository") + "/" + System.getProperty("pathToXWikiXar");
    }
}

