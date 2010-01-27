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
package com.xpn.xwiki.watch.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;
import java.util.List;

public class NewArticlesMonitoring {

    private Watch watch;

    private Timer checkNbMessageTimer;
    private Timer blinkTimer;
    private String windowTitle;
    private String messageTitle;
    // initialize on -1 just to be sure that it's going to be updated at least once
    private int totalArticles = -1;
    private int newArticles = -1;
    private Date lastChange;
    private boolean blinking = false;
    private boolean queryActive = false;

    public NewArticlesMonitoring() {}

    public NewArticlesMonitoring(Watch watch) {
        this.watch = watch;

        // Schedule the timer to run once every 10 seconds
        checkNbMessageTimer = new Timer() {
            public void run() {
                    onCheckNew();
            }
        };
        checkNbMessageTimer.scheduleRepeating(watch.getParamAsInt("newarticles_monitoring_timer", Constants.DEFAULT_PARAM_NEWARTICLES_MONITORING_TIMER));        
    }

    /**
     * Checking if we have articles
     */
    private void onCheckNew()
    {
        if (queryActive == false) {
            queryActive = true;
            watch.getDataManager().getNewArticlesCount(new AsyncCallback()
            {
                public void onFailure(Throwable throwable)
                {
                    queryActive = false;
                }

                public void onSuccess(Object object)
                {
                    queryActive = false;
                    if (object != null) {
                        // get the result of the query
                        List articlesCountList = (List) object;
                        // now get its two components: total articles and unread articles
                        if (articlesCountList != null) {
                            int updatedNewArticles = 0;
                            int updatedTotalArticles = 0;
                            for (int i = 0; i < articlesCountList.size(); i++) {
                                List currentFeedArticlesCount = (List) articlesCountList.get(i);
                                // total articles are on position 2
                                updatedTotalArticles += ((Number) currentFeedArticlesCount.get(2)).intValue();
                                // new articles are on position 1
                                updatedNewArticles += ((Number) currentFeedArticlesCount.get(1)).intValue();
                            }
                            if (updatedNewArticles == newArticles) {
                                // nothing to update
                                return;
                            }
                            // update the title bar with the new value
                            newArticles = updatedNewArticles;
                            totalArticles = updatedTotalArticles;
                            String[] argsNewArticles = new String[1];
                            argsNewArticles[0] = "" + newArticles;
                            String[] argsTotalArticles = new String[1];
                            argsTotalArticles[0] = "" + totalArticles;
                            Window.setTitle(watch.getTranslation("productname") + ": "
                                + watch.getTranslation("newarticles", argsNewArticles) + " / "
                                + watch.getTranslation("articles", argsTotalArticles));
                        }
                        // since we detected a change in the new articles count,
                        // we might as well refresh the concerned whole UI
                        watch.refreshArticleNumber();
                    }
                }
            });
        }
    }

    public void startBlinking(String message, String otherMessage) {
        if (otherMessage!=null)
            windowTitle = otherMessage;
        messageTitle = message;
        if (blinkTimer==null) {
            windowTitle = Window.getTitle();
            Window.setTitle(message);
            blinkTimer = new Timer() {
                boolean active = true;

                public void run() {
                    if (active) {
                        Window.setTitle(windowTitle);
                    } else {
                        Window.setTitle(messageTitle);
                    }
                    active = !active;
                }
            };
            if (!blinking) {
                blinking = true;
                blinkTimer.scheduleRepeating(2000);
            }
        }
    }

    public void stopBlinking() {
        if (blinkTimer!=null) {
            blinkTimer.cancel();
            blinking = false;
            blinkTimer = null;
            Window.setTitle(messageTitle);
        }
    }

    public int getArticlesNumber() {
        return totalArticles;
    }

    public Date lastChangeDate() {
        return lastChange;
    }
}
