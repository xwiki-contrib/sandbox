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

import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.xpn.xwiki.gwt.api.client.dialog.ModalMessageDialog;
import com.xpn.xwiki.gwt.api.client.app.XWikiGWTApp;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Describes a dialog that requires validation before closing, providing default algorithm
 * for the closing procedure and method placeholders for validation functions, to be
 * implemented by subclasses to provide particular validating behaviour.
 */
public abstract class ValidatingDialog extends Dialog
{
    protected ValidatingDialog(XWikiGWTApp xWikiGWTApp, String s, int i)
    {
        super(xWikiGWTApp, s, i);
    }

    protected ValidatingDialog(XWikiGWTApp xWikiGWTApp, String s, int i,
        AsyncCallback asyncCallback)
    {
        super(xWikiGWTApp, s, i, asyncCallback);
    }

    /**
     * Validates the dialog data before closing. Implement this function to add particular
     * validation to the exteding dialog. The function must return it's result through the passed
     * callback's <tt>onSuccess</tt>, in a {link@DialogValidationResponse}.
     *
     * @param cb callback to return the validation response.
     */
    protected abstract void validateDialogData(AsyncCallback cb);

    /**
     * Default behaviour on dialog ending: data is validated using {link@ValidatingDialog#validateDialogData},
     * and then, upon receiving validation answer, {link@ValidatingDialog#onValid} or
     * {link@ValidatingDialog#onInvalid} is called depending on the answer.
     */
    protected void endDialog()
    {
        this.validateDialogData(new XWikiAsyncCallback(this.app) {
            public void onFailure(Throwable throwable)
            {
                super.onFailure(throwable);
            }

            public void onSuccess(Object o)
            {
                super.onSuccess(o);
                DialogValidationResponse response = (DialogValidationResponse)o;
                if(response.isValid()) {
                    ValidatingDialog.this.onValid(response);
                } else {
                    ValidatingDialog.this.onInvalid(response);
                }
            }
        });
    }

    /**
     * Function called by {link@ValidatingDialog#endDialog}, when the dialog data is valid
     * (according to the response from {link@ValidatingDialog#validateDialogData}). Implement this
     * function to add particular functionality on dialog closing with valid data.
     * Default behaviour for this function is to invoke the superclass <tt>endDialog</tt>.
     * <br/>
     * Note: Although this function should provide a correct way of accessing the default Dialog 
     * endDialog functionality, it does not work properly in GWT compiled javascript code,
     * see {link@ValidatingDialog#endSuperDialog}.
     *
     * @param response the response from the validation function
     */
    public void onValid(DialogValidationResponse response)
    {
        super.endDialog();
    }

    /**
     * Hack around the fact that GWT compiled code cannot execute properly
     * SubclassDialog.super.onValid() in callback response in subclasses so we
     * provide here the ugly method to access the superclass endDialog().
     */
    public void endSuperDialog() {
        super.endDialog();
    }

    /**
     * Function called by {link@ValidatingDialog#endDialog}, when the dialog data is invalid
     * (according to the response from {link@ValidatingDialog#validateDialogData}). Override this
     * function to specify particular behaviour on invalid data. This function provides default
     * implementation: displaying a message box with the errdialog'sor message from the validation,
     * while keeping the dialog open.
     *
     * @param response the response from the validation function
     */
    public void onInvalid(DialogValidationResponse response)
    {
        String errorMessage = response.getMessage() != null
            ? response.getMessage() : getDialogTranslationName() + ".error";
        ModalMessageDialog messageDialog = new ModalMessageDialog(this.app,
            app.getTranslation(getDialogTranslationName() + ".error.caption"),
            errorMessage);
    }
}
