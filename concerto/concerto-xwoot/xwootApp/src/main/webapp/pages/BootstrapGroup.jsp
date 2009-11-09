<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <script type="text/javascript">
	function togleGroupPasswordSettings(enable) {
		var disabled = !enable;
	  		
	  	document.getElementById("createGroupPassword").disabled = disabled;
	  	document.getElementById("createGroupPasswordRetyped").disabled = disabled;
	}
  </script>
  <div id="boxes">
    <div id="box-unic" class="box"  style="width:780px;">
      <div id="box_title">-- Group bootstrap --</div>
      <div class="menu_margin">
        <form id="button" method="post" action="">
        
          <div id="create_group" style="border-bottom: 3px solid darkgray; padding-bottom: 10px; margin-bottom: 10px">
          	<b>Create group</b><br/><br/>
          	
          	<label for="groupName">Name:</label>
            <input id="groupName" type="text" name="groupName" size="50" /><br/>
            
            <label for="groupDescription">Description:</label>
            <input id="groupDescription" type="text" name="groupDescription" size="50" /><br/>
            
            <input id="isPrivateGroup" type="checkbox" name="isPrivateGroup" value="true" onclick="togleGroupPasswordSettings(this.checked)" />
            <label for="isPrivateGroup">This is a private group.</label><br/>
            
            <div id="group_password">
            	<label for="createGroupPassword">Password:</label>
            	<input id="createGroupPassword" type="password" name="createGroupPassword" disabled="disabled" /><br/>
            	
            	<label for="createGroupPasswordRetyped">Retype Password:</label>
            	<input id="createGroupPasswordRetyped" type="password" name="createGroupPasswordRetyped" disabled="disabled" /><br/>
            	
            	<!-- Local Keystore Password: <input type="password" name="createKeystorePassword" /><br/>  -->
            </div>
			<br/>
            <input type="submit" value="Create" name="groupChoice"/>
          </div>
          
          <div id="join_group" style="width=500px;">
         	<b>Join group</b><br/><br/>
         	
         	<select name="groupID">
            	<c:forEach items="${groups}" var="group">
            	<option value="${group.peerGroupID}"><c:out value="${group.name}"></c:out></option>
            	</c:forEach>
            </select><br/>
            
            <label for="joinGroupPassword">Password:</label>
            <input id="joinGroupPassword" type="password" name="joinGroupPassword" /><br/>
            
            <!-- Local Keystore Password: <input type="password" name="joinGroupKeystorePassword" /><br/>  -->
            
            <input id="beRendezVous" type="checkbox" name="beRendezVous" value="true" />
            <label for="beRendezVous">Route communication for this group</label><br/>
			<br/>
			
			<input type="submit" value="Refresh List" name="groupChoice"/>
			<input type="submit" value="Join" name="groupChoice"/>
          </div>
        </form>
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>