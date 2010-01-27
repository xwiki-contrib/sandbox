/**
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

package com.xpn.xwiki.watch.client.ui.dialog;

/**
 * Store information about the validity of a dialog, to be used as a response from
 * an asynchronous call doing dialog data validation.
 *
 * @see com.xpn.xwiki.watch.client.ui.dialog.FeedDialog
 *      #validateFeedData(com.google.gwt.user.client.rpc.AsyncCallback)
 */
public class DialogValidationResponse
{
    protected boolean valid;
    protected String message;
    protected int code;
    protected Object data;

    public DialogValidationResponse()
    {
        this(false, 0, null);        
    }

    public DialogValidationResponse(boolean valid) {
        this(valid, 0, null);
    }

    public DialogValidationResponse(boolean valid, int code) {
        this(valid, code, null);        
    }

    public DialogValidationResponse(boolean valid, String message) {
        this(valid, 0, message);

    }

    public DialogValidationResponse(boolean valid, int code, String message) {
        this.valid = valid;
        this.code = code;
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isValid()
    {
        return valid;
    }
    
    public int getCode()
    {
        return this.code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }
}
