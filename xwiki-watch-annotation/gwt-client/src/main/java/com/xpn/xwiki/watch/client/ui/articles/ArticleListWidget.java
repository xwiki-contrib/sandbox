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
package com.xpn.xwiki.watch.client.ui.articles;

import com.xpn.xwiki.watch.client.ui.WatchWidget;
import com.xpn.xwiki.watch.client.ui.dialog.TagListSuggestOracle;
import com.xpn.xwiki.watch.client.ui.utils.DefaultLoadingWidget;
import com.xpn.xwiki.watch.client.ui.utils.HTMLMessages;
import com.xpn.xwiki.watch.client.ui.utils.LoadingAsyncCallback;
import com.xpn.xwiki.watch.client.ui.utils.LoadingWidget;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.Constants;
import com.xpn.xwiki.watch.client.data.Feed;
import com.xpn.xwiki.watch.client.data.FeedArticle;
import com.xpn.xwiki.watch.client.data.FeedArticleComment;
import com.xpn.xwiki.gwt.api.client.widgets.WordListSuggestOracle;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public class ArticleListWidget extends WatchWidget {

    public ArticleListWidget() {
        super();
    }

    public String getName() {
        return "articlelist";
    }

    public ArticleListWidget(Watch watch) {
        super(watch);
        setPanel(new FlowPanel());
        initWidget(panel);
        init();
    }

    public void init() {
        super.init();
    }

    public void refreshData() {
        List articlesList = watch.getConfig().getArticles();
        showArticles(articlesList);
        //and refresh the contained nav-bar widgets
        watch.getUserInterface().refreshData("navbar");
        watch.getUserInterface().refreshData("navbar-bottom");
        resizeWindow();        
    }


    public void showArticles(List feedentries) {
        panel.clear();

        if ((feedentries==null)||(feedentries.size()==0)) {
            panel.add(HTMLMessages.getInfoHTML(watch.getTranslation("articlelist.noarticles")));
            return;
        }

        panel.add(new ActionBarWidget(watch));
        panel.add(new NavigationBarWidget(watch));
        
        for (int i=0;i<feedentries.size();i++) {
            FeedArticle article = (FeedArticle)feedentries.get(i);
            showArticle(article);
        }
        //put the navbar at the bottom as well
        panel.add(new NavigationBarWidget(watch) {
            public String getName()
            {
                return "navbar-bottom";
            }
        });
    }

    protected Widget getHeadlinePanel(FeedArticle article, Widget articlePanel, Widget contentZonePanel) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "headline"));
        p.add(getLeftActionsPanel(article));
        p.add(getHeaderPanel(article, articlePanel, contentZonePanel));
        p.add(getRightActionsPanel(article));
        return p;
    }

    protected Widget getHeaderPanel(FeedArticle article, Widget articlePanel, Widget contentZonePanel) {
        VerticalPanel p = new VerticalPanel();
        p.setStyleName(watch.getStyleName("article", "header"));
        p.add(getTitlePanel(article, articlePanel, contentZonePanel));
        p.add(getDetailsPanel(article));
        p.add(contentZonePanel);
        HTML commentsStatus = new HTML ();
        commentsStatus.setStyleName(watch.getStyleName("article", "comments-status"));
        int nbcomments = article.getCommentsNumber();
        if ((nbcomments > 0) || watch.getConfig().getHasCommentRight()) {
            commentsStatus.addStyleName("clickable");
        }
        String commentTitle = watch.getTranslation("nocomments");
        if (nbcomments != 0) {
            commentTitle = nbcomments + " " + watch.getTranslation("comments");
        }
        commentsStatus.setHTML(commentTitle);        
        Widget commentsZonePanel = getCommentsZonePanel(article, commentsStatus);
        p.add(getStatusPanel(article, commentsZonePanel, commentsStatus));
        p.add(commentsZonePanel);
        return p;
    }
    
    protected Widget getDetailsPanel(FeedArticle article) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "details"));
        p.add(getFeedNamePanel(article));
        Label onLabel = new Label(watch.getTranslation("wiki.posted.date"));
        onLabel.addStyleName(watch.getStyleName("article-date-on"));
        p.add(onLabel);
        p.add(getDatePanel(article));        
        return p;
    }
    
    protected Widget getFeedLogoPanel(FeedArticle article) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "logo"));

        Image feedLogo = null;
        String feedName = article.getFeedName();
        if ((feedName != null) && (!feedName.equals(""))) {
            Feed feed = (Feed) watch.getConfig().getFeedsList().get(feedName);
            if (feed != null) {
                String imgurl = watch.getFavIcon(feed);
                if (imgurl != null) {
                    feedLogo = new Image(imgurl);
                }
            }
        }
        
        // if the image was not found, use the default
        if (feedLogo == null) {
            feedLogo = new Image(watch.getSkinFile(Constants.IMAGE_FEED));
        }
        
        p.add(feedLogo);

        return p;
    }

    protected Panel getTitlePanel(final FeedArticle article, final Widget articlePanel, final Widget contentZonePanel) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "title"));
        
        HTML titleLabel = new HTML(article.getTitle());
        p.add(titleLabel);

        titleLabel.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                ArticleListWidget.this.showContentPanel(!contentZonePanel.isVisible(), (ComplexPanel) contentZonePanel,
                    article);
                resizeWindow();
                watch.getDataManager().updateArticleReadStatus(article, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                    }
                    public void onSuccess(Object object) {
                        articlePanel.removeStyleName(watch.getStyleName("article", "unread"));
                        articlePanel.addStyleName(watch.getStyleName("article", "read"));
                    }
                });
            }
        });
        return p;
    }

    protected Widget getFeedNamePanel(FeedArticle article) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "feedname"));
        p.add(getFeedLogoPanel(article));
        
        HTML htmlFeedName = new HTML();
        htmlFeedName.setStyleName(watch.getStyleName("article", "feedname-text"));
        // Get the feed title from the feed name in the article and display it
        Feed articleFeed = (Feed)watch.getConfig().getFeedsList().get(article.getFeedName());
        htmlFeedName.setHTML(
                articleFeed == null ? article.getFeedName() 
                : (articleFeed.getTitle().trim().length() > 0 ? articleFeed.getTitle() : articleFeed.getName()));
        // TODO: [Proposal] when clicking on a feed name, set FilterStatus to activate the feed
        p.add(htmlFeedName);
        return p;
    }

    protected Widget getDatePanel(FeedArticle article) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "date"));
        HTML htmlDate = new HTML();
        htmlDate.setStyleName(watch.getStyleName("article", "date-text"));
        // TODO: implement me:  Get date in format dd MMM yyyy, day_of_week tt:tt
        // TODO: tt:tt - I don't care about seconds
        // TODO: day of week is necessary for "This Week" Filter
        // TODO: dd MMM yyyy - 10 Feb 2008 format clarifies european/american date format 
        htmlDate.setHTML(article.getDate());
        // TODO: [Proposal] when clicking on a date, set FilterStatus to activate the date period
        p.add(htmlDate);
        return p;
    }

    protected Widget getLeftActionsPanel(final FeedArticle article) {
        final FlowPanel actionsPanel = new FlowPanel();
        actionsPanel.setStyleName(watch.getStyleName("article", "actionsleft"));
        updateLeftActionsPanel(actionsPanel, article);
        return actionsPanel;
    }
    
    //TODO: implement me
    protected void updateLeftActionsPanel(final FlowPanel actionsPanel, final FeedArticle article) {
        // TODO: uncomment this when we'll implement checkboxes usage 
//        CheckBox selectArticle = new CheckBox();
//        //TODO: replace article.getFlagStatus with article.getSelectStatus
//        selectArticle.setTitle(watch.getTranslation((article.getFlagStatus()==-1) ? "article.select.remove.caption" : "article.select.add.caption"));
//        selectArticle.addClickListener(new ClickListener() {
//            public void onClick(Widget widget) {
//               //TODO: implement me
//            }
//        });
//        actionsPanel.add(selectArticle);

        Image flagImage = new Image(watch.getSkinFile((article.getFlagStatus() == 1) 
            ? Constants.IMAGE_FLAG_ON  : Constants.IMAGE_FLAG_OFF));
        
        // put the flag action only if the user has the right to edit
        if (watch.getConfig().getHasEditRight()) {
            Image loadingFlagImage = new Image(watch.getSkinFile(Constants.IMAGE_LOADING_SPINNER));
            flagImage.setTitle(watch.getTranslation((article.getFlagStatus() == 1) 
                                                    ? "article.flag.remove.caption" 
                                                    : "article.flag.add.caption"));
            //create a loading widget with the flag image as main widget and loadingFlagImage as loading widget
            final LoadingWidget flagLoadingWidget = new DefaultLoadingWidget(watch, flagImage, loadingFlagImage);
            flagLoadingWidget.addStyleName(watch.getStyleName("article-flag"));
            flagImage.addStyleName("clickable");
            flagImage.addClickListener(new ClickListener() {
                public void onClick(Widget widget) {
                        int flagstatus = article.getFlagStatus();
                        final int newflagstatus = (flagstatus == 1) ? 0 : 1;
                        watch.getDataManager().updateArticleFlagStatus(article, newflagstatus, 
                            new LoadingAsyncCallback(flagLoadingWidget) {
                            	public void onFailure(Throwable caught) {
                            		super.onFailure(caught);
                            	}
                                public void onSuccess(Object result) {
                                     super.onSuccess(result);
                                     article.setFlagStatus(newflagstatus);
                                     actionsPanel.clear();
                                     updateLeftActionsPanel(actionsPanel, article);
                                }
                            });
                    }
            });
            actionsPanel.add(flagLoadingWidget);
        } else {
            // otherwise, add only the image 
            actionsPanel.add(flagImage);
        }
    }
    
    protected Widget getRightActionsPanel(final FeedArticle article) {
        final FlowPanel actionsPanel = new FlowPanel();
        actionsPanel.setStyleName(watch.getStyleName("article", "actionsright"));
        updateRightActionsPanel(actionsPanel, article);
        return actionsPanel;
    }

    protected void updateRightActionsPanel(final FlowPanel actionsPanel, final FeedArticle article) {
        Image extLinkImage = new Image(watch.getSkinFile(Constants.IMAGE_EXT_LINK));
        extLinkImage.setTitle(watch.getTranslation("articlelist.open"));
        extLinkImage.addStyleName("clickable");
        extLinkImage.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                Window.open(article.getUrl(), "_blank", "");
                }
            });
        actionsPanel.add(extLinkImage);

        Image trashImage = new Image(watch.getSkinFile((article.getFlagStatus() == -1) 
            ? Constants.IMAGE_TRASH_ON : Constants.IMAGE_TRASH_OFF));

        // add the trash button only if the user has the right to edit
        if (watch.getConfig().getHasEditRight()) {
            Image trashLoadingImage = new Image(watch.getSkinFile(Constants.IMAGE_LOADING_SPINNER));
            trashImage.setTitle(watch.getTranslation((article.getFlagStatus() == -1) 
                                ? "article.trash.remove.caption" : "article.trash.add.caption"));
            final LoadingWidget trashLoadingWidget = new DefaultLoadingWidget(watch, trashImage, trashLoadingImage);
            trashLoadingWidget.addStyleName(watch.getStyleName("article-trash"));
            trashImage.addStyleName("clickable");
            trashImage.addClickListener(new ClickListener() {
                public void onClick(Widget widget) {
                    // trash/untrash article
                    int flagstatus = article.getFlagStatus();
                    final int newflagstatus;
                    if (flagstatus == -1) {
                        // the article is trashed, untrash it
                        newflagstatus = 0;
                    } else {
                        //the article isn't trashed, it can be trashed
                        newflagstatus = -1;
                    }
                    watch.getDataManager().updateArticleFlagStatus(article, newflagstatus, 
                        new LoadingAsyncCallback(trashLoadingWidget) {
                        	public void onFailure(Throwable caught) {
                        		super.onFailure(caught);
                        	}
                            public void onSuccess(Object result) {
                                super.onSuccess(result);
                                 article.setFlagStatus(newflagstatus);
                                 actionsPanel.clear();
                                 updateRightActionsPanel(actionsPanel, article);
                            }
                        });
                }
            });
            actionsPanel.add(trashLoadingWidget);
        } else {
            //otherwise add just the image
            actionsPanel.add(trashImage);
        }
    }
    
    protected Widget getStatusPanel(FeedArticle article, Widget commentsZonePanel, HTML commentsStatus) {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "status"));
        HorizontalPanel statusPanel = new HorizontalPanel();
        statusPanel.add(getCommentsStatusPanel(commentsZonePanel, commentsStatus, article));
        FlowPanel tagsContainer = new FlowPanel();
        HorizontalPanel tagsStatusPanel = new HorizontalPanel();
        prepareTagsPanel(article, tagsStatusPanel, tagsContainer);
        statusPanel.add(tagsStatusPanel);
        statusPanel.add(tagsContainer);
        p.add(statusPanel);
        return p;
    }
    
    protected void prepareTagsPanel(FeedArticle article, HorizontalPanel tagsStatusPanel, FlowPanel tagsContainer)
    {
        // Create and add the tags icon 
        Image tagsLogo = new Image(watch.getSkinFile(Constants.IMAGE_TAG)); 
        tagsStatusPanel.add(tagsLogo);
        // Create and add the tags status panel        
        HTML tagsStatus = new HTML (getTagsStatusTitle(article));
        tagsStatus.setStyleName(watch.getStyleName("article", "tags-status"));
        tagsStatusPanel.add(tagsStatus);
        // Create the tags add panel. invisible, to be set to visible / invisible on tags status click
        if (watch.getConfig().getHasEditRight()) {
            tagsStatus.addStyleName("clickable");
            final Widget tagsAdd = getTagsAddZonePanel(tagsContainer, article, tagsStatus);
            tagsStatusPanel.add(tagsAdd);
            tagsStatus.addClickListener(new ClickListener(){
                public void onClick(Widget sender) {
                    tagsAdd.setVisible(!tagsAdd.isVisible());
                    resizeWindow();
                }
            });
        }
        // Populate the tagsContainer panel
        refreshTagsContainer(tagsContainer, article, tagsStatus);
    }
    
    protected Widget getTagElementPanel(final FeedArticle article, final String tag, final FlowPanel tagsContainer,
        final HTML tagStatus)
    {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "tag"));
        HorizontalPanel tagPanel = new HorizontalPanel();
        Label tagName = new Label(tag);
        tagName.setStyleName(watch.getStyleName("article", "tag-name"));
        Image deleteAction = new Image(watch.getSkinFile((Constants.IMAGE_DELETE)));
        deleteAction.addMouseListener(new MouseListenerAdapter() {
            public void onMouseEnter(Widget sender)
            {
                ((Image)sender).setUrl(watch.getSkinFile(Constants.IMAGE_DELETE_ACTIVE));
            }
            public void onMouseLeave(Widget sender)
            {
                ((Image)sender).setUrl(watch.getSkinFile(Constants.IMAGE_DELETE));
            }
        });
        Image loadingImage = new Image(watch.getSkinFile(Constants.IMAGE_LOADING_SPINNER));
        deleteAction.setTitle(watch.getTranslation("article.tag.remove.caption"));
        final LoadingWidget deleteTagWidget = new DefaultLoadingWidget(watch, deleteAction, loadingImage);
        deleteTagWidget.addStyleName(watch.getStyleName("article-tag-remove"));
        deleteAction.addClickListener(new ClickListener() {
            public void onClick(Widget w)
            {
                watch.getDataManager().removeTag(article, tag, new LoadingAsyncCallback(deleteTagWidget){
                    public void onSuccess(Object o)
                    {
                        super.onSuccess(o);
                        // success - We need to refreshData the number of tags
                        // Remove the tag from the list
                        article.getTags().remove(tag);
                        tagStatus.setHTML(getTagsStatusTitle(article));
                        refreshTagsContainer(tagsContainer, article, tagStatus);
                        watch.refreshTagCloud();                        
                    }
                    public void onFailure(Throwable t)
                    {
                        super.onFailure(t);
                    }
                });
            }
        });
        tagPanel.add(tagName);
        tagPanel.add(deleteTagWidget);
        p.add(tagPanel);
        return p;
    }
    
    protected void refreshTagsContainer(FlowPanel tagsContainer, FeedArticle article, HTML tagStatus){
        tagsContainer.clear();
        if (article.getTags().size() != 0) {
            for (int i = 0; i < article.getTags().size(); i++) {
                tagsContainer.add(getTagElementPanel(article, (String) article.getTags().get(i), tagsContainer,
                    tagStatus));
            }
        }    
    }
    
    protected Widget getTagsAddZonePanel(FlowPanel tagsContainer, FeedArticle article, HTML tagStatus) {
        FlowPanel p = new FlowPanel();
        p.add(getTagsAddPanel(tagsContainer, p, article, tagStatus));
        // This panel is hidden by default
        p.setVisible(false);
        return p;
    }
    
    protected Widget getTagsAddPanel(final FlowPanel tagsContainer, final FlowPanel tagsAddPanel,
        final FeedArticle article, final HTML tagStatus)
    {
        FlowPanel p = new FlowPanel();
        p.setStyleName(watch.getStyleName("article", "tags-add"));
        HorizontalPanel addPanel = new HorizontalPanel();
        final TextBox addInput = new TextBox();
        WordListSuggestOracle tagListOracle = new WordListSuggestOracle(new TagListSuggestOracle(watch),
            Constants.PROPERTY_TAGS_SEPARATORS_EDIT, true);        
        SuggestBox addInputSuggestBox = new SuggestBox(tagListOracle, addInput);
        // because the addInput is a GWT suggest box, force autocomplete off so that browsers don't suggest too
        DOM.setElementProperty(addInput.getElement(), "autocomplete", "off");
        Button addAction =  new Button(watch.getTranslation("button.add"));
        addPanel.add(addInputSuggestBox);
        addPanel.add(addAction);
        Image loadingImage = new Image(watch.getSkinFile(Constants.IMAGE_LOADING_SPINNER));
        FlowPanel loadingPanel = new FlowPanel();
        loadingPanel.add(loadingImage);
        final LoadingWidget addTagLoadingWidget = new DefaultLoadingWidget(watch, addPanel, loadingPanel);
        addTagLoadingWidget.addStyleName(watch.getStyleName("add-tags"));
        addAction.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                // Get the new list of tags for the article
                final List newTags = FeedArticle.joinTagsLists(article.getTags(), 
                    FeedArticle.parseTagsString(addInput.getText().trim(), true), true);
                if (addInput.getText().trim().length() != 0) {
                    watch.getDataManager().updateTags(article, newTags, new LoadingAsyncCallback(addTagLoadingWidget) {
                            public void onFailure(Throwable caught) {
                                super.onFailure(caught);
                            }
        
                            public void onSuccess(Object result) {
                                super.onSuccess(result);
                                // success - We need to refreshData the number of tags
                                article.setTags(newTags);
                                tagStatus.setHTML(getTagsStatusTitle(article));
                                refreshTagsContainer(tagsContainer, article, tagStatus);
                                // Clear the tags add input to prepare it for next adding
                                addInput.setText("");
                                tagsAddPanel.setVisible(false);
                                watch.refreshTagCloud();
                            }
                        });
                }
            }
        });
        p.add(addTagLoadingWidget);
        return p;
    }
    
    protected String getTagsStatusTitle(FeedArticle article){
        String tagsTitle = "";
        if (article.getTags().size() == 0) {
            tagsTitle = watch.getTranslation("notags");
        } else {
            tagsTitle = watch.getTranslation("tags");        
        }
        return tagsTitle;
    }
    
    protected Widget getTagsStatusPanel(FlowPanel tagsContainer, final FeedArticle article) {
        final FlowPanel tagsPanel = new FlowPanel();
        HorizontalPanel tagsStatusPanel = new HorizontalPanel();
        Image tagsLogo = new Image(watch.getSkinFile(Constants.IMAGE_TAG)); 
        HTML tagsStatus = new HTML (getTagsStatusTitle(article));
        tagsStatus.setStyleName(watch.getStyleName("article", "tags-status"));
        tagsStatusPanel.add(tagsLogo);
        tagsStatusPanel.add(tagsStatus);
        final Widget tagsAdd = getTagsAddZonePanel(tagsContainer, article, tagsStatus);
        tagsStatusPanel.add(tagsAdd);
        tagsPanel.add(tagsStatusPanel);
        tagsStatus.addClickListener(new ClickListener(){
            public void onClick(Widget sender) {
                tagsAdd.setVisible(!tagsAdd.isVisible());
                resizeWindow();
            }
        });
        return tagsPanel;
    }
    
    protected Widget getCommentsStatusPanel(final Widget commentsZonePanel, final HTML commentsStatus,
        final FeedArticle article)
    {
        FlowPanel p = new FlowPanel();
        HorizontalPanel commentsStatusPanel = new HorizontalPanel();
        Image commentsLogo = new Image(watch.getSkinFile(Constants.IMAGE_COMMENT)); 
        commentsStatus.addClickListener(new ClickListener(){
            public void onClick(Widget sender) {
                // toggle the comments panel
                ArticleListWidget.this.showCommentsPanel(!commentsZonePanel.isVisible(),
                    (ComplexPanel) commentsZonePanel, article, commentsStatus);
                resizeWindow();
            }
        });
        commentsStatusPanel.add(commentsLogo);
        commentsStatusPanel.add(commentsStatus);
        p.add(commentsStatusPanel);
        return p;
    }
    
    protected Widget getCommentsZonePanel(FeedArticle article, HTML commentsStatus) {
        FlowPanel p = new FlowPanel();
        // the comments panel is hidden by default
        showCommentsPanel(false, p, article, commentsStatus);
        return p;
    }
    
    /**
     * Displays the comments zone panel and populates it if it's the first time it's printed.
     */
    protected void showCommentsPanel(boolean visible, ComplexPanel commentsPanel, FeedArticle article,
        HTML commentsStatus)
    {
        if (visible && (commentsPanel.getWidgetCount() == 0)) {
            commentsPanel.setStyleName(watch.getStyleName("article", "comments-add"));
            refreshCommentsContainer((FlowPanel)commentsPanel, article, commentsStatus);            
        }
        commentsPanel.setVisible(visible);
    }

    protected void refreshCommentsContainer(final FlowPanel commentPanel, final FeedArticle article,
        final HTML commentsStatus)
    {
        commentPanel.clear();
        int nbcomments = article.getCommentsNumber();
        String commentTitle = watch.getTranslation("nocomments");
        if (nbcomments != 0) {
            commentTitle = nbcomments + " " + watch.getTranslation("comments");
        }
        commentsStatus.setHTML(commentTitle);
        
        List comments = article.getComments();
        if ((comments!=null)&&(comments.size()>0)) {
            FlowPanel commentsZonePanel = new FlowPanel();
            for (int i = 0; i < comments.size(); i++) {
                FeedArticleComment comment = (FeedArticleComment) comments.get(i);
                commentsZonePanel.add(getCommentElementPanel(comment));
            }
            commentPanel.add(commentsZonePanel);
        }
        
        // add the comments panel only if the user has the right to comment
        if (watch.getConfig().getHasCommentRight()) {
            VerticalPanel g = new VerticalPanel();
            Image commentAddLoadingImage = new Image(watch.getSkinFile(Constants.IMAGE_LOADING_SPINNER));
            Panel commentAddLoadingPanel = new FlowPanel();
            commentAddLoadingPanel.add(commentAddLoadingImage);
            final LoadingWidget commentAddLoadingWidget = new DefaultLoadingWidget(watch, g, commentAddLoadingPanel);
            Label t = new Label(watch.getTranslation("commentadd.caption"));
            g.add(t);
            final TextArea commentTextArea = new TextArea();
            g.add(commentTextArea);
            HorizontalPanel buttonsContainer = new HorizontalPanel();
            Button cancelAction = new Button(watch.getTranslation("button.cancel"));
            cancelAction.addStyleName(watch.getStyleName("article-comment-cancel"));
            cancelAction.addClickListener(new ClickListener() {
                public void onClick(Widget sender) {
                    commentTextArea.setText("");
                }
            });
            Button saveAction = new Button(watch.getTranslation("button.add"));
            saveAction.addStyleName(watch.getStyleName("article-comment-add"));
            buttonsContainer.add(saveAction);
            buttonsContainer.add(cancelAction);
            g.add(buttonsContainer);
            commentPanel.add(commentAddLoadingWidget);
        
            saveAction.addClickListener(new ClickListener() {
                public void onClick(Widget widget) {
                    // Send the comment
                    final String comment = commentTextArea.getText().trim();
                    if (comment.length() > 0) {
                        watch.getDataManager().addComment(article, comment, 
                            new LoadingAsyncCallback(commentAddLoadingWidget) {
                                public void onFailure(Throwable caught) {
                                    // failure we show the exception
                                    super.onFailure(caught);
                                }
    
                                public void onSuccess(Object result) {
                                    super.onSuccess(result);
                                    // success - We need to refreshData the number of comments
                                    // we reread the article to make sure we get it right
                                    // TODO: it should be an ArticleLoadingWidget here, not a comment one, but the comment
                                    // looks a lot better
                                    watch.getDataManager().getArticle(article.getPageName(), 
                                            new LoadingAsyncCallback(commentAddLoadingWidget) {
                                                public void onFailure(Throwable caught) {
                                                    super.onFailure(caught);
                                                }
                
                                                public void onSuccess(Object result) {
                                                    super.onSuccess(result);
                                                    if (article.getCommentsNumber() != 0) {
                                                        commentsStatus.setHTML(article.getCommentsNumber() + " "
                                                            + watch.getTranslation("comments"));
                                                    }
                                                    // Refresh the comment panel
                                                    refreshCommentsContainer(commentPanel, (FeedArticle) result, 
                                                                         commentsStatus);
                                                    // We need to resize in case this brings up a scroll bar
                                                    resizeWindow();
                                                }
                                            });
                                }
                            });
                    }
                }
            });
        }
    }

    protected Widget getCommentElementPanel(FeedArticleComment comment) {
        FlowPanel pp = new FlowPanel();
        pp.setStyleName(watch.getStyleName("article", "comment"));
        HorizontalPanel p = new HorizontalPanel();
        FlowPanel detailsPanel = new FlowPanel();
        detailsPanel.setStyleName(watch.getStyleName("article", "comment-details"));
        HorizontalPanel authorPanel = new HorizontalPanel();
        Image authorLogo = new Image(watch.getSkinFile(Constants.IMAGE_USER));
        HTML author = new HTML(comment.getAuthor());
        authorPanel.add(authorLogo);
        authorPanel.add(author);
        author.setStyleName(watch.getStyleName("article", "comment-author"));
        detailsPanel.add(authorPanel);
        HTML date = new HTML(comment.getDate());
        date.setStyleName(watch.getStyleName("article", "comment-date"));
        detailsPanel.add(date);
        p.add(detailsPanel);
        HTML content = new HTML(comment.getContent());
        content.setStyleName(watch.getStyleName("article", "comment-content"));
        p.add(content);
        pp.add(p);
        return pp;
    }

    protected Widget getContentZonePanel(FeedArticle article) {
        FlowPanel p = new FlowPanel();
        // the content panel is invisible by default
        showContentPanel(false, p, article);
        return p;
    }
    
    /**
     * Set the content panel visible, also handling panel initialization. Initially, if the panel is not visible, 
     * it will be empty and will be initialized from the feed article upon first show, to optimize DOM manipulation 
     * on list fill. 
     */
    protected void showContentPanel(boolean visible, ComplexPanel contentPanel, FeedArticle article) {
        if (visible && (contentPanel.getWidgetCount() == 0)) {
            // populate this panel
            HTML content = new HTML(article.getContent());
            content.setStyleName(watch.getStyleName("article", "content"));
            contentPanel.add(content);         
        }
        contentPanel.setVisible(visible);
    }

    public void showArticle(FeedArticle article) {
        FlowPanel articlepanel = new FlowPanel();
        articlepanel.setStyleName(watch.getStyleName("article"));
        if (article.getReadStatus() == 1) {
            articlepanel.addStyleName(watch.getStyleName("article", "read"));
        } else {
            articlepanel.addStyleName(watch.getStyleName("article", "unread"));
        }
        Widget contentZonePanel = getContentZonePanel(article);
        articlepanel.add(getHeadlinePanel(article, articlepanel, contentZonePanel));
        panel.add(articlepanel);
    } 
    
    public void resizeWindow() {
        int windowWidth = watch.getUserInterface().getOffsetWidth();
        int feedTreeWidth = watch.getUserInterface().getFeedTreeWidth();
        int filterBarWidth = watch.getUserInterface().getFilterBarWidth();
        int newWidth = windowWidth - feedTreeWidth - filterBarWidth;
        // Handle floating point widths in FF3: decrease by one, to be sure it fits
        // TODO: remove this ugly and unreliable hack when we'll be able to get floating point widths
        if (Watch.getUserAgent().toLowerCase().indexOf("firefox/3.0") != -1) {
            newWidth = newWidth - 1;
        }
        if (newWidth < 0)
         newWidth = 0;
        setWidth(newWidth + "px");
    }
}
