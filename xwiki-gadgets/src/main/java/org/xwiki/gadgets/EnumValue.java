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

/**
 * One of the values you can specify for the {@link UserPref#getDatatype()} attribute is enum. The enum data type is
 * presented in the user interface as a menu of choices. You specify the contents of the menu using {@link EnumValue}.
 * 
 * @see <a href="http://code.google.com/apis/gadgets/docs/reference.html#Userprefs_Ref"> Gadgets XML Reference - Enum
 *      Data Types</a>
 * @version $Id$
 */
public class EnumValue
{
    /**
     * Required string that provides a unique value. This value is displayed in the menu in the user preferences edit
     * box unless a display_value is provided.
     */
    private String value;

    /**
     * Optional string that is displayed in the menu in the user preferences edit box. If you do not specify a
     * display_value, the value is displayed in the user interface.
     */
    private String displayValue;

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the display value
     */
    public String getDisplayValue()
    {
        return displayValue;
    }

    /**
     * @param displayValue The display value
     */
    public void setDisplayValue(String displayValue)
    {
        this.displayValue = displayValue;
    }
}
