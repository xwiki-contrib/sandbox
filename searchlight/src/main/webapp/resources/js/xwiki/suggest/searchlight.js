document.observe("dom:loaded", function(){

  if (typeof XWiki != 'undefined' && typeof XWiki.widgets != 'undefined' && typeof XWiki.widgets.Suggest != 'undefined') {
 
    var spotlight =
      // Create the Suggest.
      new XWiki.widgets.Suggest( $('headerglobalsearchinput'), {
        parentContainer: $('mainmenu'),
        className: 'ajaxsuggest spotlight',
        relativeSize: 2.5,
        align: "right",
        sources : [
          #set($configSources = $xwiki.searchDocuments(", BaseObject as obj where doc.fullName = obj.name and obj.className = 'XWiki.SearchSuggestSourceClass'"))
           #foreach($sourceDocumentName in $configSources)
            #set($sourceDocument = $xwiki.getDocument($sourceDocumentName))
            #foreach($source in $sourceDocument.getObjects('XWiki.SearchSuggestSourceClass'))
              #if($source.getProperty('activated').value == 1)
              {
                name : "$escapetool.javascript($source.display('name','view'))",
                varname : 'input',
                script : "#evaluate($source.getProperty('url').value)&query=$source.getProperty('query').value&nb=$source.getProperty('resultsNumber').value&",
                icon : "#evaluate($source.getProperty('icon').value)",
                highlight: #if($source.getProperty('highlight').value == 1) true #else false #end
              },
              #end
           #end
          #end
 	  null  // Don't handle last coma. This is going to be compated anyway.
        ].compact()
      });

  }

});

