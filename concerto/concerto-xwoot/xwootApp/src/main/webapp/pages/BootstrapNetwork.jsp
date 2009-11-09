<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>
  <script type="text/javascript">
  	function togleCustomNetworkSettings(enable) {
  		var disabled = !enable;
  		
  	  	document.getElementById("rdvSeedingUri").disabled = disabled;
  	  	document.getElementById("relaySeedingUri").disabled = disabled;
  		document.getElementById("rdvSeeds").disabled = disabled;
  		document.getElementById("relaySeed").disabled = disabled;
  		
  		document.getElementById("beRendezVous").disabled = disabled;
  		document.getElementById("beRelay").disabled = disabled;
  	}

  	function togleExternalIpSettings(enable) {
  	  	var disabled = !enable;
  	  	
		document.getElementById("externalIp").disabled = disabled;
		document.getElementById("useOnlyExternalIp").disabled = disabled;
  	}

  	function togleTcpSettings(enable) {
		var disabled = !enable;

		document.getElementById("tcpPort").disabled = disabled;
		document.getElementById("useTcpIncomming").disabled = disabled;
		document.getElementById("useMulticast").disabled = disabled;
  	}

  	function togleHttpSettings(enable) {
		var disabled = !enable;

		document.getElementById("httpPort").disabled = disabled;
		document.getElementById("useHttpIncomming").disabled = disabled;
  	}
  </script>
  
  <div id="boxes">
    <div id="box-unic" class="box"  style="width:780px;">
      <div id="box_title">-- Network bootstrap --</div>
      <div class="menu_margin">
      <form id="button" method="post" action="">
          <div id="create_network" style="border-bottom: 3px solid darkgray; padding-bottom: 10px; margin-bottom: 10px">
          	<b>Create network</b><br/><br/>
            <input type="submit" value="Create" name="networkChoice"/>
          </div>
          <div id="join_network">
          	<b>Join network</b><br/><br/>
          	
          	<input id="useConcertoNetwork" type="radio" name="useNetwork" value="concerto" ${useConcertoNetwork} onclick="togleCustomNetworkSettings(false)"/>
          	<label for="useConcertoNetwork">Use default XWiki Concerto network. (Recommended)</label><br/>
          	
          	<input id="usePublicJxtaNetwork" type="radio" name="useNetwork" value="publicJxta" ${usePublicJxtaNetwork} onclick="togleCustomNetworkSettings(false)"/>
          	<label for="usePublicJxtaNetwork">Use JXTA public network. (Not recommended. Use only for tests.)</label><br/>
          	
          	<input id="useCustomNetworkRadio" type="radio" name="useNetwork" value="custom" ${useCustomNetwork} onclick="togleCustomNetworkSettings(true)"/>
          	<label for="useCustomNetworkRadio">Use a custom network.</label><br/>
          	
          	<c:if test="${empty useCustomNetwork}">
          		<c:set var="disableCustomNetworkSettings" value='disabled="disabled"'/>
          	</c:if>
          	<div id="join_custom_network" style="padding-left:40px">
	          	<label for="rdvSeedingUri">RDV SeedingUri:</label>
	          	<input id="rdvSeedingUri" type="text" name="rdvSeedingUri" size="50" value="${rdvSeedingUri}" ${disableCustomNetworkSettings} /><br/>
	          	
	          	<label for="relaySeedingUri">Relay SeedingUri:</label>
	          	<input id="relaySeedingUri" type="text" name="relaySeedingUri" size="50" value="${relaySeedingUri}" ${disableCustomNetworkSettings} /><br/>
	          	
	          	<label for="rdvSeeds">RDV Seeds:</label>
	          	<input id="rdvSeeds" type="text" name="rdvSeeds" size="50" value="${rdvSeeds}" ${disableCustomNetworkSettings} />(ex: tcp://192.168.1.200:9701, http://192.18.37.36:9700, etc...)<br/>
	          	
	          	<label for="relaySeed">Relay Seeds:</label>
	          	<input id="relaySeed" type="text" name="relaySeeds" size="50" value="${relaySeeds}" ${disableCustomNetworkSettings} />(ex: tcp://192.168.1.200:9701, http://192.18.37.36:9700, etc...)<br/>
	            
	            <input id="beRendezVous" type="checkbox" name="beRendezVous" value="true" ${beRendezVous} ${disableCustomNetworkSettings} />
	            <label for="beRendezVous">Route communication for this network (WARNING: can lead to network island if no other seeds are specified or not accessible for a long time!)</label><br/>
            	
            	<input id="beRelay" type="checkbox" name="beRelay" value="true" ${beRelay} ${disableCustomNetworkSettings} />
            	<label for="beRelay">Relay communication for Firewalled/NAT-ed peers in this network.</label><br/>
			</div>
			
			<br/>
			<input type="submit" value="Join" name="networkChoice"/>
			
			<div id="common_settings" style="border-top: 1px solid darkgray; padding-top: 10px; margin-top: 10px">
				<b>Common(Advanced) settings</b><br/><br/>
				
				<c:if test="${empty useExternalIp}">
          			<c:set var="disableExternalIpSettings" value='disabled="disabled"'/>
          		</c:if>
				<input id="useExternalIp" type="checkbox" name="useExternalIp" value="true" ${useExternalIp} onclick="togleExternalIpSettings(this.checked)" />
				<label for="useExternalIp">Use external IP</label><br/>
				
				<div id="external_ip_settings" style="padding-left: 20px">
					<label for="externalIp">External IP:</label>
					<input id="externalIp" type="text" name="externalIp" value="${externalIp}" ${disableExternalIpSettings} /> <br/>
					
					<input id="useOnlyExternalIp" type="checkbox" name="useOnlyExternalIp" value="true" ${useOnlyExternalIp} ${disableExternalIpSettings} />
					<label for="useOnlyExternalIp">Use only external IP for all communication</label><br/>
    			</div>
    			
    			
    			<c:if test="${empty useTcp}">
          			<c:set var="disableTcpSettings" value='disabled="disabled"'/>
          		</c:if>
    			<input id="useTcp" type="checkbox" name="useTcp" value="true" ${useTcp} onclick="togleTcpSettings(this.checked)" />
    			<label for="useTcp">Use TCP</label><br/>
    			
				<div id="Tcp_settings" style="padding-left: 20px">
					<label for="tcpPort">TCP port:</label>
					<input id="tcpPort" type="text" name="tcpPort" value="${tcpPort}" ${disableTcpSettings} /> (Default is 9701) <br/>
					
					<input id="useTcpIncomming" type="checkbox" name="useTcpIncomming" value="true" ${useTcpIncomming} ${disableTcpSettings} />
					<label for="useTcpIncomming">Accept incoming TCP connections. (Uncheck if port is blocked)</label><br/>
					
					<input id="useMulticast" type="checkbox" name="useMulticast" value="true" ${useMulticast} ${disableTcpSettings} />
					<label for="useMulticast">Use TCP Multicast to discover and communicate with network members in LAN.</label><br/>
    			</div>
    			
    			
    			<c:if test="${empty useHttp}">
          			<c:set var="disableHttpSettings" value='disabled="disabled"'/>
          		</c:if>
    			<input id="useHttp" type="checkbox" name="useHttp" value="true" ${useHttp} onclick="togleHttpSettings(this.checked)" />
    			<label for="useHttp">Use HTTP</label><br/>
    			
				<div id="Http_settings" style="padding-left: 20px">
					<label for="httpPort">Http port:</label>
					<input id="httpPort" type="text" name="httpPort" value="${httpPort}" ${disableHttpSettings} />  (Default is 9700. Recommended are 80 or 8080) <br/>
					
					<input id="useHttpIncomming" type="checkbox" name="useHttpIncomming" value="true" ${useHttpIncomming} ${disableHttpSettings} />
					<label for="useHttpIncomming">Accept incoming Http connections. (Uncheck if port is blocked)</label><br/>
    			</div>
    			
			</div>
          </div>
        </form>        
      </div>
    </div>
    <div class="clearer"></div>
  </div>
<%@ include file="footer.jsp" %>
