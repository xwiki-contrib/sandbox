const
XWikiFoxPref = {
	prefManager :Components.classes['@mozilla.org/preferences-service;1']
			.getService(Components.interfaces.nsIPrefService).getBranch(
					"extensions.xwikifox."),

	onLoad : function() {
		this.populateWikisListBox();
	},

	addWiki : function() {
		window.openDialog("chrome://xwikifox/content/wikidialog.xul", "_blank",
				"chrome,resizable=no,dependent=yes");
	},

	removeWiki : function() {
		var listbox = document.getElementById("wikis");
		toRemoveWiki = listbox.selectedItem.value;
		var newWikisString = "";
		wikis = this.getWikis();
		for ( var i = 0; i < wikis.length; i++) {
			var wiki = wikis[i].split(";");
			if (wiki[1] == toRemoveWiki) {

			} else {
				if (i < wikis.length - 1)
					newWikisString = newWikisString + wikis[i] + " ";
				else
					newWikisString = newWikisString + wikis[i];
			}
		}
		if(newWikisString.charAt(newWikisString.length - 1) == ' ')
			newWikisString = newWikisString.substring(0, newWikisString.length - 1);
		this.prefManager.setCharPref("wikisListAsString", newWikisString);
		listbox.removeItemAt(listbox.selectedIndex);
		
	},

	submitWiki : function() {
		var wikiRestUrl = document.getElementById("xwikifox-wikiRestUrl").value;
		var wikiMainUrl = document.getElementById("xwikifox-wikiMainUrl").value;
		var username = document.getElementById("xwikifox-username").value;
		var wikiString;
		if (!(wikiRestUrl && wikiMainUrl)) {
			alert("You must enter a REST Url and a Main Url");
			return false;
		}
		try {
			this.prefManager.getCharPref("wikisListAsString");
		} catch (e) {
			this.prefManager.setCharPref("wikisListAsString", "");
		}
		if (this.prefManager.getCharPref("wikisListAsString") != "") {
			if (username == "")
				wikiString = " " + wikiRestUrl + ";" + wikiMainUrl;
			else {
				wikiString = " " + wikiRestUrl + ";" + wikiMainUrl + ";"
						+ username;
			}
		} else {
			if (username == "")
				wikiString = wikiRestUrl + ";" + wikiMainUrl;
			else {
				wikiString = wikiRestUrl + ";" + wikiMainUrl + ";" + username;
			}
		}
		var prevWikiListAsString = this.prefManager
				.getCharPref("wikisListAsString");
		this.prefManager.setCharPref("wikisListAsString", prevWikiListAsString
				+ wikiString);
		var listbox = opener.document.getElementById("wikis");
		var item = listbox.appendItem(wikiMainUrl, wikiMainUrl);
		listbox.selectItem(item);
		this.prefManager.setCharPref("currentMainWiki", wikiMainUrl);
		this.prefManager.setCharPref("currentRestWiki", wikiRestUrl);
		this.prefManager.setCharPref("currentUsername", username);
	},
	
	onSubmit : function() {

	},

	populateWikisListBox : function() {
		var wikis = this.getWikis();
		for ( var i = 0; i < wikis.length; i++) {
			var wiki = wikis[i].split(";");
			var listbox = document.getElementById("wikis");
			listbox.appendItem(wiki[1], wiki[1]);
		}
	},

	getWikis : function() {
		try {
			var wikiList = this.prefManager.getCharPref("wikisListAsString");
		} catch (e) {
			return;
		}
		var wikis = wikiList.split(" ");
		return wikis;
	}
};
