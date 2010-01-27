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

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Widget;

/**
 * Keyboard listener to handle a return in an input box and execute click on the button passed in the constructor.
 */
public class ButtonClickReturnHandler extends KeyboardListenerAdapter
{
    /**
     * Button to be clicked when this listener is triggered
     */
    protected Button button;
    
    public ButtonClickReturnHandler(){}
    
    public ButtonClickReturnHandler(Button button) {
        this.button = button;
    }
    
    public void onKeyPress(Widget sender, char keyCode, int modifiers)
    {
        if (keyCode == 13) {
            this.button.click();
        }
    }
}
