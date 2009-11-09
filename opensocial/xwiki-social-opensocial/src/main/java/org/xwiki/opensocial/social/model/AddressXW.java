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

import org.apache.shindig.social.opensocial.model.Address;

public class AddressXW implements Address, Comparable<AddressXW>
{
    private String country;

    private Float latitude;

    private Float longitude;

    private String locality;

    private String postalCode;

    private String region;

    private String streetAddress;

    private String type;

    private String formatted;

    private Boolean primary;

    public AddressXW()
    {
    }

    public AddressXW(String formatted)
    {
        this.formatted = formatted;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public Float getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Float latitude)
    {
        this.latitude = latitude;
    }

    public String getLocality()
    {
        return locality;
    }

    public void setLocality(String locality)
    {
        this.locality = locality;
    }

    public Float getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Float longitude)
    {
        this.longitude = longitude;
    }

    public String getPostalCode()
    {
        return postalCode;
    }

    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    public String getRegion()
    {
        return region;
    }

    public void setRegion(String region)
    {
        this.region = region;
    }

    public String getStreetAddress()
    {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress)
    {
        this.streetAddress = streetAddress;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getFormatted()
    {
        return formatted;
    }

    public void setFormatted(String formatted)
    {
        this.formatted = formatted;
    }

    public Boolean getPrimary()
    {
        return primary;
    }

    public void setPrimary(Boolean primary)
    {
        this.primary = primary;
    }

    public int compareTo(AddressXW o)
    {
        if (o == null)
            return -1;
        String oCountry = o.getCountry();
        String oLocality = o.getLocality();
        String oFormatted = o.getFormatted();
        int result =
            (formatted == null) ? ((oFormatted == null) ? 0 : 1) : (oFormatted == null) ? -1 : formatted
                .compareTo(oFormatted);
        if (result == 0)
            result =
                (country == null) ? ((oCountry == null) ? 0 : 1) : (oCountry == null) ? -1 : country
                    .compareTo(oCountry);
        if (result == 0)
            result =
                (locality == null) ? ((oLocality == null) ? 0 : 1) : (oLocality == null) ? -1 : locality
                    .compareTo(oLocality);
        return result;
    }
}
