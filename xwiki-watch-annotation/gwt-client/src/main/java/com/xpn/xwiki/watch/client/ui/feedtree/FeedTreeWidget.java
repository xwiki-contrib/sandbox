/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * 
 * This is free software;you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation;either version2.1of
 * the License,or(at your option)any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software;if not,write to the Free
 * Software Foundation,Inc.,51 Franklin St,Fifth Floor,Boston,MA
 * 02110-1301 USA,or see the FSF site:http://www.fsf.org.
 */
package com.xpn.xwiki.watch.client.ui.feedtree;

import com.xpn.xwiki.watch.client.data.Feed;
import com.xpn.xwiki.watch.client.data.FilterStatus;
import com.xpn.xwiki.watch.client.data.Group;
import com.xpn.xwiki.watch.client.ui.WatchWidget;
import com.xpn.xwiki.watch.client.ui.dialog.GroupDialog;
import com.xpn.xwiki.watch.client.ui.dialog.FeedDialog;
import com.xpn.xwiki.watch.client.ui.dialog.StandardFeedDialog;
import com.xpn.xwiki.watch.client.ui.dialog.FeedDeleteDialog;
import com.xpn.xwiki.watch.client.ui.utils.ItemObject;
import com.xpn.xwiki.watch.client.ui.utils.TextWidgetComposite;
import com.xpn.xwiki.watch.client.Watch;
import com.xpn.xwiki.watch.client.Constants;
import com.xpn.xwiki.gwt.api.client.app.XWikiAsyncCallback;
import com.xpn.xwiki.gwt.api.client.dialog.Dialog;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.*;

public class FeedTreeWidget extends WatchWidget
{
    private Tree groupTree = new Tree();

    public FeedTreeWidget()
    {
        super();
    }

    public String getName()
    {
        return "feedtree";
    }

    public FeedTreeWidget(Watch watch)
    {
        super(watch);
        setPanel(new FlowPanel());
        initWidget(panel);
        init();
    }

    public void init()
    {
        super.init();
        HTML titleHTML = new HTML(watch.getTranslation("feedtree.title"));
        titleHTML.setStyleName(watch.getStyleName("feedtree", "title"));
        titleHTML.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                watch.launchConfig("feeds");
            }
        });
        panel.add(titleHTML);

        Image configImage = new Image(watch.getSkinFile(Constants.IMAGE_CONFIG));
        configImage.setStyleName(watch.getStyleName("feedtree", "image"));
        configImage.setTitle(watch.getTranslation("config"));
        configImage.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                watch.launchConfig("feeds");
            }
        });
        panel.add(configImage);
        groupTree.setStyleName(watch.getStyleName("feedtree", "groups"));
        panel.add(groupTree);
    }

    public void refreshData()
    {
        boolean remakeRequired = remakeTreeRequired();
        boolean updateResult = true;
        if (!remakeRequired) {
            updateResult = updateFeedTree();
        }
        // if a remake was required or the update has failed, remake the tree
        if (remakeRequired || !updateResult) {
            makeFeedTree();
        }
    }

    /**
     * Check if tree needs to be rebuilt. This happens when a feed has been added or deleted or a feed has been moved
     * from a group to another, or when a feed / group has been renamed and the order of the nodes has changed due to
     * this rename. The comparison is made by comparing the current tree with the data from config. TODO: implement
     * correctly the notification mechanism to pass events to the UI so that we know exactly which type of update we are
     * doing and this function is not needed any more. see http://jira.xwiki.org/jira/browse/XWATCH-83
     * 
     * @return true if a rebuild of the whole feed tree is needed, false if widgets update is enough
     */
    private boolean remakeTreeRequired()
    {
        if (this.groupTree == null) {
            return true;
        }
        Map feedsbygroup = watch.getConfig().getFeedsByGroupList();
        Map groups = watch.getConfig().getGroups();
        // check groups and their order
        List groupNames = new ArrayList(feedsbygroup.keySet());
        if (groupNames.size() != this.groupTree.getItemCount()) {
            return true;
        }
        Collections.sort(groupNames, new GroupComparator(groups, "All"));
        for (int i = 0; i < groupNames.size(); i++) {
            TreeItem currentGroupItem = this.groupTree.getItem(i);
            GroupTreeItemObject groupUserObject = (GroupTreeItemObject) currentGroupItem.getUserObject();
            String treeGroupName = ((Group) groupUserObject.getData()).getName();
            if (!(((Group) groups.get(groupNames.get(i))).getName()).equals(treeGroupName)) {
                // the group on this position does not match
                return true;
            }
            Map groupFeeds = (Map) feedsbygroup.get(groupNames.get(i));
            // check feed names in this group, and their order
            List feedNames = new ArrayList(groupFeeds.keySet());
            if (feedNames.size() != currentGroupItem.getChildCount()) {
                return true;
            }
            Collections.sort(feedNames, new FeedComparator(groupFeeds, null));
            for (int j = 0; j < feedNames.size(); j++) {
                if (!feedNames.get(j).equals(
                    ((Feed) ((FeedTreeItemObject) currentGroupItem.getChild(j).getUserObject()).getData()).getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates the feed tree with the new data. Iterates through all the groups and feeds and resets their data with the
     * new data obtained from config, refreshing the displayed widget. It also resets selection of the tree widget
     * according to the tree item selection.
     * 
     * @return true if update has succeeded, false if inconsistencies have been found during the update process. call
     *         {@link FeedTreeWidget#makeFeedTree()} to create the tree from scratch if this function fails.
     */
    private boolean updateFeedTree()
    {
        // update the feed tree according to the new data
        Map feedsbygroup = watch.getConfig().getFeedsByGroupList();
        Map groups = watch.getConfig().getGroups();
        List groupKeys = new ArrayList(feedsbygroup.keySet());
        Collections.sort(groupKeys, new GroupComparator(groups, "All"));
        for (int i = 0; i < this.groupTree.getItemCount(); i++) {
            TreeItem currentGroupItem = this.groupTree.getItem(i);
            GroupTreeItemObject groupUserObject = (GroupTreeItemObject) currentGroupItem.getUserObject();
            String currentGroupKey = (String) groupKeys.get(i);
            Group foundGroup = (Group) groups.get(currentGroupKey);
            // special case for the All group
            if (foundGroup == null) {
                foundGroup = new Group();
                foundGroup.setName(currentGroupKey);
            }
            groupUserObject.setData(foundGroup, true);
            // set selected if needed
            groupUserObject.setSelected(currentGroupItem.isSelected());
            Map groupFeeds = (Map) feedsbygroup.get(currentGroupKey);
            // for each feed in the tree, find it in the data map and update it
            for (int j = 0; j < currentGroupItem.getChildCount(); j++) {
                TreeItem currentFeedItem = currentGroupItem.getChild(j);
                FeedTreeItemObject feedUserObject = (FeedTreeItemObject) currentFeedItem.getUserObject();
                Feed currentTreeFeed = (Feed) feedUserObject.getData();
                // get feed from the new data
                Feed foundFeed = (Feed) groupFeeds.get(currentTreeFeed.getName());
                if (foundFeed == null) {
                    // then there is an inconsistency of some sort, better remake tree
                    return false;
                } else {
                    // otherwise, reset the feed of this FeedTreeItemObject and refresh it
                    feedUserObject.setData(foundFeed, true);
                    // set widget's state to tree item state
                    feedUserObject.setSelected(currentFeedItem.isSelected());
                }
            }
        }
        return true;
    }

    /**
     * Creates the feed tree from scratch. It is called whenever a radical update of the tree has to be done, i.e.
     * {@link FeedTreeWidget#remakeTreeRequired()} returns true, or {@link FeedTreeWidget#updateFeedTree()} returns
     * false.
     */
    private void makeFeedTree()
    {
        // get the state of the tree items on first level -> the groups tree items
        HashMap itemsState = new HashMap();
        for (int i = 0; i < this.groupTree.getItemCount(); i++) {
            TreeItem currentTreeItem = this.groupTree.getItem(i);
            // get user object
            ItemObject userObj = (ItemObject) currentTreeItem.getUserObject();
            itemsState.put(userObj.getKey(), new Boolean(currentTreeItem.getState()));
        }
        // get the selected item to set it back when the tree is refreshed
        TreeItem selectedTreeItem = this.groupTree.getSelectedItem();
        String selectedItemKey = null;
        if (selectedTreeItem != null) {
            ItemObject selectedItemObject = (ItemObject) selectedTreeItem.getUserObject();
            if (selectedItemObject != null) {
                selectedItemKey = selectedItemObject.getKey();
            }
        }
        // clear all trees
        groupTree.clear();

        Map feedsbygroup = watch.getConfig().getFeedsByGroupList();
        Map groups = watch.getConfig().getGroups();

        List keys = new ArrayList(feedsbygroup.keySet());
        Collections.sort(keys, new GroupComparator(groups, "All"));
        Iterator groupit = keys.iterator();
        while (groupit.hasNext()) {
            final String groupname = (String) groupit.next();
            Group currentGroup = (Group) groups.get(groupname);
            if (currentGroup == null) {
                currentGroup = new Group();
                currentGroup.setName(groupname);
            }
            if ((groupname != null) && (!groupname.trim().equals(""))) {
                Map groupFeeds = (Map) feedsbygroup.get(groupname);
                TreeItem groupItemTree = new TreeItem();
                // set the TreeItem's object
                GroupTreeItemObject groupObj = new GroupTreeItemObject(groupname, currentGroup);
                groupItemTree.setUserObject(groupObj);
                // check if selected
                boolean selected = false;
                if (selectedItemKey != null && groupname.equals(selectedItemKey)) {
                    selected = true;
                    selectedTreeItem = groupItemTree;
                }
                groupItemTree.setWidget(groupObj.getWidget(selected));
                groupTree.addItem(groupItemTree);
                List feedList = new ArrayList(groupFeeds.keySet());
                Collections.sort(feedList, new FeedComparator(groupFeeds, null));
                Iterator feedgroupit = feedList.iterator();
                while (feedgroupit.hasNext()) {
                    String feedname = (String) feedgroupit.next();
                    Feed feed = (Feed) groupFeeds.get(feedname);
                    // set it's userObject to the name of the group + name of the feed since a
                    // feed can be part of multiple groups and we need to identify it uniquely.
                    String itemTreeKey = groupname + "." + feed.getPageName();
                    ItemObject feedObj = new FeedTreeItemObject(itemTreeKey, feed);
                    TreeItem feedItem = new TreeItem();
                    feedItem.setUserObject(feedObj);
                    selected = false;
                    if (selectedItemKey != null && itemTreeKey.equals(selectedItemKey)) {
                        selected = true;
                        selectedTreeItem = feedItem;
                    }
                    feedItem.setWidget(feedObj.getWidget(selected));
                    groupItemTree.addItem(feedItem);
                }
                // expand it if necessary
                Boolean state = (Boolean) itemsState.get(groupname);
                if (state != null) {
                    groupItemTree.setState(state.booleanValue());
                }
                groupTree.addItem(groupItemTree);
            }
        }
        // set the selected tree item
        this.groupTree.setSelectedItem(selectedTreeItem);
    }

    public void resetSelections()
    {
        // Check the validity of the current selection with respect to the filter
        FilterStatus fstatus = watch.getFilterStatus();
        Feed filterFeed = fstatus.getFeed();
        String filterGroupPageName = fstatus.getGroup();
        // Get currently selected item
        TreeItem selectedTreeItem = this.groupTree.getSelectedItem();
        // Get user object to check the correspondence
        TreeItemObject selectedTreeItemObject = null;
        boolean isValidTreeSelection = true;
        if (selectedTreeItem != null) {
            selectedTreeItemObject = (TreeItemObject) selectedTreeItem.getUserObject();
            if (selectedTreeItemObject instanceof GroupTreeItemObject) {
                if (filterGroupPageName != null) {
                    if (!((Group) selectedTreeItemObject.getData()).getPageName().equals(filterGroupPageName)) {
                        // The filter group is not the tree group
                        isValidTreeSelection = false;
                    }
                } else {
                    // Group is selected in the tree but not in the filter
                    isValidTreeSelection = false;
                }
            }
            if (selectedTreeItemObject instanceof FeedTreeItemObject) {
                if (filterFeed != null) {
                    if (!((Feed) selectedTreeItemObject.getData()).getPageName().equals(filterFeed.getPageName())) {
                        // The filter feed is not the tree selected feed
                        isValidTreeSelection = false;
                    }
                } else {
                    // Feed is selected in the tree but not in the filter
                    isValidTreeSelection = false;
                }
            }
        } else {
            if (filterFeed != null || filterGroupPageName != null) {
                isValidTreeSelection = false;
            }
        }

        // Now change selection if needed
        if (!isValidTreeSelection) {
            TreeItem newSelectedTreeItem = null;
            // Find the new SelectedItem
            if (filterGroupPageName != null) {
                // Iterate groups level and find the current group
                for (int i = 0; i < this.groupTree.getItemCount(); i++) {
                    TreeItem currentItem = this.groupTree.getItem(i);
                    Group currentGroup = (Group) (((GroupTreeItemObject) currentItem.getUserObject()).getData());
                    if (currentGroup.getPageName().equals(filterGroupPageName)) {
                        // Found the item
                        newSelectedTreeItem = currentItem;
                        break;
                    }
                }
            } else if (filterFeed != null) {
                // Iterate through the All group and find the current feed
                TreeItem allTreeGroup = null;
                if (this.groupTree.getItemCount() > 0) {
                    allTreeGroup = this.groupTree.getItem(0);
                }
                if (allTreeGroup != null) {
                    // Iterate in this group and find the selected feed
                    for (int i = 0; i < allTreeGroup.getChildCount(); i++) {
                        TreeItem currentItem = allTreeGroup.getChild(i);
                        Feed currentFeed = (Feed) (((FeedTreeItemObject) currentItem.getUserObject()).getData());
                        if (currentFeed.getPageName().equals(filterFeed.getPageName())) {
                            // Found the item
                            newSelectedTreeItem = currentItem;
                            break;
                        }
                    }
                }
            }
            // Unselect old selected widget
            if (selectedTreeItemObject != null) {
                selectedTreeItemObject.setSelected(false);
            }
            // Select the new tree item
            this.groupTree.setSelectedItem(newSelectedTreeItem, false);
            // Also get the widget and select it
            if (newSelectedTreeItem != null) {
                ((TreeItemObject) newSelectedTreeItem.getUserObject()).setSelected(true);
            }
        }
    }

    public void resizeWindow()
    {
        // Watch.setMaxHeight(panel);
    }

    public abstract class TreeItemObject extends ItemObject
    {
        // TODO: push this to the ItemObject
        protected TextWidgetComposite widget;

        public TreeItemObject(String key, Object data)
        {
            super(key, data);
        }

        // TODO: push this to the ItemObject
        public Object getData()
        {
            return this.data;
        }

        public void setData(Object data, boolean refreshWidget)
        {
            this.data = data;
            if (refreshWidget) {
                ((HTML) ((TextWidgetComposite) this.widget).getMainWidget()).setHTML(getTitle());
            }
        }

        public abstract String getTitle();
        
        public abstract String getTooltip();

        public abstract void setSelected(boolean selected);
    }

    public class GroupTreeItemObject extends TreeItemObject
    {
        public GroupTreeItemObject(String key, Object data)
        {
            super(key, data);
        }

        public String getTitle()
        {
            return ((Group) getData()).getName();
        }
        
        public String getTooltip()
        {
            return ((Group) getData()).getName();
        }

        public Widget getWidget(boolean selected)
        {
            if (this.widget == null) {
                HTML title = new HTML(getTitle(), true);
                title.setTitle(getTooltip());
                title.addStyleName(watch.getStyleName("feedtree", "link"));
                title.addStyleName(watch.getStyleName("feedtree", "group"));
                title.addClickListener(new ClickListener() {
                    public void onClick(Widget widget) {
                        Group group = (Group) getData();
                        watch.refreshOnGroupChange(group.getPageName().trim().equals("") ? group.getName() : group
                            .getPageName());
                    }
                });
                this.widget = new TextWidgetComposite(title);
            }
            this.setSelected(selected);
            return this.widget;
        }

        public void setSelected(boolean selected)
        {
            if (this.widget == null) {
                // nothing to select
                return;
            }

            // clear all the widgets in this composite
            for (Iterator wIt = this.widget.getWidgets().iterator(); wIt.hasNext();) {
                TextWidgetComposite w = (TextWidgetComposite) wIt.next();
                this.widget.remove(w);
            }

            Group group = (Group) getData();
            // if group is All group or it is a non-existent group, we shouldn't be able to edit it
            if (selected && (!group.getName().equals(watch.getTranslation("all"))) && !group.getPageName().equals("")) {
                // create a composite with link as main widget and some actions
                // Create and add the delete and edit actions in reverse order because they will be floated to the right
                
                // Create the delete label only if the user has the right to delete
                if (watch.getConfig().getHasDeleteRight()) {
                    Label deleteLabel = new Label(watch.getTranslation("feedtree.delete"));
                    deleteLabel.addClickListener(new ClickListener() {
                        public void onClick(Widget widget) {
                            String confirmString = watch.getTranslation("removegroup.confirm", 
                                new String[] {((Group) getData()).getName()});
                            boolean confirm = Window.confirm(confirmString);
                            if (confirm) {
                                watch.getDataManager().removeGroup((Group) getData(), new XWikiAsyncCallback(watch) {
                                    public void onFailure(Throwable caught) {
                                        super.onFailure(caught);
                                    }
    
                                    public void onSuccess(Object result) {
                                        super.onSuccess(result);
                                        // We need to refreshData the tree
                                        watch.refreshOnNewGroup();
                                        watch.refreshOnNewKeyword();
                                    }
                                });
                            } else {
                                // nothing
                            }
                        }
                    });
                    TextWidgetComposite deleteComposite = new TextWidgetComposite(deleteLabel);
                    deleteComposite.setStyleName(watch.getStyleName("feedtree", "groupaction") + " "
                        + watch.getStyleName("feedtree", "deletegroup"));
                    widget.add(deleteComposite);
                }
                
                // Create and add the edit link only if the user has the right to edit
                if (watch.getConfig().getHasEditRight()) {
                    Label editLabel = new Label(watch.getTranslation("feedtree.edit"));
                    editLabel.addClickListener(new ClickListener() {
                        public void onClick(Widget widget) {
                            GroupDialog gDialog =
                                new GroupDialog(watch, "addgroup", Dialog.BUTTON_CANCEL | Dialog.BUTTON_NEXT,
                                    (Group) getData());
                            gDialog.setAsyncCallback(new AsyncCallback() {
                                public void onFailure(Throwable throwable) {
                                    // nothing
                                }
    
                                public void onSuccess(Object object) {
                                    Group newGroup = (Group) object;
                                    watch.getDataManager().updateGroup(newGroup, new XWikiAsyncCallback(watch) {
                                        public void onFailure(Throwable caught) {
                                            super.onFailure(caught);
                                        }
    
                                        public void onSuccess(Object result) {
                                            super.onSuccess(result);
                                            // We need to refreshData the tree
                                            watch.refreshOnNewGroup();
                                            watch.refreshOnNewKeyword();
                                        }
                                    });
                                }
                            });
                            gDialog.show();
                        }
                    });
                    TextWidgetComposite editComposite = new TextWidgetComposite(editLabel);
                    editComposite.setStyleName(watch.getStyleName("feedtree", "groupaction") + " "
                        + watch.getStyleName("feedtree", "editgroup"));
                    widget.add(editComposite);
                }
                if (widget.getWidgets().size() > 0) {
                    // add a fake clear floats widget
                    TextWidgetComposite fakeClearFloatsWidget = new TextWidgetComposite(null);
                    fakeClearFloatsWidget.setStyleName("clearfloats");
                    widget.add(fakeClearFloatsWidget);
                }
            }
        }
    }

    public class FeedTreeItemObject extends TreeItemObject
    {
        public FeedTreeItemObject(String key, Object data)
        {
            super(key, data);
        }

        public String getTitle()
        {
            Feed feed = (Feed) getData();
            String feedTitle =
                ((feed.getTitle().trim().length() > 0) ? feed.getTitle() : feed.getName()) + " (" + feed.getNb() + ")";
            String imgurl = watch.getFavIcon(feed);
            if (imgurl != null) {
                feedTitle = "<img src=\"" + imgurl + "\" class=\"" + watch.getStyleName("feedtree", "logo-icon")
                        + "\" alt=\"\" />" + feedTitle;
            }
            return feedTitle;
        }
        
        public String getTooltip()
        {
            Feed feed = (Feed)getData();
            return (feed.getTitle().trim().length() > 0) ? feed.getTitle() : feed.getName();
        }

        public Widget getWidget(boolean selected)
        {
            if (widget == null) {
                // create the widget
                HTML title = new HTML(getTitle(), true);
                title.setTitle(getTooltip());
                title.addClickListener(new ClickListener() {
                    public void onClick(Widget widget) {
                        watch.refreshOnFeedChange((Feed) getData());
                    }
                });
                title.addStyleName(watch.getStyleName("feedtree", "link"));
                title.addStyleName(watch.getStyleName("feedtree", "feed"));
                widget = new TextWidgetComposite(title);
            }
            // set selected or unselected
            this.setSelected(selected);
            return widget;
        }

        public void setSelected(boolean selected)
        {
            if (this.widget == null) {
                // nothing to select, no widget
                return;
            }
            // clear all the widgets in this composite
            for (Iterator wIt = this.widget.getWidgets().iterator(); wIt.hasNext();) {
                TextWidgetComposite w = (TextWidgetComposite) wIt.next();
                this.widget.remove(w);
            }

            // if selected, generate the two action links. Actions are created and added in reverse order 
            // because they will be floated to the right
            if (selected) {
                // Add the delete action only if the user has the delete right
                if (watch.getConfig().getHasDeleteRight()) {
                    Label deleteLabel = new Label(watch.getTranslation("feedtree.delete"));
                    deleteLabel.addClickListener(new ClickListener() {
                        public void onClick(Widget widget) {
                            // use a delete feed dialog
                            FeedDeleteDialog deleteDialog = new FeedDeleteDialog(watch, "removefeed", (Feed) getData());
                            deleteDialog.show();
                        }
                    });
                    TextWidgetComposite deleteComposite = new TextWidgetComposite(deleteLabel);
                    deleteComposite.setStyleName(watch.getStyleName("feedtree", "feedaction") + " "
                        + watch.getStyleName("feedtree", "deletefeed"));
                    widget.add(deleteComposite);
                }
                
                // Add the edit action only if the user has the right to edit
                if (watch.getConfig().getHasEditRight()) {
                    Label editLabel = new Label(watch.getTranslation("feedtree.edit"));
                    editLabel.addClickListener(new ClickListener() {
                        public void onClick(Widget widget) {
                            FeedDialog feedDialog =
                                new StandardFeedDialog(watch, "standard", Dialog.BUTTON_CANCEL | Dialog.BUTTON_NEXT,
                                    (Feed) getData());
                            feedDialog.setAsyncCallback(new AsyncCallback() {
                                public void onFailure(Throwable throwable) {
                                    // nothing
                                }
    
                                public void onSuccess(Object object) {
                                    Feed newfeed = (Feed) object;
                                    watch.getDataManager().updateFeed(newfeed, new XWikiAsyncCallback(watch) {
                                        public void onFailure(Throwable caught) {
                                            super.onFailure(caught);
                                        }
    
                                        public void onSuccess(Object result) {
                                            super.onSuccess(result);
                                            watch.refreshOnUpdateFeed();
                                        }
                                    });
                                }
                            });
                            feedDialog.show();
                        }
                    });
                    TextWidgetComposite editComposite = new TextWidgetComposite(editLabel);
                    editComposite.setStyleName(watch.getStyleName("feedtree", "feedaction") + " "
                        + watch.getStyleName("feedtree", "editfeed"));
                    widget.add(editComposite);
                }
                if (widget.getWidgets().size() > 0) {
                    // add a fake clear floats widget
                    TextWidgetComposite fakeClearFloatsWidget = new TextWidgetComposite(null);
                    fakeClearFloatsWidget.setStyleName("clearfloats");
                    widget.add(fakeClearFloatsWidget);
                }
            }
        }
    }

    /**
     * Compares two group keys based on the group information from the groups map, specifically, by the names of the
     * groups referred by the group keys; it also allows a minimum element to be set, to be always returned as smaller.
     * The comparison is always case insensitive.
     * To be used for group sorting in tree.
     */
    public class GroupComparator implements Comparator
    {
        private Map groups;

        private Object first;

        public GroupComparator(Map groups, Object first)
        {
            this.groups = groups;
            this.first = first;
        }

        public int compare(Object o1, Object o2)
        {
            if (this.first != null) {
                // we have first element, must test o1 and o2 against it
                if (this.first.equals(o1)) {
                    if (this.first.equals(o2)) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (this.first.equals(o2)) {
                    return 1;
                }
            }
            Group gr1 = (Group) groups.get(o1);
            Group gr2 = (Group) groups.get(o2);
            String gCompareKey1 = (gr1 == null) ? (String) o1 : gr1.getName();
            String gCompareKey2 = (gr2 == null) ? (String) o2 : gr2.getName();

            if (gCompareKey1 == null) {
                if (gCompareKey2 == null) {
                    return 0;
                } else {
                    // nulls at the end
                    return 1;
                }
            } else {
                return gCompareKey1.toLowerCase().compareTo(gCompareKey2.toLowerCase());
            }
        }
    }

    /**
     * Compares two feed names based on the feed information from the feeds map, specifically, by the titles of the
     * feeds referred by the feed names; it also allows a minimum element to be set, to be always returned as smaller.
     * The comparison is always case insensitive.
     * To be used for feed sorting alphabetically by title in tree.
     */
    public class FeedComparator implements Comparator {
        private Map feeds;

        private Object first;

        public FeedComparator(Map feeds, Object first)
        {
            this.feeds = feeds;
            this.first = first;
        }

        public int compare(Object o1, Object o2)
        {
            if (this.first != null) {
                // we have first element, must test o1 and o2 against it
                if (this.first.equals(o1)) {
                    if (this.first.equals(o2)) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (this.first.equals(o2)) {
                    return 1;
                }
            }
            Feed f1 = (Feed) feeds.get(o1);
            Feed f2 = (Feed) feeds.get(o2);
            String fCompareKey1 =
                (f1 == null) ? (String) o1 : ((f1.getTitle().trim().length() > 0) ? f1.getTitle() : f1.getName());
            String fCompareKey2 =
                (f2 == null) ? (String) o2 : ((f2.getTitle().trim().length() > 0) ? f2.getTitle() : f2.getName());

            if (fCompareKey1 == null) {
                if (fCompareKey2 == null) {
                    return 0;
                } else {
                    // nulls at the end
                    return 1;
                }
            } else {
                return fCompareKey1.toLowerCase().compareTo(fCompareKey2.toLowerCase());
            }
        }        
    }
}
