function XWikiFox() {
	this._window = null;
	this.isWindowOpen = false;
	this.prefManager = Components.classes['@mozilla.org/preferences-service;1']
			.getService(Components.interfaces.nsIPrefService).getBranch(
					"extensions.xwikifox.");
	this.lastContainer = null;
	this.openTreeUrlBool = true;

}

XWikiFox.prototype = {

	load : function() {
		this._window = document.getElementById("xwikifox-main-window");
		this._window.width = 350;
		this._window.height = 400;
		this.loadWikiData();
	},

	loadWikiData : function() {
		this.getWikiUrl();
		this.getWikiTitle();
		this.getSpaces(true);
		this.unselectAllTabs();
		var spacesTab = document.getElementById("spaces-tab");
		spacesTab.setAttribute("selected", true);
		this.getTags(false);
		this.getRss('recent', false);
		this.getRss('watchlist', false);
	},

	unselectAllTabs : function() {
		var spacesTab = document.getElementById("spaces-tab");
		var tagsTab = document.getElementById("tags-tab");
		var recentTab = document.getElementById("recent-tab");
		var watchlistTab = document.getElementById("watchlist-tab");
		var searchTab = document.getElementById("search-tab");
		spacesTab.setAttribute("selected", false);
		tagsTab.setAttribute("selected", false);
		recentTab.setAttribute("selected", false);
		watchlistTab.setAttribute("selected", false);
		searchTab.setAttribute("selected", false);
	},

	openUrl : function(url) {
		gBrowser.selectedTab = gBrowser.loadOneTab(url);
	},

	getWikiUrl : function() {
		var mainUrl = this.prefManager.getCharPref("currentMainWiki");
		var wikiUrl = mainUrl.replace("xwiki/bin/view/", "");
		var wikiUrlLabel = document.getElementById("xwikifox-wikiurl");
		wikiUrlLabel.setAttribute('value', wikiUrl);
		wikiUrlLabel.setAttribute('href', wikiUrl);
	},

	getWikiTitle : function() {
		var restUrl = this.prefManager.getCharPref("currentRestWiki");
		const
		WEBHOME_URI = restUrl + "spaces/Main/pages/WebHome";
		var request = new XMLHttpRequest();
		var title;
		request.open("GET", WEBHOME_URI, true);
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				var itemList = request.responseXML.getElementsByTagName("page");
				for ( var i = 0; i < itemList.length; i++) {
					var nodeList = itemList.item(i).childNodes;
					for ( var j = 0; j < nodeList.length; j++) {
						var node = nodeList.item(j);
						if (node.nodeName == "title") {
							title = node.firstChild.nodeValue;
						}

					}
					var wikiTitleLabel = document
							.getElementById("xwikifox-wikititle");
					wikiTitleLabel.setAttribute('value', title);

				}
			}
		}
		request.send(null);
	},

	openTreeUrl : function(event) {
		// var tree = event.target;
		if (this.openTreeUrlBool) {
			var tree = document.getElementById("xwikifox-spaces-tree");
			var treeitem = tree.view.getItemAtIndex(tree.currentIndex);
			var treecell = treeitem.firstChild.firstChild;
			this.openUrl(treecell.getAttribute("href"));
		} else {
			this.openTreeUrlBool = true;
		}
	},

	openWindow : function(e) {
		if (e.button == 0) {
			if (this.isWindowOpen) {
				this.closeWindow();
			} else {
				this.isWindowOpen = true;
				var _statusbar = document.getElementById("xwikifox-statusbar");
				this._window.openPopup(_statusbar, "before_start");
			}
		}
	},

	closeWindow : function() {
		this._window.hidePopup();
		this.isWindowOpen = false;
	},

	openPrefWindow : function() {
		window.openDialog("chrome://xwikifox/content/preference.xul", "_blank",
				"chrome,resizable=no,dependent=yes");
	},

	getSpaces : function(display) {
		var container = document.getElementById("xwikifox-scrollbox-spaces");
		var treechildren = document
				.getElementById("xwikifox-spaces-tree-children");
		var tree = document.getElementById("xwikifox-spaces-tree");
		if (display == true) {
			try {
				this.lastContainer.style.display = "none";
				this.lastContainer = container;
			} catch (e) {
				// lastContainer is null
			}
			container.style.display = "inherit";
		}
		const
		SPACE_URI = this.prefManager.getCharPref("currentRestWiki") + "spaces";
		var request = new XMLHttpRequest();
		var name;
		var url;
		var thisObj = this;
		request.open("GET", SPACE_URI, true);
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				if (request.status == 200) {
					var itemList = request.responseXML
							.getElementsByTagName("space");
					thisObj.removeXmlChilds(treechildren);
					for ( var i = 0; i < itemList.length; i++) {
						var nodeList = itemList.item(i).childNodes;
						for ( var j = 0; j < nodeList.length; j++) {
							var node = nodeList.item(j);
							if (node.nodeName == "name") {
								name = node.firstChild.nodeValue;
							} else if (node.nodeName == "xwikiAbsoluteUrl") {
								url = node.firstChild.nodeValue;
							}

						}
						/*
						 * var a = thisObj.createLink(name, url);
						 * container.appendChild(a);
						 */
						var treeitem = thisObj.createItem(name, url,
								"xwikifox-space-children-");
						treeitem.addEventListener("DOMAttrModified",
								xwikiFox.addDocuments, false);
						tree.lastChild.appendChild(treeitem);

					}
				} else {
					alert("Sorry, we couldn't retrieve any data.");
				}
			}
		}
		this.lastContainer = container;
		request.send(null);
	},

	getTags : function(display) {
		const
		TAG_URI = this.prefManager.getCharPref("currentRestWiki") + "tags";
		var container = document.getElementById("xwikifox-scrollbox-tags");
		if (display == true) {
			try {
				this.lastContainer.style.display = "none";
				this.lastContainer = container;
			} catch (e) {
				// lastContainer is null
			}
			container.style.display = "inline";
		}
		var request = new XMLHttpRequest();
		var tagCount = [];
		var maxPopularity = 0;
		var name;
		var currentMainWiki = this.prefManager.getCharPref("currentMainWiki");
		var thisObj = this;
		request.open("GET", TAG_URI, true);
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				var itemList = request.responseXML.getElementsByTagName("tag");

				for ( var i = 0; i < itemList.length; i++) {
					var nodeList = itemList.item(i).childNodes;
					for ( var j = 0; j < nodeList.length; j++) {
						var node = nodeList.item(j);
						if (node.nodeName == "link") {
							tagRestUrl = node.getAttribute("href");
							;
						}
						tagCount[i] = thisObj.getTagPopularity(tagRestUrl);
						if (tagCount[i] > maxPopularity) {
							maxPopularity = tagCount[i];
						}
					}
				}
				thisObj.removeXmlChilds(container);
				for ( var i = 0; i < itemList.length; i++) {
					name = itemList.item(i).getAttribute("name");
					var url = currentMainWiki;
					url = url + "Main/Tags?do=viewTag&tag=";
					url = url + name;
					var popularity = Math.floor(tagCount[i] / maxPopularity * 3
							+ 1);
					// getting the number of documents tagged with the current
					// tag
					var cssClass = "xwikifox-popularity-" + popularity;
					var a = thisObj.createTag(name, url, cssClass);
					// var a = thisObj.createLinkNew(name, url);
					container.appendChild(a);
				}
			}
		}
		request.send(null);
	},

	getTagPopularity : function(tagRestUrl) {
		var request = new XMLHttpRequest();
		var thisObj = this;
		request.open("GET", tagRestUrl, false);
		request.send(null);
		var itemList = request.responseXML.getElementsByTagName("pageSummary");
		return itemList.length;

	},

	showSearch : function() {
		var container = document.getElementById("xwikifox-scrollbox-search");
		try {
			this.lastContainer.style.display = "none";
			this.lastContainer = container;
		} catch (e) {
			// lastContainer is null
		}
		container.style.display = "inherit";
	},

	searchNew : function() {
		var searchInput = document.getElementById('xwikifox-search-input');
		var restUrl = this.prefManager.getCharPref("currentRestWiki");
		var treechildren = document
				.getElementById("xwikifox-search-tree-children")
		var childrenId;
		var tree = document.getElementById("xwikifox-search-tree");
		const
		SEARCH_URI = restUrl + "search?q=" + searchInput.value;
		var request = new XMLHttpRequest();
		var thisObj = this;
		var spaceName, oldSpaceName = "", pageName, url;
		request.open("GET", SEARCH_URI, true);
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				if (request.status == 200) {
					// tree.style.display = "inline";
					var itemList = request.responseXML
							.getElementsByTagName("searchResult");
					thisObj.removeXmlChilds(treechildren);
					for ( var i = 0; i < itemList.length; i++) {
						var nodeList = itemList.item(i).childNodes;
						for ( var j = 0; j < nodeList.length; j++) {
							var node = nodeList.item(j);
							if (node.nodeName == "space") {
								spaceName = node.firstChild.nodeValue;
							} else if (node.nodeName == "link") {
								urlPage = node.getAttribute("href");
							} else if (node.nodeName == "pageName") {
								pageName = node.firstChild.nodeValue;
							}
						}
						if (oldSpaceName != spaceName) {
							var treeitem = thisObj.createItem(spaceName, "",
									"xwikifox-search-children-");
							tree.lastChild.appendChild(treeitem);
							oldSpaceName = spaceName;
						}
						childrenId = "xwikifox-search-children-" + spaceName;
						var treechildrenSpace = document
								.getElementById(childrenId);
						var treeitem = document.createElement("treeitem");
						var treerow = document.createElement("treerow");
						var treecell = document.createElement("treecell");
						treecell.setAttribute("label", pageName);
						treecell.setAttribute("href", urlPage);
						treecell.setAttribute("src",
								"chrome://xwikifox/content/images/page.png")
						treerow.appendChild(treecell);
						treeitem.appendChild(treerow);
						treechildrenSpace.appendChild(treeitem);

					}
				}
			}
		}
		request.send(null);

	},

	/*
	 * workaround for tag cloud getTagsRss : function(feedUrl) { var httpRequest =
	 * null; var docNr; function infoReceived() { var data =
	 * httpRequest.responseText;
	 * 
	 * var ioService = Components.classes['@mozilla.org/network/io-service;1']
	 * .getService(Components.interfaces.nsIIOService); var uri =
	 * ioService.newURI(feedUrl, null, null);
	 * 
	 * if (data.length) { var parser =
	 * Components.classes["@mozilla.org/feed-processor;1"]
	 * .createInstance(Components.interfaces.nsIFeedProcessor); try {
	 * parser.listener = { handleResult: function(result) { var feed =
	 * result.doc; feed.QueryInterface(Components.interfaces.nsIFeed); var
	 * itemArray = feed.items; docNr = itemArray.length; //alert("first" +
	 * docNr) } }; //alert("second" + docNr) parser.parseFromString(data, uri); }
	 * catch (e) { alert("Error parsing feed."); } } }
	 * 
	 * httpRequest = new XMLHttpRequest();
	 * 
	 * httpRequest.open("GET", feedUrl, true); try { httpRequest.onload =
	 * infoReceived; httpRequest.send(null); //alert("third" + docNr) } catch
	 * (e) { alert(e); } //return docNr; },
	 */

	getRss : function(recent, display) {
		const
		USERNAME = this.prefManager.getCharPref("currentUsername");
		var httpRequest = null;
		var currentMainWiki = this.prefManager.getCharPref("currentMainWiki");
		var container, numberFeeds = this.prefManager.getIntPref("numberfeeds");
		if (recent == "recent") {
			var feedUrl = currentMainWiki + "Main/WebRss?xpage=plain";
			container = document.getElementById("xwikifox-scrollbox-recent");
		} else {
			var feedUrl = currentMainWiki + "XWiki/" + USERNAME
					+ "?xpage=watchlistrss";
			container = document.getElementById("xwikifox-scrollbox-watchlist");
		}
		if (display == true) {
			try {
				this.lastContainer.style.display = "none";
				this.lastContainer = container;
			} catch (e) {
				alert("e")
				// lastContainer is null
			}
			container.style.display = "inline";
		}
		function infoReceived() {
			var data = httpRequest.responseText;

			var ioService = Components.classes['@mozilla.org/network/io-service;1']
					.getService(Components.interfaces.nsIIOService);
			var uri = ioService.newURI(feedUrl, null, null);

			if (data.length) {
				var parser = Components.classes["@mozilla.org/feed-processor;1"]
						.createInstance(Components.interfaces.nsIFeedProcessor);
				var listener = new FeedTestResultListener(container,
						numberFeeds);
				try {
					parser.listener = listener;
					parser.parseFromString(data, uri);
				} catch (e) {
					alert("Error parsing feed.");
				}
			}
		}

		httpRequest = new XMLHttpRequest();

		httpRequest.open("GET", feedUrl, true);
		try {
			httpRequest.onload = infoReceived;
			httpRequest.send(null);
		} catch (e) {
			alert(e);
		}

	},

	getDocuments : function(space, treechildren) {
		var DOCUMENTS_URI = this.prefManager.getCharPref("currentRestWiki")
				+ "spaces/" + space + "/pages";
		var request = new XMLHttpRequest();
		var name;
		var url;
		var thisObj = this;
		request.open("GET", DOCUMENTS_URI, true);
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				window.setCursor("auto");
				var itemList = request.responseXML
						.getElementsByTagName("pageSummary");
				thisObj.removeXmlChilds(treechildren);
				for ( var i = 0; i < itemList.length; i++) {
					var nodeList = itemList.item(i).childNodes;
					for ( var j = 0; j < nodeList.length; j++) {
						var node = nodeList.item(j);
						if (node.nodeName == "name") {
							name = node.firstChild.nodeValue;
						} else if (node.nodeName == "xwikiAbsoluteUrl") {
							url = node.firstChild.nodeValue;
						}

					}
					var treeitem = document.createElement("treeitem");
					var treerow = document.createElement("treerow");
					var treecell = document.createElement("treecell");
					treecell.setAttribute("label", name);
					// treecell.allowEvents = "true";
					treecell.setAttribute("href", url);
					treecell.setAttribute("src",
							"chrome://xwikifox/content/images/page.png")
					// treecell.setAttribute("onclick","alert(1)");
					treerow.appendChild(treecell);
					treeitem.appendChild(treerow);
					// treeitem.addEventListener("click",
					// xwikiFox.openDocumentUrl, false);
					treechildren.appendChild(treeitem);

				}
			}
		}
		request.send(null);
	},

	showWikisMenu : function(menu) {
		var currentWiki = this.prefManager.getCharPref("currentMainWiki");
		var wikis = this.getWikis();
		this.removeXmlChilds(menu);
		for ( var i = 0; i < wikis.length; i++) {
			var wiki = wikis[i].split(";");
			var item = document.createElement("menuitem");
			item.setAttribute("label", wiki[1]);
			item.setAttribute("type", "radio");
			item.setAttribute("oncommand", "xwikiFox.changeWiki(this.label)");
			if (wiki[1] == currentWiki) {
				item.setAttribute("checked", true);
			}
			menu.appendChild(item);
		}
	},

	changeWiki : function(newCurrentWiki) {
		var wikis = this.getWikis();
		for ( var i = 0; i < wikis.length; i++) {
			var wiki = wikis[i].split(";");
			if (wiki[1] == newCurrentWiki) {
				this.prefManager.setCharPref("currentMainWiki", wiki[1]);
				this.prefManager.setCharPref("currentRestWiki", wiki[0]);
				try {
					this.prefManager.setCharPref("currentUsername", wiki[2]);
				} catch (e) {
					this.prefManager.setCharPref("currentUsername", "");
				}
			}
		}
		this.loadWikiData();
	},

	createTag : function(name, url, cssClass) {
		var link = document.createElement("label");
		link.setAttribute("class", cssClass + " xwikifox-link");
		link.setAttribute("href", url);
		link.addEventListener("click", function(e) {
			xwikiFox.openTab(e);
		}, false);
		link.setAttribute("value", name);
		return link;
	},

	createLink : function(name, url) {
		var link = document.createElement("label");
		link.setAttribute("class", "text-link");
		link.setAttribute("href", url);
		link.setAttribute("value", name);
		return link;
	},

	createLinkNew : function(name, url) {
		var link = document.createElement("label");
		link.setAttribute("class", "xwikifox-link");
		link.setAttribute("href", url);
		link.addEventListener("click", function(e) {
			xwikiFox.openTab(e);
		}, false);
		link.setAttribute("value", name);
		return link;
	},

	removeXmlChilds : function(elem) {
		while (elem.hasChildNodes())
			elem.removeChild(elem.lastChild);
	},

	getWikis : function() {
		try {
			var wikiList = this.prefManager.getCharPref("wikisListAsString");
		} catch (e) {
			return;
		}
		var wikis = wikiList.split(" ");
		return wikis;
	},

	createItem : function(name, url, partId) {
		var treeitem = document.createElement("treeitem");
		var treerow = document.createElement("treerow");
		var treecell = document.createElement("treecell");
		var treechildren = document.createElement("treechildren");
		treechildren.setAttribute("id", partId + name);
		treecell.setAttribute("label", name);
		treecell.setAttribute("href", url);
		treecell.setAttribute("src",
				"chrome://xwikifox/content/images/folder.png")
		// treecell.setAttribute("id", "xwikifox-" + name);
		treeitem.setAttribute("container", true);
		// treeitem.setAttribute("open", true);
		treerow.appendChild(treecell);
		treeitem.appendChild(treerow);
		treeitem.appendChild(treechildren);
		return treeitem;
	},

	addDocuments : function(aEvent) {
		if (aEvent.attrName == "open") {
			if (aEvent.target.getAttribute("open") == "true") {
				window.setCursor("wait");
				xwikiFox.openTreeUrlBool = false;
				var tree = document.getElementById("xwikifox-spaces-tree");
				try {
					tree.view.selection.clearSelection();
				} catch (e) {
					alert("error");
				}
				var space = aEvent.target.firstChild.firstChild
						.getAttribute("label");
				var childrenId = "xwikifox-space-children-" + space;
				var treechildren = document.getElementById(childrenId);

				xwikiFox.getDocuments(space, treechildren);
			}
		}
	},

	openTab : function(e) {
		var win = Components.classes['@mozilla.org/appshell/window-mediator;1']
				.getService(Components.interfaces.nsIWindowMediator)
				.getMostRecentWindow('navigator:browser');
		win.gBrowser.selectedTab = win.gBrowser.addTab(e.currentTarget
				.getAttribute("href"));
		return false;
	}

};

function FeedTestResultListener(container, numberFeeds) {
	this.container = container;
	this.numberFeeds = numberFeeds;
}

FeedTestResultListener.prototype = {
	handleResult : function(result) {
		var feed = result.doc;
		feed.QueryInterface(Components.interfaces.nsIFeed);
		this.removeXmlChilds(this.container);
		var itemArray = feed.items;
		var numItems = itemArray.length;
		if (numItems > this.numberFeeds)
			numItems = this.numberFeeds;
		var i;
		for (i = 0; i < numItems; i++) {
			theEntry = itemArray.queryElementAt(i,
					Components.interfaces.nsIFeedEntry);
			if (theEntry) {
				var name = theEntry.title.text;
				var summary = theEntry.summary.text;
				var date = theEntry.updated;
				var url = theEntry.link.resolve("");
				var row = this.createChangeRow(name, summary, date, url);
				this.container.appendChild(row);
			}
		}
	},

	createChangeRow : function(name, summary, date, url) {
		var rowContainer = document.createElement("hbox");
		rowContainer.setAttribute("id", "xwikifox-change-row");
		var avatarContainer = document.createElement("vbox");
		var avatarImage = document.createElement("image");
		var spacer = document.createElement("spacer");
		spacer.setAttribute("flex", 1);
		avatarContainer.appendChild(avatarImage);
		avatarContainer.appendChild(spacer);
		avatarImage.setAttribute("src",
				"chrome://xwikifox/content/images/page_edit.png");
		rowContainer.appendChild(avatarContainer);
		var horizontalContainer = document.createElement("vbox");
		var link = xwikiFox.createLinkNew(name, url);
		horizontalContainer.appendChild(link);
		var summaryElement = document.createElement("description");
		var temp = summary.split(" edited by ");
		var version = temp[0];
		var temp2 = temp[1].split(" on ");
		var user = temp2[0];
		var temp3 = temp2[1].split(" CEST ");
		var date = temp3[0];
		var versionText = document.createTextNode(version);
		var versionContainer = document.createElement("vbox");
		versionContainer.setAttribute("id", "xwikifox-recent-version");
		versionContainer.appendChild(versionText);
		var editedByText = document.createTextNode(" edited by ");
		var userText = document.createTextNode(user);
		var onText = document.createTextNode(" on ");
		var userContainer = document.createElement("vbox");
		userContainer.setAttribute("id", "xwikifox-recent-user");
		userContainer.appendChild(userText);
		var dateText = document.createTextNode(date);
		var dateContainer = document.createElement("vbox");
		dateContainer.setAttribute("id", "xwikifox-recent-date");
		dateContainer.appendChild(dateText);
		// var summaryText = document.createTextNode(summary);
		var summaryContainer = document.createElement("vbox");
		summaryContainer.setAttribute("id", "xwikifox-summary-container");
		summaryElement.appendChild(versionContainer);
		summaryElement.appendChild(editedByText);
		summaryElement.appendChild(userContainer);
		summaryElement.appendChild(onText);
		summaryElement.appendChild(dateContainer);
		// summaryElement.appendChild(summaryText);
		summaryContainer.appendChild(summaryElement);
		horizontalContainer.appendChild(summaryContainer);
		var dateLabel = document.createElement("label");
		dateLabel.setAttribute("value", date);
		// horizontalContainer.appendChild(dateLabel);
		rowContainer.appendChild(horizontalContainer);
		return rowContainer;

	},

	removeXmlChilds : function(elem) {
		while (elem.hasChildNodes())
			elem.removeChild(elem.lastChild);
	}

};

var xwikiFox = new XWikiFox();
window.addEventListener("load", function(e) {
	xwikiFox.load(e);
}, false);
