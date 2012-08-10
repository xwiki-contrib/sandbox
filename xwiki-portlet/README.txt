Integration steps:

1. Dependency management
-----------------------------------------------------------
1.1. Remove portlet-api-1.0.jar
1.2. Copy nekohtml-1.9.16.jar
1.3. Copy rhino-1.7R4.jar
1.4. Copy xwiki-core-portlet-3.5.1.jar
1.5. Repackage yuicompressor because it conflicts with Rhino
1.5. Repackage boilerpipe because it conflicts with NekoHTML parser
1.6. You may have to update the prototype.js used by the portal with the one used by XWiki

2. Configuration
-----------------------------------------------------------
2.1. Copy portlet.xml
2.2. Copy editportlet.vm
2.3. Modify xwikivars.vm

#set ($isInPortletMode = $xwikimode == 1 || "$!request.getAttribute('javax.portlet.request')" != '')
#set ($isInServletMode = $xwikimode == 0 && !$isInPortletMode)

2.4. Modify web.xml

<!-- Filter that rewrites the XWiki output to be able to include it in a portal page. -->
<filter>
  <filter-name>XWiki Portlet Filter</filter-name>
  <filter-class>org.xwiki.portlet.controller.DispatchFilter</filter-class>
</filter>

<!--
  This filter must be the first one applied in portlet mode because it wraps the request and response objects to fix
  some of the differences between portlet and servlet mode.
-->
<filter-mapping>
  <filter-name>XWiki Portlet Filter</filter-name>
  <url-pattern>/*</url-pattern>
  <dispatcher>INCLUDE</dispatcher>
  <dispatcher>FORWARD</dispatcher>
</filter-mapping>

2.5. Wrap HTML element identifiers used in JavaScript code with ID('someHTMLElementId')
2.5.1. Add the ID function to javascript.vm

var ID = function(id){return id}

2.5.2. Wrap the WYSIWYG editor 'hookId' configuration parameter (wysiwyg_writeConfig macro in macros.vm)

#if($entry.key.equals('hookId'))
  ${separator}$entry.key: ID('$!{escapetool.javascript($entry.value)}')
#else
  ${separator}$entry.key: '$!{escapetool.javascript($entry.value)}'
#end

2.5.3 Wrap 'Comments' with ID function in js/uicomponents/viewers/comments.js

2.6 Modify javascript.vm by adding the following lines before prototype.js

#if($isInPortletMode)
$xwiki.jsfx.use('js/xwiki/portletOverwrites.js', {'forceSkinAction': true})
#end

2.7 Copy portletOverwrites.js