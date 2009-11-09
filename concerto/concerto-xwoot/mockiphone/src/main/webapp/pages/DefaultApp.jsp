<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
    <meta name="author" content="ECOO Team - Maire Julien"/>
    <meta name="keywords" content="MockIphone, IWoot, Woot, XWoot, XWiki, ECOO Team, LORIA, Wooki"/>
    <meta name="description" content="P2P XWiki with Iphone"/>
    <meta name="robots" content="all"/>
    <title>XWoot</title>
    
    <style type="text/css" media="screen" title="currentStyle">
    
        @import "style/${(skin==null or skin=='')?'grey':skin}_style.css";
    
    </style>
    
    <link rel="Shortcut Icon" type="image/x-icon" href="images/xwoot.ico"/>        
</head>

<body id="body">
    <div id="content">    
        <div id="logo">
        </div>
        <div id="colors">
            <div>
                <a href="synchronize.do?skin=red" title="red XWoot !"><img src="images/red.png"/></a>
            </div>
            <div>
                <a href="synchronize.do?skin=grey" title="grey XWoot !"><img src="images/grey.png"/></a>
            </div>
            <div>
                <a href="synchronize.do?skin=orange" title="orange XWoot !"><img src="images/orange.png"/></a>
            </div>
            <div>
                <a href="synchronize.do?skin=green" title="green XWoot !"><img src="images/green.png"/></a>
            </div>    
        </div>
        <div id="boxes" style="padding-left:20px;">
            <img class="small_icon" src="images/xconcerto20_2.ico" style="vertical-align:middle;padding-right:5px;"/><a style="font-size:15px;" href="${xwiki_url}" title="Go to xwiki">${iwootUrl}</a>
        </div>
        <div id="toolbar">
        </div>
        <div id="boxes">
              <div id="box-left" class="box">
                <div id="box_title">-- Page list 
                 [<a href="defaultApp.do?action=refreshlist" title="Ask to IWoot the page list.">Refresh</a>]
                --<br> </div>
                <div>
                    <c:forEach items="${pagelist}" var="page">
                        <ul><li>${page}
                            [<a href="defaultApp.do?action=addpage&pagename=${page}" title="Ask this page to IWoot.">Ask</a>] </li></ul>
                    </c:forEach>
                </div>
                
                <div id="box_title">-- Managed Page list 
                    [<a href="defaultApp.do?action=createpage" title="Remove this page.">create new page</a>] --<br> 
                </div>
                <div>
                    <c:forEach items="${managedpagelist}" var="page">
                        <ul><li>
                            <a href="defaultApp.do?action=viewpage&pagename=${page.key}" title="View content of this page.">${page.key}</a>
                            <c:if test="${page.value eq true}">*</c:if>
                        </ul></li>
                    </c:forEach>
                </div>
            </div>
            
            <div id="box-right" class="box">    
                <c:if test="${editpage eq false}">
                    <c:if test="${nopage eq true}">
                        <div id="box_title">-- No page selected --</div>
                    </c:if>
                    <c:if test="${nopage eq false}">
                        <div id="box_title">-- ${pagename} [<a href="defaultApp.do?action=editpage&pagename=${pagename}" title="Edit content of this page.">edit</a>]
                                [<a href="defaultApp.do?action=deletepage&pagename=${pagename}" title="Remove this page.">delete</a>] </ul></li>
                                <c:if test="${iscurrentpagemodified eq true}">
                                    [<a href="defaultApp.do?action=sendpage&pagename=${pagename}" title="Send this page to IWoot.">send</a>] </ul></li>
                                </c:if> 
                                --<br>
                        </div>
                    </c:if>
                    <div class="checkbox">
                        <textarea style="width:97%; min-width:370px;" rows="8">${content}</textarea>
                    </div>
                </c:if>
                <c:if test="${editpage eq true}">
                    <div id="box_title">-- ${pagename} -- <br></div>
                    <div class="checkbox">

                        <form method="post" action="defaultApp.do?action=savepage&pagename=${pagename}">
                            <textarea name="newcontent" style="width:97%; min-width:370px;" rows="8">${content}</textarea>        

                            <input type="button" value="Cancel" onClick="history.go(-1)">
                            <input type="submit" value="Save" />
                        </form>
                    </div>
                </c:if>
            </div>
        </div>
         <div class="clearer"></div>
    </div>
</body>