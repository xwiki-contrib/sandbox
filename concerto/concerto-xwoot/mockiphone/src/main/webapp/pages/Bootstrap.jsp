<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <div id="boxes">    
    <div id="box-unic" class="box" style="width:775px;">
      <form method="post" action="">
        <div>
          <div><label for="iwoot_endpoint">IWoot endpoint (Ex : http://my.local.dns.fr:8080/iwoot)</label></div>
          <input type="text" value="${properties.iwoot_endpoint}" name="iwoot_endpoint" size="50" style="margin-left:20px;"/>

          <div><label for="mockiphone_working_dir">Datas serialization folder (Ex : c:\tmp\xwootDatas ou /home/toto/tmp/xwootDatas)</label></div>
          <input type="text" value="${properties.mockiphone_working_dir}" name="mockiphone_working_dir" size="50" style="margin-left:20px;"/>
          
          <div>
            <input type="submit" name="update" value="Save" class="menu-margin" style="margin-left:20px;margin-bottom:10px;"/>
          </div>
        </div>
      </form>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
