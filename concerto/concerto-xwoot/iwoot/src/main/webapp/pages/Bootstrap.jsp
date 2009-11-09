<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">    
    <div id="box-unic" class="box" style="width:775px;">
      <form method="post" action="">
        <div>
          <div><label for="iwoot_xwoot">XWoot type
	          <select name="iwoot_xwoot">
	          	<option value=real selected="selected">real</option>
	          	<option value=mock>mock</option>
	          </select>
	      </div>
	      
	      <div><label for="iwoot_xwoot_url">XWoot application URL (Ex : http://my.local.dns.fr:8080/xwootApp)</label></div>
          <input type="text" value="${properties.iwoot_xwoot_url}" name="iwoot_xwoot_url" size="50" style="margin-left:20px;"/>
          
          <div><label for="iwoot_xwoot">WCM type
	          <select name="iwoot_wcm">
	          	<option value=real selected="selected">real</option>
	          	<option value=mock>mock</option>
	          </select>
	      </div>
          <div></div>
	      <div><label for="iwoot_real_wcm_url">XWiki xml-rpc endpoint (Ex : http://my.local.dns.fr:8080/xwiki/xmlrpc)</label></div>
          <input type="text" value="${properties.iwoot_real_wcm_url}" name="iwoot_real_wcm_url" size="50" style="margin-left:20px;"/>

          <div><label for="iwoot_real_wcm_login">XWiki user login (Ex : Admin)</label></div>
          <input type="text" value="${properties.iwoot_real_wcm_login}" name="iwoot_real_wcm_login" size="50" style="margin-left:20px;"/>

          <div><label for="iwoot_real_wcm_pwd">XWiki user password</label></div>
          <input type="password" value="${properties.iwoot_real_wcm_pwd}" name="iwoot_real_wcm_pwd" size="50" style="margin-left:20px;"/> 

          <div>
            <input type="submit" name="update" value="Save" class="menu-margin" style="margin-left:20px;margin-bottom:10px;"/>
          </div>
        </div>
      </form>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
