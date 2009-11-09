<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ page import="java.util.List"%> 
<%@ page import="org.xwoot.contentprovider.Entry"%> 
<%@ page import="java.lang.String"%> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Content provider diagnostics</title>
</head>
<body>
<h1>Content provider diagnostics</h1>
<hr/>
<table>
	<tr>
		<td colspan="2">
		<h2>General configuration</h2>
		</td>
	</tr>
	<tr>
		<td><b>Property</td>
		<td><b>Value</b></td>
	</tr>
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
		<td>${config.ignorePatterns}</td>
	</tr>
	<tr>
		<td>Accept patterns:</td>
		<td>${config.acceptPatterns}</td>
	</tr>
	<tr>
		<td>Cumulative classes:</td>
		<td>${config.cumulativeClasses}</td>
	</tr>
	<tr>
		<td>Wootable properties:</td>
		<td>${config.wootablePropertiesMap}</td>
	</tr>
</table>

<hr/>

<table border="1">
	<tr>
		<td colspan="4">
		<h2>Content provider status</h2>
		</td>
	</tr>
	<tr>
		<td><b>PageId</b></td>
		<td><b>Timestamp</b></td>
		<td><b>Version</b></td>
		<td><b>Cleared</b></td>			
	</tr>
<%
  List<Entry> entries = (List<Entry>) request.getAttribute("entries");
  for(Entry entry : entries) {
%>
	<tr>
		<td><%= entry.getPageId() %></td>
		<td><%= entry.getTimestamp() %></td>
		<td><%= entry.getVersion() %>.<%= entry.getMinorVersion() %></td>
		<td><%= entry.isCleared() %></td>						
	</tr>  
<%      
  }
%>
</table>
</body>
</html>