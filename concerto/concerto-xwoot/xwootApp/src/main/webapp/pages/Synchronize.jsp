<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
    
        <%-- @import "style/${(skin==null or skin=='')?'grey':skin}_style.css"; --%>
        @import "style/grey_style.css";
    
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
            <img class="small_icon" src="images/xconcerto20_2.ico" style="vertical-align:middle;padding-right:5px;"/><a style="font-size:15px;" href="${xwiki_url}" title="Go to xwiki">${xwiki_url}</a>
            
            <!-- Network connection status -->
            <c:if test="${networkConnection eq -1}">
            	<img class="icon" src="images/networkNotConnected.png" alt="Not connected to network" title="Not connected to network" style="vertical-align:middle;padding-right:5px;" />
            </c:if>
            <c:if test="${networkConnection eq 0}">
            	<img class="icon" src="images/networkRDVWithNoConnections.png" alt="No peers conencted to this network RDV" title="No peers conencted to this network RDV" style="vertical-align:middle;padding-right:5px;" />
            </c:if>
            <c:if test="${networkConnection eq 1}">
            	<img class="icon" src="images/networkConnected.png" alt="Connected to network" title="Connected to network" style="vertical-align:middle;padding-right:5px;" />
            </c:if>
            
            <!-- Group connection status -->
            <c:if test="${groupConnection eq -1}">
                <img class="icon" src="images/groupNotConnected.png" alt="Not connected to group" title="Not connected to group" style="vertical-align:middle;padding-right:5px;" />
            </c:if>
            <c:if test="${groupConnection eq 0}">
                <img class="icon" src="images/groupRDVWithNoConnections.png" alt="No peers connected to this RDV" title="No peers connected to this RDV" style="vertical-align:middle;padding-right:5px;" />
            </c:if>
            <c:if test="${groupConnection eq 1}">
                <img class="icon" src="images/groupConnected.png" alt="Connected to group" title="Connected to group" style="vertical-align:middle;padding-right:5px;" />
            </c:if>
        </div>
        <div id="toolbar">
            <c:if test="${p2pconnection eq false}">
                <a href="synchronize.do?action=p2pnetworkconnection" title="Connect to P2P Network."><img class="icon" src="images/P2Pon.png"/></a>
                <img class="icon" src="images/P2Poffoff.png"/>
            </c:if>
            <c:if test="${p2pconnection eq true}">
                <img class="icon" src="images/P2Ponoff.png"/>
                <a href="synchronize.do?action=p2pnetworkconnection" title="Disconnect from P2P Network."><img class="icon" src="images/P2Poff.png"/></a>
            </c:if>
            
            <!--  <a href="synchronize.do?action=addNeighbor" title="Add a new neighbor."><img class="icon" src="images/AddNeighbor.png"/></a>-->
            <img class="icon" src="images/barre.png"/>
            <c:if test="${cpconnection eq false}">
                <a href="synchronize.do?action=cpconnection" title="Connect to xwiki server."><img class="icon" src="images/xwikion.png"/></a>
                <img class="icon" src="images/xwikioffoff.png"/>
                <!--  <img class="icon" src="images/CustomPageoff.png"/>
                <img class="icon" src="images/AddAllPageoff.png"/>
                <img class="icon" src="images/RemoveAllPageoff.png"/>
                <img class="icon" src="images/SynchronizePageoff.png"/>-->
            </c:if>    
            <c:if test="${cpconnection eq true}">
                <img class="icon" src="images/xwikionoff.png"/>
                <a href="synchronize.do?action=cpconnection" title="Disconnect from xwiki server."><img class="icon" src="images/xwikioff.png"/></a>
                <!-- <a href="synchronize.do?action=pageManagement&val=custom" title="Custom add/remove wiki page."><img class="icon" src="images/CustomPage.png"/></a>
                <a href="synchronize.do?action=pageManagement&val=all" title="Add all wiki pages."><img class="icon" src="images/AddAllPage.png"/></a>
                <a href="synchronize.do?action=pageManagement&val=remove" title="Remove all wiki pages."><img class="icon" src="images/RemoveAllPage.png"/></a>
                <a href="synchronize.do?action=synchronize" title="Synchronize selected pages."><img class="icon" src="images/SynchronizePage.png"/></a>-->
            </c:if>
            
            <!-- There should be no need for this anymore.
            <img class="icon" src="images/barre.png"/>
            <a href="<%= request.getHeader("referer") %>"><img class="icon" src="images/goback.png"/>Back to <%= request.getHeader("referer") %></a>
             -->
        </div>
        <div id="boxes">            
            <!--<div id="box-left" class="box">
            <c:if test="${customPage eq true}">
                <div>
                    <c:if test="${noPageChoice eq true}">
                        <div>
                            <c:if test="${spaces eq null}"> No space in xwiki </c:if>
                            <c:if test="${spaces ne null}">
                                <div id="box_title">-- Spaces --</div>
                                <div class="menu_margin">
                                    <form id="button" method="post" action="synchronize.do?action=pageManagement&val=custom&actionManagement=printPages">
                                        <select name="currentSpace">
                                            <c:forEach items="${spaces}" var="space_i">
                                                <c:if test="${currentSpace eq space_i}"> <option value=${space_i} selected="selected">${space_i}</option></c:if>
                                                <c:if test="${currentSpace ne space_i}"> <option value=${space_i}>${space_i}</option></c:if>
                                            </c:forEach>
                                        </select>
                                        <input type="submit" value="Select" />
                                    </form>
                                
                                    <form method="post" action="synchronize.do">
                                        <input type="submit" value="Back" />
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </c:if>
                        
                    <c:if test="${noPageChoice ne true}">
                        <div id="box_title">--  ${currentSpace}'s pages --</div>
                        
                        <div class="checkbox">    
                            <c:if test="${managedPages eq null}">
                                No page in this space.<br>
                                !!There is probably a problem with xwiki!!
                            </c:if>
                            <c:if test="${managedPages ne null}">                            
                                    <form method="post" action="synchronize.do?action=pageManagement&val=custom&actionManagement=validate&space=${currentSpace}">
                                        <div>
                                            <c:forEach items="${managedPages}" var="page">
                                                <c:if test="${page.value eq true}"><input type="checkbox" name="PAGE-${page.key}" checked>${page.key} </input></c:if>
                                                <c:if test="${page.value eq false}"><input type="checkbox" name="PAGE-${page.key}">${page.key} </input></c:if>
                                                <br>
                                            </c:forEach>
                                        </div>
                                        <div id="button" class="button_margin">
                                            <input type="submit" value="Validate" />
                                        </div>
                                    </form>
                                
                                <div id="button" class="button_margin">
                                    <form method="post" action="synchronize.do">
                                        <input type="submit" value="Cancel" />
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </c:if>
                </div>
            </c:if>
            <c:if test="${customPage ne true}">
                <div id="box_title">-- Page list --<br> Site is 
                            <c:if test="${cpconnection eq true}">
                                connected to XWiki
                            </c:if>
                            <c:if test="${cpconnection eq false}">
                                disconnected from XWiki
                            </c:if></div>
                <div>
                    <c:forEach items="${pages}" var="page">
                        <ul><li>${page}</li></ul>
                    </c:forEach>
                </div>
            </c:if>
            </div>-->
            <div id="box-left" class="box">
                  <div id="box_title">
                  Content provider configuration<br/><br/>
                  </div>
<table>
	<tr>
		<td>Endpoint:</td>
		<td>${content_provider.endpoint}</td>
	</tr>
	<tr>
		<td>Connected:</td>
		<td>${content_provider.connected}</td>
	</tr>
	<tr>
		<td>Ignore patterns:</td>
		<td>${content_provider.configuration.ignorePatterns}</td>
	</tr>
	<tr>
		<td>Accept patterns:</td>
		<td>${content_provider.configuration.acceptPatterns}</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	<tr>
		<td colspan="2"><a href="contentProviderDiagnostics">Diagnostics
		page</a></td>
	</tr>
</table>
            </div>            
            <div id="box-right" class="box">
                <div id="box_title">
                -- Neighbor list -- <br> Site is 
                <c:if test="${p2pconnection eq true}">
                    connected to P2P network
                </c:if>
                <c:if test="${p2pconnection eq false}">
                    disconnected from P2P network
                </c:if>
                    
                </div>
                <c:if test="${action eq 'addneighbor'}">
                    <div class="menu_margin">
                        <form id="button" method="post" action="synchronize.do?action=addNeighbor">
                                <input type="text" name="neighbor" size="50" />
                                <input type="submit" value="Add" /> 
                        </form>
                        <form method="post" action="synchronize.do">
                            <input type="submit" value="Cancel" />
                        </form>
                    </div>
                </c:if>
                <c:if test="${noneighbor eq false}">
                <div>
                    <ul>
                        <c:forEach items="${neighbors}" var="neighbor">
                        <li>
                            <table cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <%-- <td><a href="synchronize.do?action=removeNeighbor&neighbor=${neighbor.key}" title="delete ${neighbor.key}"><img class="small_icon" src="images/delete.png" /></a></td> --%>
                                    <c:if test="${neighbor.value eq true}">
                                        <td><a href="synchronize.do?action=antiEntropy&neighbor=${neighbor.key.pipeID}" title="start anti-entropy with ${neighbor.key.name}"><img class="small_icon" src="images/anti-entropy.png" /></a></td>
                                        <td><%-- <a href="${neighbor.key}" title="go to ${neighbor.key}"> --%>${neighbor.key.name}<%--</a> --%></td>
                                    </c:if>
                                    <c:if test="${neighbor.value eq false}">
                                        <td><img class="small_icon" src="images/anti-entropy_grey.png" /></td>
                                        <%--<td><img class="small_icon" src="images/state_grey.png" /></td>--%>
                                        <td id="deconnected_neighbor">${neighbor.key.name}</td>
                                    </c:if>
                                </tr>
                            </table>
                        </li>
                        </c:forEach>
                    </ul>
                </div>
                </c:if>            
            </div>
            <div class="clearer">
            </div>
        </div>
    </div>
</body>