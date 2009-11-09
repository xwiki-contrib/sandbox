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
package org.xwiki.eclipse.core.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xwiki.eclipse.core.DataManager;

/**
 * This is a property tester for the Eclipse Core Expression framework for building expression containing conditions
 * about data managers. It is used in declarative handler definitions for activating/de-activating handlers.
 */
public class DataManagerPropertyTester extends PropertyTester
{
    public static final String IS_CONNECTED_PROPERTY = "isConnected";

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
     * java.lang.Object)
     */
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
    {
        DataManager dataManager = (DataManager) receiver;
        if (property.equals(IS_CONNECTED_PROPERTY)) {
            return expectedValue == null ? dataManager.isConnected()
                : dataManager.isConnected() == ((Boolean) expectedValue);
        }

        Assert.isTrue(false);

        return false;
    }

}
