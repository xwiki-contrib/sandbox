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
package org.xwiki.opensocial.social.model;

import org.apache.shindig.social.opensocial.model.Name;

public class NameXW implements Name, Comparable<NameXW>
{

    private String additionalName;

    private String familyName;

    private String givenName;

    private String honorificPrefix;

    private String honorificSuffix;

    private String formatted;

    public NameXW()
    {
    }

    public NameXW(String formatted)
    {
        this.formatted = formatted;
    }

    public String getFormatted()
    {
        return formatted;
    }

    public void setFormatted(String formatted)
    {
        this.formatted = formatted;
    }

    public String getAdditionalName()
    {
        return additionalName;
    }

    public void setAdditionalName(String additionalName)
    {
        this.additionalName = additionalName;
    }

    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    public String getGivenName()
    {
        return givenName;
    }

    public void setGivenName(String givenName)
    {
        this.givenName = givenName;
    }

    public String getHonorificPrefix()
    {
        return honorificPrefix;
    }

    public void setHonorificPrefix(String honorificPrefix)
    {
        this.honorificPrefix = honorificPrefix;
    }

    public String getHonorificSuffix()
    {
        return honorificSuffix;
    }

    public void setHonorificSuffix(String honorificSuffix)
    {
        this.honorificSuffix = honorificSuffix;
    }

    public int compareTo(NameXW o)
    {
        if (o == null)
            return -1;
        String oGivenName = o.getGivenName();
        String oFamilyName = o.getFamilyName();
        String oFormatted = o.getFormatted();
        int result =
            (formatted == null) ? ((oFormatted == null) ? 0 : 1) : (oFormatted == null) ? -1 : formatted
                .compareTo(oFormatted);

        if (result == 0)
            result =
                (familyName == null) ? ((oFamilyName == null) ? 0 : 1) : (oFamilyName == null) ? -1 : familyName
                    .compareTo(oFamilyName);
        if (result == 0)
            result =
                (givenName == null) ? ((oGivenName == null) ? 0 : 1) : (oGivenName == null) ? -1 : givenName
                    .compareTo(oGivenName);
        return result;
    }
}
