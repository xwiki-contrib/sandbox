<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">    
    <div id="box-unic" class="box" style="width:775px;">
      <form method="post">
        <div>
          <div><label for="xwiki_endpoint">XWiki endpoint (Ex : http://my.local.dns.fr:8080/xwiki/xmlrpc)</label></div>
          <input type="text" value="${xwiki_properties.xwiki_endpoint}" name="xwiki_endpoint" size="50" style="margin-left:20px;"/>

          <div><label for="xwiki_username">XWiki user login (Ex : Admin)</label></div>
          <input type="text" value="${xwiki_properties.xwiki_username}" name="xwiki_username" size="50" style="margin-left:20px;"/>

          <div><label for="xwiki_password">XWiki user password</label></div>
          <input type="password" value="${xwiki_properties.xwiki_password}" name="xwiki_password" size="50" style="margin-left:20px;"/> 

          <div><label for="xwoot_working_dir">Datas serialization folder (Ex : c:\tmp\xwootDatas ou /home/toto/tmp/xwootDatas)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_working_dir}" name="xwoot_working_dir" size="50" style="margin-left:20px;"/>

<%--
          <div><label for="xwoot_site_id">XWoot unic ID -- Integer value (Ex : 19216801)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_site_id}" name="xwoot_site_id" size="50" style="margin-left:20px;"/>

          <div><label for="xwoot_server_url">XWoot application URL (Ex : http://my.local.dns.fr:8080/xwootApp)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_server_url}" name="xwoot_server_url" size="50" style="margin-left:20px;"/>
--%>
 		  
          <div><label for="xwoot_server_name">XWoot server name (Ex : Chewbacca)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_server_name}" name="xwoot_server_name" size="50" style="margin-left:20px;"/>

<%-- 
          <div><label for="xwoot_refresh_log_delay">XWoot refresh log delay in seconds (Ex : 60)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_refresh_log_delay}" name="xwoot_refresh_log_delay" size="5" style="margin-left:20px;"/>

          <div><label for="xwoot_neighbors_list_size">Max size of neighbors list (Ex : 3)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_neighbors_list_size}" name="xwoot_neighbors_list_size" size="5" style="margin-left:20px;"/>

          <div><label for="xwoot_pbcast_round">Round number per propagation (Ex : 3)</label></div>
          <input type="text" value="${xwoot_properties.xwoot_pbcast_round}" name="xwoot_pbcast_round" size="5" style="margin-left:20px;"/>
--%>
          <div>
            <input type="submit" name="update" value="Save" class="menu-margin" style="margin-left:20px;margin-bottom:10px;"/>
          </div>
        </div>
      </form>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
