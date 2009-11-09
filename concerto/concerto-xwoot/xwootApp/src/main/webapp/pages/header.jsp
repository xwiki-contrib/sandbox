<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
  <meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
  <meta name="author" content="ECOO Team - Maire Julien"/>
  <meta name="keywords" content="Woot, XWoot, XWiki, ECOO Team, LORIA, Wooki"/>
  <meta name="description" content="P2P XWiki"/>
  <meta name="robots" content="all"/>
  <title>XWoot</title>
  <style type="text/css" media="screen" title="currentStyle">
    @import "style/grey_style.css";
  </style>
  <link rel="Shortcut Icon" type="image/x-icon" href="images/xwoot.ico"/>        
</head>
<body id="body">
  <div id="content" style="width:800px;">    
    <div id="logo" style="width:780px;"></div>
    <div id="colors">
      <div><a href="${request.contextPath}${request.servletPath}?skin=red" title="red XWoot !"><img src="images/red.png"/></a></div>
      <div><a href="${request.contextPath}${request.servletPath}?skin=grey" title="grey XWoot !"><img src="images/grey.png"/></a></div>
      <div><a href="${request.contextPath}${request.servletPath}?skin=orange" title="orange XWoot !"><img src="images/orange.png"/></a></div>
      <div><a href="${request.contextPath}${request.servletPath}?skin=green" title="green XWoot !"><img src="images/green.png"/></a></div>
    </div>
    <div id="toolbar" style="padding-left:25px;">
      <c:if test="${xwiki_url ne null}">
        <img class="small_icon" src="images/xconcerto20_2.ico" style="vertical-align:middle;padding-right:5px;"/><a style="font-size:15px;" href="${xwiki_url}" title="Go to xwiki">${xwiki_url}</a>
      </c:if>
      <c:if test="${xwiki_url eq null}">
     -- Bootstrap : please configure parameters -- 
      </c:if>
    </div>
    <c:if test="${errors ne null}">
      <div style="color:#FF0000;">${errors}</div>
    </c:if>
