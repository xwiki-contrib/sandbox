/*  
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
 */
package com.xpn.xwiki.watch.client.ui.utils;

import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;

/**
 * Loading widget that treats failure by displaying the failure message in a dialog, as the default error handling in 
 * {@link XWikiAsyncCallback}.
 */
public class DefaultLoadingWidget extends LoadingWidget
{   
    protected XWikiGWTApp app;
    
    public DefaultLoadingWidget(XWikiGWTApp app, Widget mainWidget, Widget loadingWidget)
    {
        super(mainWidget, loadingWidget);        
        this.app = app;
    }

    public DefaultLoadingWidget(XWikiGWTApp app, Widget mainWidget)
    {
        super(mainWidget);
        this.app = app;
    }

    /**
     * Overrides default failure behaviour to displauy a dialog showing the error.
     */
    public void onFailure(Throwable t)
    {
        super.onFailure(t);
        // show the error as default
        this.app.showError(t);
    }
}
