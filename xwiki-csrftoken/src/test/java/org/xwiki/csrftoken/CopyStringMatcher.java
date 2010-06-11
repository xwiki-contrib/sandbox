/*
 *  This file is part of xwiki-core-csrftoken
 *
 *  Copyright (c) 2008-2010 Alex Busenius
 *
 *  xwiki-core-csrftoken is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  xwiki-core-csrftoken is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with xwiki-core-csrftoken.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.xwiki.csrftoken;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;


/**
 * A matcher that acts as an action and returns the matched argument in the mocked method, allowing
 * small modifications (prepend/append something to the value).
 * 
 * @version $Id: $
 * @since 2.4
 */
public final class CopyStringMatcher extends BaseMatcher<String> implements Action 
{
    /** The string to copy. */
    private String value = null;

    /** String to prepend to the copied value. */
    private String prefix;

    /** String to append to the copied value. */
    private String suffix;

    /**
     * Create new CopyStringMatcher with the given prefix and suffix.
     * 
     * @param prefix string to prepend to the value
     * @param suffix string to append to the value
     */
    public CopyStringMatcher(String prefix, String suffix)
    {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * @see org.hamcrest.Matcher#matches(java.lang.Object)
     */
    public boolean matches(Object argument)
    {
        if (argument instanceof String) {
            value = (String) argument;
            return true;
        }
        return false;
    }

    /**
     * @see org.hamcrest.SelfDescribing#describeTo(org.hamcrest.Description)
     */
    public void describeTo(Description d)
    {
        d.appendText("COPY VALUE: ");
        d.appendValue(value);
    }

    /**
     * @see org.jmock.api.Invokable#invoke(org.jmock.api.Invocation)
     */
    public String invoke(Invocation invocation) throws Throwable
    {
        if (value == null)
            return prefix + suffix;
        return prefix + value + suffix;
    }
}

