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

import com.xpn.xwiki.watch.client.Watch;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle;
import java.util.Iterator;
import java.util.List;


/**
 * Oracle that suggests a tag list based on tags fetched only once, upon creation.
 */
public class TagListSuggestOracle extends SuggestOracle {
    protected MultiWordSuggestOracle defaultSuggest;
    protected Watch app;
    private boolean fetched = false;
    

    public TagListSuggestOracle(Watch app) {
        this.app = app;
        this.defaultSuggest = new MultiWordSuggestOracle();
    }

    public void requestSuggestions(final Request request, final Callback callback) {
        //if tags haven't been fetched, fetch tags and put them in the list
        if (!this.fetched) {
            this.app.getDataManager().getTagsList(new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    //keep quiet, we just won't have any suggestions
                }
                public void onSuccess(Object o) {
                    for (Iterator lIt = ((List)o).iterator(); lIt.hasNext();) {
                        TagListSuggestOracle.this.defaultSuggest.add((String)((List)lIt.next()).get(0));
                    }
                    TagListSuggestOracle.this.fetched = true;
                    //and don't forget to suggest!
                    TagListSuggestOracle.this.defaultSuggest.requestSuggestions(request, callback);                    
                }
            });
        }
        this.defaultSuggest.requestSuggestions(request, callback);
    }

    public boolean isDisplayStringHTML() {
        return this.defaultSuggest.isDisplayStringHTML();
    }
}
