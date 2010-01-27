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

import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;

/**
 * Widget that will handle replacement of a widget with a loading panel. It gets a main widget in its constructor and a
 * loading widget set (the widget to display while loading). The switch between the two widgets will be done by calling
 * {@link LoadingWidget#startLoading()} while response will be handled by the two functions, 
 * {@link LoadingWidget#onSuccess(Object)} and {@link LoadingWidget#onFailure(Throwable)} depending on the response. 
 */
public class LoadingWidget extends DeckPanel
{
    public LoadingWidget(Widget mainWidget)
    {
        // insert this widget on first position, always
        this.insert(mainWidget, 0);
        // and show main widget, by default
        this.showWidget(0);
    }

    public LoadingWidget(Widget mainWidget, Widget loadingWidget)
    {
        this(mainWidget);
        this.setLoadingWidget(loadingWidget);
    }

    public void setLoadingWidget(Widget loadingWidget)
    {
        this.insert(loadingWidget, 1);
    }

    /**
     * Override this method to customize loading.
     */
    public void startLoading()
    {
        if (this.getWidgetCount() >= 2) {
            this.showWidget(1);
        } else {
            // Nothing happens if we don't have enough widgets. Either no widget is set, either the loading widget is
            // not set
        }
    }

    /**
     * Internal use function used to switch back from the loading panel.
     */
    protected void closeLoading()
    {
        if (this.getWidgetCount() >= 1) {
            this.showWidget(0);
        } else {
            // Nothing happens if we have no widget on the first position.
        }
    }

    /**
     * Executed on finish loading with success. By default it only switches back from the loading widget. Override this
     * to implement particular successful loading.
     */
    public void onSuccess(Object o)
    {
        this.closeLoading();
    }

    /**
     * Executed on finish loading with failure. By default it only switches back from the loading widget. Override this
     * to implement particular failed loading.
     */    
    public void onFailure(Throwable t)
    {
        this.closeLoading();
    }

    /**
     * Overrides {@link DeckPanel#showWidget(int)} to set the size of this Widget by its main widget and resize all the 
     * other panels to match the size of the main panel. The main panel is considered to be the panel at index 0.
     */
    public void showWidget(int index)
    {
        String widthString = "";
        String heightString = "";
        // if we don't show main widget we inherit width and height otherwise we set it to none so that it resizes
        if (index != 0 && this.getVisibleWidget() >= 0) {
            Widget visibleWidget = this.getWidget(this.getVisibleWidget());
            widthString = visibleWidget.getOffsetWidth() + "px";
            heightString = visibleWidget.getOffsetHeight() + "px";
        }
        super.showWidget(index);
        this.getWidget(index).setWidth(widthString);
        this.getWidget(index).setHeight(heightString);
    }
    
    public Widget getMainWidget()
    {
        return this.getWidget(0);
    }
    
    public Widget getLoadingWidget() 
    {
        return this.getWidget(1);
    }
}
